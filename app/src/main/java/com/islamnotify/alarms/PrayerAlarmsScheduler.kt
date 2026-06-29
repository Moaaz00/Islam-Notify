package com.islamnotify.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.islamnotify.android.AlarmReceiver
import com.islamnotify.common.AppUtils
import com.islamnotify.common.AppUtils.toPrayerDataList
import com.islamnotify.notification.data.NotificationWorker
import com.islamnotify.notification.util.NotificationUtils
import com.islamnotify.prayer_times.domain.LocationPrayerResult
import com.islamnotify.prayer_times.domain.PrayerDataUseCase
import com.islamnotify.prayer_times.domain.model.PrayerData
import com.islamnotify.prayer_times.domain.model.PrayerEntities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import java.time.Instant
import java.time.ZoneId
import java.time.DayOfWeek
import java.util.concurrent.TimeUnit

class PrayerAlarmsScheduler @Inject constructor(
    @param:ApplicationContext val context: Context,
    val alarmManager: AlarmManager
): AlarmsRepository {
    override suspend fun schedulePrayerAlarm(alarmId: Int) {
        val inputData = workDataOf(Constants.WORKER_KEY_ALARM_ID to alarmId)

        Log.d("PrayerAlarmScheduler", "alarm id sent from ui to Scheduler: $alarmId")
        // 2. Build the WorkRequest
        val workRequest = OneTimeWorkRequestBuilder<AlarmSchedulerWorker>()
            .setInputData(inputData)
            .build()

        // 3. Enqueue it
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    override fun cancelPrayerAlarm(alarmId: Int) {
        // 1. Recreate the identical Intent used during scheduling
        val intent = Intent(context, WakeupAlarmReceiver::class.java).apply {
            action = Constants.PRAYER_ALARM_ACTION
        }

        // 2. Recreate the identical PendingIntent matching the request code (alarmId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId, // Request code must match the alarmId
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Cancel the trigger inside AlarmManager
        alarmManager.cancel(pendingIntent)

        // 4. Cancel the PendingIntent itself to clean up system resources
        pendingIntent.cancel()

        Log.d("PrayerAlarmScheduler", "Alarm with id = $alarmId was cancelled")
    }

}