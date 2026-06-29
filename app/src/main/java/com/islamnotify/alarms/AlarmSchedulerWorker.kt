package com.islamnotify.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.islamnotify.common.AppUtils.toPrayerDataList
import com.islamnotify.main.presentation.MainActivity
import com.islamnotify.prayer_times.domain.LocationPrayerResult
import com.islamnotify.prayer_times.domain.PrayerDataUseCase
import com.islamnotify.prayer_times.domain.model.PrayerData
import com.islamnotify.prayer_times.domain.model.PrayerEntities
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId
@HiltWorker
class AlarmSchedulerWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    val alarmManager: AlarmManager,
    val prayerDataUseCase: PrayerDataUseCase,
    val prayerAlarmDao: PrayerAlarmDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "PrayerAlarmScheduler"
    }

    override suspend fun doWork(): Result {

        val alarmId = inputData.getInt(Constants.WORKER_KEY_ALARM_ID, -1)

        if (alarmId == -1) {
            try {
                prayerAlarmDao.updateAlarmStatus(alarmId, AlarmStatus.FAILED)
                return Result.failure()
            } catch (writeEx: Exception) {
                Log.e(TAG, "doWork: ", writeEx)
                return Result.failure()
            }
        }

        try {
            Log.d("PrayerAlarmScheduler", "alarm id sent from Scheduler to work manager: $alarmId")
            schedulePrayerAlarm(alarmId)
            return Result.success()
        } catch (e: Exception){
            try {
                prayerAlarmDao.updateAlarmStatus(alarmId, AlarmStatus.FAILED)
                return Result.failure()
            } catch (writeEx: Exception) {
                Log.e(TAG, "schedulePrayerAlarm: ", writeEx)
                return Result.failure()
            }
        }
    }


    suspend fun schedulePrayerAlarm(alarmId: Int) {

        // fetch the alarm data from database. update the status to failed if the operation failed
        val prayerAlarm = try {
            prayerAlarmDao.getAlarmById(alarmId)
        } catch (e: Exception) {
            try {
                Log.e(TAG, "schedulePrayerAlarm: couldn't get the alarm from database", e)
                prayerAlarmDao.updateAlarmStatus(alarmId, AlarmStatus.FAILED)
                return
            } catch (writeEx: Exception) {
                Log.e(TAG, "schedulePrayerAlarm: ", writeEx)
                return
            }
        }

        // fetch prayer times
        val prayerData: PrayerEntities? =
            when (val result = prayerDataUseCase.getPrayerDataWithLastLocation()) {
                is LocationPrayerResult.PrayerError -> {
                    null
                }

                is LocationPrayerResult.LocationError -> {
                    null
                }

                is LocationPrayerResult.LocationStale -> {
                    result.prayerData
                }

                is LocationPrayerResult.Success -> {
                    result.prayerData
                }

                else -> {
                    null
                }
            }

        // update the status to failed if couldn't fetch the prayer times
        if (prayerData == null) {
            try {
                prayerAlarmDao.updateAlarmStatus(alarmId, AlarmStatus.FAILED)
                return
            } catch (writeEx: Exception) {
                Log.e(TAG, "schedulePrayerAlarm: ", writeEx)
                return
            }
        }

        // if the alarm doesn't exist, just return
        if (prayerAlarm == null) {
            Log.w(TAG, "schedulePrayerAlarm: Alarm Doesn't exist in the database")
            return
        }

        // get the next alarm millis and update database
        val nextAlarmMillis = calculateTimeFromPrayer(prayerData, prayerAlarm)
        try {
            prayerAlarmDao.updateNextTriggerTime(alarmId, nextAlarmMillis)
        } catch (writeEx: Exception) {
            Log.e(TAG, "schedulePrayerAlarm: ", writeEx)
        }

        // schedule alarm
        val intent = Intent(context, WakeupAlarmReceiver::class.java).apply {
            action = Constants.PRAYER_ALARM_ACTION
            putExtra(Constants.KEY_ALARM_ID, prayerAlarm.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            alarmId + 5000,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

// 3. Create the AlarmClockInfo object
        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            nextAlarmMillis, // The exact epoch millisecond time (e.g. 6:00 PM)
            showPendingIntent
        )

        if (canScheduleExactAlarms) {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextAlarmMillis,
                pendingIntent
            )
        }

        // logging
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")
            .withZone(ZoneId.systemDefault()) // Automatically respects the device's local timezone

        // 2. Format the millisecond timestamp into a human-readable string
        val formattedTime = formatter.format(Instant.ofEpochMilli(nextAlarmMillis))

        // 3. Log the output
        Log.d(TAG, "Alarm with id = $alarmId was scheduled for: $formattedTime")
    }


    private fun calculateTimeFromPrayer(
        prayerEntities: PrayerEntities,
        prayerAlarm: PrayerAlarm
    ): Long {
        val prayerDataLists: List<PrayerData> = prayerEntities.toPrayerDataList()

        // 1. Locate the target prayer data (e.g. Fajr, Asr)
        val targetPrayer =
            prayerDataLists.firstOrNull { it.type == prayerAlarm.prayer } ?: return 0L

        val currentTime = System.currentTimeMillis()
        val offsetMillis = prayerAlarm.offsetMinutes * 60 * 1000L

        // 2. Determine offset sign depending on the relation (BEFORE = subtract, AFTER = add)
        val offsetModifier = when (prayerAlarm.relation) {
            AlarmRelations.BEFORE -> -1
            AlarmRelations.AFTER -> 1
        }
        val appliedOffset = offsetMillis * offsetModifier

        // 3. Calculate candidate alarm times for today and tomorrow
        val todayAlarmTime = targetPrayer.millis + appliedOffset
        val tomorrowAlarmTime = targetPrayer.nextDayMillis + appliedOffset

        // 4. Handle repeating day schedules
        if (prayerAlarm.daysOfWeek.isNotEmpty()) {
            // Map each active day of the week to its next future occurrence
            // and return the minimum (the closest one in time)
            return prayerAlarm.daysOfWeek.minOfOrNull { day ->
                prayerDataUseCase.calculatePrayerMillisForNextDayX(
                    todayAlarmTime,
                    day
                )
            } ?: tomorrowAlarmTime
        } else {
            // 5. Handle one-time alarms
            return if (todayAlarmTime > currentTime) {
                todayAlarmTime
            } else {
                tomorrowAlarmTime
            }
        }
    }
}
