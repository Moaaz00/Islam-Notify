package com.islamnotify.sounds.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.islamnotify.android.AlarmReceiver
import com.islamnotify.common.AppUtils
import com.islamnotify.common.domain.CrashReporter
import com.islamnotify.prayer_times.domain.LocationPrayerResult
import com.islamnotify.prayer_times.domain.PrayerDataUseCase
import com.islamnotify.prayer_times.domain.model.PrayerEntities
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import com.islamnotify.sounds.domain.SoundStates
import com.islamnotify.sounds.domain.SoundsConfig
import com.islamnotify.sounds.utils.SoundsUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.islamnotify.sounds.utils.SoundsUtils.toSoundDataList
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class SoundsWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    val prayerDataUseCase: PrayerDataUseCase,
    val soundsDataStore: SoundsDataStore,
    val alarmManager: AlarmManager,
    val crashReporter: CrashReporter
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "SoundsFlow"
    }

    override suspend fun doWork(): Result {
        try {
            scheduleMidnightRequest()
            scheduleSoundsForToday()
            Log.d(TAG, "doWork: success")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "doWork: failed", e)
            crashReporter.recordNonFatal(e)
            return Result.failure()
        }
    }


    private suspend fun scheduleSoundsForToday() {
        val prayerData: PrayerEntities? =
            when (val result = prayerDataUseCase.getPrayerDataWithLastLocation()) {
                is LocationPrayerResult.LocationStale -> result.prayerData
                is LocationPrayerResult.Success -> result.prayerData
                else -> null
            }

        if (prayerData == null) {
            Log.e(TAG, "scheduleSoundsForToday: Failed to get prayer times")
            crashReporter.log("SoundsWorker.scheduleSoundsForToday: no prayer data, sounds not scheduled")
            return
        }

        val soundsConfig: SoundsConfig = soundsDataStore.getConfig().first()
        val prayers = prayerData.toSoundDataList(soundsConfig)

        prayers.forEach { soundData ->
            when (soundData.soundState) {
                SoundStates.AZAN -> {
                    if (soundsConfig.isAzanEnabled) {
                        sendSoundAlarm(
                            SoundsUtils.ACTION_AZAN_SOUND,
                            soundData.millis,
                            soundData.prayerType
                        )
                    } else {
                        Log.d(
                            TAG,
                            "scheduleSoundsForToday: azan is disabled. return without scheduling for azan"
                        )
                    }
                }

                SoundStates.IQAMA -> {
                    if (soundsConfig.isIqamaEnabled) {
                        sendSoundAlarm(
                            SoundsUtils.ACTION_IQAMA_SOUND,
                            soundData.millis,
                            soundData.prayerType
                        )
                    } else {
                        Log.d(
                            TAG,
                            "scheduleSoundsForToday: iqama is disabled. return without scheduling for iqama"
                        )
                    }
                }

                SoundStates.NOTIFY -> {
                    sendSoundAlarm(
                        SoundsUtils.ACTION_NOTIFY_SOUND,
                        soundData.millis,
                        soundData.prayerType
                    )
                }

                SoundStates.MUTE -> {
                    Log.d(
                        TAG,
                        "scheduleSoundsForToday: the prayer (${soundData.prayerType.name}) is muted"
                    )
                }
            }
        }
    }


    private fun scheduleMidnightRequest() {
        val triggerTime = AppUtils.getMidnightTomorrowPlusSeconds(3)

        if (triggerTime <= System.currentTimeMillis()) {
            Log.e(TAG, "scheduleMidnightRequest: midnight schedule failed")
            crashReporter.log("SoundsWorker.scheduleMidnightRequest: computed trigger time in the past")
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = SoundsUtils.ACTION_SOUNDS_MIDNIGHT_REQUEST

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SoundsUtils.REQUEST_CODE_SOUNDS_MIDNIGHT_REQUEST,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canScheduleExactAlarms) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }


        // fallback work manager for midnight schedule
        if (triggerTime > System.currentTimeMillis()) {
            val oneTimeWorkRequest = OneTimeWorkRequestBuilder<SoundsWorker>()
                .setInitialDelay(
                    triggerTime - System.currentTimeMillis() + 10_000,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                SoundsUtils.MIDNIGHT_WORK_REQUEST_TAG,
                ExistingWorkPolicy.REPLACE,
                oneTimeWorkRequest
            )
        }

        Log.d(TAG, "scheduleMidnightRequest: success")

    }


    private fun sendSoundAlarm(
        action: String,
        millis: Long,
        prayerType: PrayerTypes
    ) {

        if (millis <= System.currentTimeMillis()) {
            Log.d(TAG, "sendSoundAlarm: the time for ${prayerType.name} has passed")
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            setAction(action)
            putExtra(SoundsUtils.EXTRA_PRAYER_TYPE, prayerType.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            prayerType.name.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canScheduleExactAlarms) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                millis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                millis,
                pendingIntent
            )
        }

        Log.d(TAG, "sendSoundAlarm: scheduled an alarm for ${prayerType.name}")
    }
}

data class PrayersSoundData(
    var prayerType: PrayerTypes,
    var millis: Long,
    var soundState: SoundStates
)
