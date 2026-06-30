package com.islamnotify.notification.data

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.islamnotify.R
import com.islamnotify.android.AlarmReceiver
import com.islamnotify.calendar.domain.CalendarRepository
import com.islamnotify.common.AppUtils
import com.islamnotify.common.AppUtils.formatPrayerTimes
import com.islamnotify.common.AppUtils.getLocalizedContext
import com.islamnotify.common.AppUtils.toPrayerDataList
import com.islamnotify.main.presentation.MainActivity
import com.islamnotify.location.domain.model.LocationData
import com.islamnotify.notification.domain.NotificationFailureCauses
import com.islamnotify.notification.domain.NotificationWorkResult
import com.islamnotify.notification.util.NotificationUtils
import com.islamnotify.prayer_times.domain.LocationPrayerResult
import com.islamnotify.prayer_times.domain.PrayerDataUseCase
import com.islamnotify.prayer_times.domain.model.PrayerData
import com.islamnotify.prayer_times.domain.model.PrayerEntities
import java.util.concurrent.TimeUnit
import kotlin.collections.forEach

class NotificationWorkHandler(
    val context: Context,
    val prayerDataUseCase: PrayerDataUseCase,
    val alarmManager: AlarmManager,
    val calendarRepository: CalendarRepository
) {

    companion object {
        const val TAG = "NotificationFlow"
    }


    suspend fun doNotificationWork(): NotificationWorkResult {
        // check for permissions
        val failureCauses = checkForPermissions()

        if (failureCauses.contains(NotificationFailureCauses.NOTIFICATION_PERMISSION_DENIED)) {
            Log.e(TAG, "doNotificationWork: notification permission denied. canceling work")
            return NotificationWorkResult.Error(failureCauses)
        }

        //fetch prayer data
        return when (val result = prayerDataUseCase.getPrayerDataWithLastLocation()) {
            is LocationPrayerResult.Success -> {
                schedulePrayerAlarms(result.prayerData)
                scheduleMidnightAlarms()
                sendNotification(result.prayerData, result.locationData)
                NotificationWorkResult.Success(failureCauses)
            }

            is LocationPrayerResult.LocationStale -> {
                schedulePrayerAlarms(result.prayerData)
                scheduleMidnightAlarms()
                sendNotification(result.prayerData, result.locationData)
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
        locationData: LocationData
    ) {
        // open the app when the notification is clicked
        val startActivityIntent = Intent(context, MainActivity::class.java)
        startActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val startActivityPendingIntent = PendingIntent.getActivity(
            context, 0, startActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )


        // notification data
        val nextPrayerData = prayerDataUseCase.getNextPrayer(prayerEntities)
        val nextPrayerDiff = nextPrayerData.millis - System.currentTimeMillis()
        val base = SystemClock.elapsedRealtime() + nextPrayerDiff

//        val date = AppUtils.getHijriDate(context, false)
        val dateData = calendarRepository.getHijriDate()
        val date = "${dateData.formatedDayOfMonth} ${dateData.monthName} ${dateData.formatedYear}"
        val locationName = locationData.locationName ?: String()
        val fajrTime = prayerEntities.fajr.time.formatPrayerTimes(context)
        val sunriseTime = prayerEntities.sunrise.time.formatPrayerTimes(context)
        val zuhrTime = prayerEntities.zuhr.time.formatPrayerTimes(context)
        val asrTime = prayerEntities.asr.time.formatPrayerTimes(context)
        val sunsetTime = prayerEntities.sunset.time.formatPrayerTimes(context)
        val ishaTime = prayerEntities.isha.time.formatPrayerTimes(context)

        val zuhrName = if (AppUtils.isTodayFriday()) {
            context.getLocalizedContext().getString(R.string.jummah_name)
        } else {
            context.getLocalizedContext().getString(R.string.zuhr_name)
        }


        // collapsed notification view
        val collapsedRemoteViews =
            RemoteViews(context.packageName, R.layout.collapsed_prayer_notification)

        collapsedRemoteViews.setTextViewText(R.id.Date, date)

        if (locationName.isEmpty()) {
            collapsedRemoteViews.setTextColor(
                R.id.LocationDateDivider,
                context.getColor(R.color.transparent)
            )
        }

        collapsedRemoteViews.setTextViewText(R.id.LocationName, locationName)

        collapsedRemoteViews.setTextViewText(
            R.id.NextPrayerName,
            AppUtils.formatPrayerTypes(context, nextPrayerData)
        )
        collapsedRemoteViews.setChronometerCountDown(R.id.Chronometer, true)
        collapsedRemoteViews.setChronometer(R.id.Chronometer, base, null, true)


        // expanded notification view
        val expandedRemoteViews =
            RemoteViews(context.packageName, R.layout.expanded_prayer_notification)
        expandedRemoteViews.setTextViewText(R.id.Date, date)
        expandedRemoteViews.setTextViewText(R.id.LocationName, locationName)
        expandedRemoteViews.setTextViewText(
            R.id.NextPrayerName,
            AppUtils.formatPrayerTypes(context, nextPrayerData)
        )

        expandedRemoteViews.setChronometerCountDown(R.id.Chronometer, true)
        expandedRemoteViews.setChronometer(R.id.Chronometer, base, null, true)
        expandedRemoteViews.setTextViewText(R.id.Fajr, fajrTime)
        expandedRemoteViews.setTextViewText(R.id.Sunrise, sunriseTime)
        expandedRemoteViews.setTextViewText(R.id.Zuhr, zuhrTime)
        expandedRemoteViews.setTextViewText(R.id.Asr, asrTime)
        expandedRemoteViews.setTextViewText(R.id.Sunset, sunsetTime)
        expandedRemoteViews.setTextViewText(R.id.Isha, ishaTime)
        expandedRemoteViews.setTextViewText(
            R.id.FajrTitle,
            context.getLocalizedContext().getString(R.string.fajr_name)
        )
        expandedRemoteViews.setTextViewText(
            R.id.SunriseTitle,
            context.getLocalizedContext().getString(R.string.sunrise_name)
        )
        expandedRemoteViews.setTextViewText(
            R.id.ZuhrTitle,
            zuhrName
        )
        expandedRemoteViews.setTextViewText(
            R.id.AsrTitle,
            context.getLocalizedContext().getString(R.string.asr_name)
        )
        expandedRemoteViews.setTextViewText(
            R.id.SunsetTitle,
            context.getLocalizedContext().getString(R.string.sunset_name)
        )
        expandedRemoteViews.setTextViewText(
            R.id.IshaTitle,
            context.getLocalizedContext().getString(R.string.isha_name)
        )


        // send notification
        val builder = NotificationCompat.Builder(context, AppUtils.PRAYER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setColor(ContextCompat.getColor(context, R.color.light_green))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(collapsedRemoteViews)
            .setAutoCancel(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setWhen(System.currentTimeMillis())
            .setContentIntent(startActivityPendingIntent)
            .setAllowSystemGeneratedContextualActions(false)
            .setCustomBigContentView(expandedRemoteViews)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        val notificationManager = NotificationManagerCompat.from(context)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(
                TAG,
                "UpdateNotification sentNotification failed : permission denied"
            )
        }
        notificationManager.notify(NotificationUtils.PRAYER_NOTIFICATION_ID, builder.build())
        Log.d(TAG, "UpdateNotification sentNotification success")
    }
}