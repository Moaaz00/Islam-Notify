package com.islamnotify.notification.data

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.islamnotify.android.AlarmReceiver
import com.islamnotify.calendar.domain.CalendarRepository
import com.islamnotify.common.AppUtils
import com.islamnotify.common.AppUtils.toPrayerDataList
import com.islamnotify.common.domain.CrashReporter
import com.islamnotify.notification.domain.NotificationFailureCauses
import com.islamnotify.notification.domain.NotificationWorkResult
import com.islamnotify.notification.domain.PrayerNotificationCalculator
import com.islamnotify.notification.util.NotificationUtils
import com.islamnotify.prayer_times.domain.LocationPrayerResult
import com.islamnotify.prayer_times.domain.PrayerDataUseCase
import com.islamnotify.prayer_times.domain.model.PrayerData
import com.islamnotify.prayer_times.domain.model.PrayerEntities
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class NotificationWorkHandler(
    val context: Context,
    val prayerDataUseCase: PrayerDataUseCase,
    val alarmManager: AlarmManager,
    val calendarRepository: CalendarRepository,
    val crashReporter: CrashReporter
) {

    companion object {
        const val TAG = "NotificationFlow"
    }

    private val renderer = PrayerNotificationRenderer(context)


    suspend fun doNotificationWork(): NotificationWorkResult {
        // check for permissions
        val failureCauses = checkForPermissions()

        if (failureCauses.contains(NotificationFailureCauses.NOTIFICATION_PERMISSION_DENIED)) {
            Log.e(TAG, "doNotificationWork: notification permission denied. canceling work")
            crashReporter.log("NotificationWorkHandler: notification permission denied, work cancelled")
            return NotificationWorkResult.Error(failureCauses)
        }

        //fetch prayer data
        return when (val result = prayerDataUseCase.getPrayerDataWithLastLocation()) {
            is LocationPrayerResult.Success -> {
                schedulePrayerAlarms(result.prayerData)
                scheduleMidnightAlarms()
                sendNotification(result.prayerData, result.locationData.locationName.orEmpty())
                NotificationWorkResult.Success(failureCauses)
            }

            is LocationPrayerResult.LocationStale -> {
                schedulePrayerAlarms(result.prayerData)
                scheduleMidnightAlarms()
                sendNotification(result.prayerData, result.locationData.locationName.orEmpty())
                NotificationWorkResult.Success(failureCauses)
            }

            is LocationPrayerResult.LocationError -> {
                NotificationWorkResult.LocationError(failureCauses)
            }

            is LocationPrayerResult.PrayerError -> {
                NotificationWorkResult.PrayerError(failureCauses)
            }

            else -> {
                NotificationWorkResult.Error(failureCauses)
            }
        }
    }


    private suspend fun schedulePrayerAlarms(prayerEntities: PrayerEntities) {
        // schedule fallback work manager for next prayer
        val nextPrayerMillis: Long = prayerDataUseCase.getNextPrayer(prayerEntities).millis
        if (nextPrayerMillis > System.currentTimeMillis() + 2_000) {
            val oneTimeWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(
                    nextPrayerMillis - System.currentTimeMillis() + 10000,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                NotificationUtils.PRAYERS_WORK_REQUEST_TAG,
                ExistingWorkPolicy.REPLACE,
                oneTimeWorkRequest
            )

            Log.d(TAG, "Scheduling Notification Fallback Worker")
        }


        // schedule alarms for all prayers
        val prayerDataList: List<PrayerData> = prayerEntities.toPrayerDataList()
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = AppUtils.NOTIFICATION_ALARM_ACTION

        val canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        prayerDataList.forEach { prayerData ->
            if (prayerData.millis > System.currentTimeMillis() + 2_000) {
                scheduleSingleAlarm(prayerData, intent, canScheduleExactAlarms)
            }
        }
    }


    private fun scheduleSingleAlarm(
        prayerData: PrayerData,
        intent: Intent,
        canScheduleExactAlarms: Boolean
    ) {
        val millis = prayerData.millis

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            prayerData.type.name.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

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

        Log.d(TAG, "Scheduling Notification Alarm for ${prayerData.type.name} at ${prayerData.time}")

    }


    private fun checkForPermissions(): MutableList<NotificationFailureCauses> {
        val permissionList = mutableListOf<NotificationFailureCauses>()

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoringBatteryOptimizations =
            powerManager.isIgnoringBatteryOptimizations(context.packageName)


        if (!isIgnoringBatteryOptimizations) {
            permissionList.add(NotificationFailureCauses.BATTERY_PERMISSION_DENIED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionList.add(NotificationFailureCauses.NOTIFICATION_PERMISSION_DENIED)
        }

        return permissionList
    }


    private fun scheduleMidnightAlarms() {
        val midnightMillis = AppUtils.getMidnightTomorrowPlusSeconds(3)

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = AppUtils.NOTIFICATION_MIDNIGHT_ALARM_ACTION

        val pendingIntent = PendingIntent.getBroadcast(
            context, NotificationUtils.NOTIFICATION_MIDNIGHT_REQUEST_CODE, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )


        // fallback work manager for midnight schedule
        if (midnightMillis > System.currentTimeMillis()) {
            val oneTimeWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(
                    midnightMillis - System.currentTimeMillis() + 10000,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                NotificationUtils.MIDNIGHT_WORK_REQUEST_TAG,
                ExistingWorkPolicy.REPLACE,
                oneTimeWorkRequest
            )
        }


        // schedule an alarm for midnight
        val canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canScheduleExactAlarms) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                midnightMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                midnightMillis,
                pendingIntent
            )
        }
    }


    private suspend fun sendNotification(
        prayerEntities: PrayerEntities,
        locationName: String
    ) {
        val now = ZonedDateTime.now()
        val state = PrayerNotificationCalculator.compute(
            times = PrayerNotificationCalculator.eventTimes(prayerEntities),
            events = prayerDataUseCase.getNextEventTypes(),
            now = now
        )
        if (state == null) {
            Log.e(TAG, "sendNotification: no usable prayer times, notification not updated")
            crashReporter.log("NotificationWorkHandler.sendNotification: no usable prayer times")
            return
        }

        val dateData = calendarRepository.getHijriDate()
        val date = "${dateData.formatedDayOfMonth} ${dateData.monthName} ${dateData.formatedYear}"
        val notification = renderer.build(
            state = state,
            nowMillis = now.toInstant().toEpochMilli(),
            hijriDate = date,
            locationName = locationName
        )

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(
                TAG,
                "UpdateNotification sentNotification failed : permission denied"
            )
            crashReporter.log("NotificationWorkHandler.sendNotification: POST_NOTIFICATIONS not granted, notifying anyway")
        }
        try {
            NotificationManagerCompat.from(context)
                .notify(NotificationUtils.PRAYER_NOTIFICATION_ID, notification)
            Log.d(TAG, "UpdateNotification sentNotification success")
        } catch (e: Exception) {
            // e.g. SecurityException when posting without permission — was previously uncaught.
            Log.e(TAG, "UpdateNotification sentNotification threw", e)
            crashReporter.recordNonFatal(e)
        }

        scheduleProgressUpdate()
    }


    /**
     * Re-sends the notification so the progress bar (and the table's faded rows) move. Uses the
     * cached prayer times and location, so it's cheap. Does nothing if the user dismissed the
     * notification (allowed on Android 14+); the next prayer alarm brings it back.
     */
    suspend fun refreshNotification() {
        if (!isNotificationShowing()) {
            Log.d(TAG, "refreshNotification: notification not showing, skipping")
            return
        }

        val initialData = prayerDataUseCase.loadInitialData()
        val prayerData = initialData.prayerData
        if (prayerData == null) {
            Log.w(TAG, "refreshNotification: no cached prayer times")
            return
        }
        sendNotification(prayerData, initialData.locationData?.locationName.orEmpty())
    }


    private fun isNotificationShowing(): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.activeNotifications
            .any { it.id == NotificationUtils.PRAYER_NOTIFICATION_ID }
    }


    /**
     * About every 10 minutes while the phone is in use. RTC (not RTC_WAKEUP) never wakes the
     * phone: while it sleeps the alarm waits, and runs as soon as the phone wakes up, so the bar
     * is up to date when the user looks.
     */
    private fun scheduleProgressUpdate() {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = AppUtils.NOTIFICATION_PROGRESS_ALARM_ACTION

        val pendingIntent = PendingIntent.getBroadcast(
            context, NotificationUtils.NOTIFICATION_PROGRESS_REQUEST_CODE, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.set(
            AlarmManager.RTC,
            System.currentTimeMillis() + NotificationUtils.PROGRESS_UPDATE_INTERVAL_MILLIS,
            pendingIntent
        )
    }
}