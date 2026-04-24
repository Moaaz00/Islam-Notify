package com.islamnotify.notification.data

import android.app.Service
import android.content.Context
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.islamnotify.R
import com.islamnotify.common.AppUtils
import com.islamnotify.common.AppUtils.getLocalizedContext
import com.islamnotify.notification.domain.NotificationWorkResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    val notificationWorkHandler: NotificationWorkHandler
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "NotificationFlow"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Worker's doWork() starting")
        try {
            when (val result = notificationWorkHandler.doNotificationWork()) {
                is NotificationWorkResult.Error, is NotificationWorkResult.PrayerError, is NotificationWorkResult.LocationError -> {
                    Log.e(TAG, "Worker's doWork: failed $result")
                    return Result.failure()
                }

                else -> {
                    Log.d(TAG, "Worker's doWork: success")
                    return Result.success()
                }

            }
        } catch (e: Exception) {
            Log.e(TAG, "Worker's doWork error", e)
            return Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification =
            NotificationCompat.Builder(context, AppUtils.OTHERS_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setColor(ContextCompat.getColor(context, R.color.light_green))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentTitle(
                    context.getLocalizedContext().getString(R.string.notification_update_string)
                ).build()

        return ForegroundInfo(
            2222,
            notification
        )
    }
}

//package com.islamnotify.notification.data
//
//import android.Manifest
//import android.app.AlarmManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.PowerManager
//import android.os.SystemClock
//import android.util.Log
//import android.widget.RemoteViews
//import androidx.core.app.ActivityCompat
//import androidx.core.app.NotificationCompat
//import androidx.core.app.NotificationManagerCompat
//import androidx.core.content.ContextCompat
//import androidx.hilt.work.HiltWorker
//import androidx.work.CoroutineWorker
//import androidx.work.Data
//import androidx.work.ExistingWorkPolicy
//import androidx.work.OneTimeWorkRequestBuilder
//import androidx.work.WorkManager
//import androidx.work.WorkerParameters
//import com.islamnotify.R
//import com.islamnotify.android.AlarmReceiver
//import com.islamnotify.common.AppUtils
//import com.islamnotify.common.AppUtils.formatPrayerTimes
//import com.islamnotify.common.AppUtils.getLocalizedContext
//import com.islamnotify.common.AppUtils.toPrayerDataList
//import com.islamnotify.display_prayers.presentation.MainActivity
//import com.islamnotify.location.domain.model.LocationData
//import com.islamnotify.location.domain.model.LocationFailureCause
//import com.islamnotify.notification.domain.NotificationFailureCauses
//import com.islamnotify.notification.domain.WorkerStates
//import com.islamnotify.notification.util.NotificationUtils
//import com.islamnotify.prayer_data.domain.LocationPrayerResult
//import com.islamnotify.prayer_data.domain.PrayerDataUseCase
//import com.islamnotify.prayer_data.domain.model.PrayerData
//import com.islamnotify.prayer_data.domain.model.PrayerEntities
//import dagger.assisted.Assisted
//import dagger.assisted.AssistedInject
//import java.util.concurrent.TimeUnit
//
//
//@HiltWorker
//class NotificationWorker @AssistedInject constructor(
//    @Assisted val context: Context,
//    @Assisted val workerParams: WorkerParameters,
//    val prayerDataUseCase: PrayerDataUseCase,
//    val alarmManager: AlarmManager
//) : CoroutineWorker(context, workerParams) {
//
//    companion object {
//        const val TAG = "NotificationFlow"
//    }
//
//    override suspend fun doWork(): Result = runCatching {
//        Log.d("NotificationFlow", "Worker's doWork() starting")
//
//        // check for permission
//        val failureList = checkForPermissions()
//        if (failureList.contains(NotificationFailureCauses.NOTIFICATION_PERMISSION_DENIED.name)) {
//            return@runCatching buildWorkerResult(
//                workerState = WorkerStates.FAILED,
//                failureArray = failureList.toTypedArray(),
//                isSuccess = false
//            )
//        }
//
//        val (workerState: WorkerStates, isSuccess: Boolean) = when (val result =
//            prayerDataUseCase.getPrayerDataWithLastLocation()) {
//            is LocationPrayerResult.Success -> handlePrayerResult(
//                result.prayerData,
//                result.locationData,
//                failureList
//            )
//
//            is LocationPrayerResult.LocationStale -> {
//                if (result.failureCause == LocationFailureCause.PERMISSION_DENIED) {
//                    failureList.add(NotificationFailureCauses.LOCATION_PERMISSION_DENIED.name)
//                } else if (result.failureCause == LocationFailureCause.GPS_DISABLED) {
//                    failureList.add(NotificationFailureCauses.GPS_DISABLED.name)
//                }
//
//                handlePrayerResult(result.prayerData, result.locationData, failureList)
//            }
//
//            is LocationPrayerResult.PrayerError -> WorkerStates.PRAYER_ERROR to false
//            is LocationPrayerResult.LocationError -> {
//                if (result.failureCause == LocationFailureCause.PERMISSION_DENIED) {
//                    failureList.add(NotificationFailureCauses.LOCATION_PERMISSION_DENIED.name)
//                    WorkerStates.FAILED to false
//                } else if (result.failureCause == LocationFailureCause.GPS_DISABLED) {
//                    failureList.add(NotificationFailureCauses.GPS_DISABLED.name)
//                    WorkerStates.FAILED to false
//                } else {
//                    WorkerStates.LOCATION_ERROR to false
//                }
//            }
//
//            else -> WorkerStates.GENERIC_ERROR to false
//        }
//
//        Log.d(TAG, "doWork: success")
//
//        buildWorkerResult(workerState, isSuccess, failureList.toTypedArray())
//    }.getOrElse { e ->
//        Log.e(TAG, "Error in NotificationWorker", e)
//        buildWorkerResult(
//            workerState = WorkerStates.GENERIC_ERROR,
//            failureArray = checkForPermissions().toTypedArray(),
//            isSuccess = false
//        )
//    }
//
//
//    private fun handlePrayerResult(
//        prayerEntities: PrayerEntities,
//        locationData: LocationData,
//        failureCauses: MutableList<String>
//    ): Pair<WorkerStates, Boolean> {
//        val notificationSent = handlePrayerSuccess(prayerEntities, locationData)
//        return if (notificationSent) {
//            WorkerStates.SUCCESS to true
//        } else {
//            failureCauses.add(NotificationFailureCauses.NOTIFICATION_PERMISSION_DENIED.name)
//            WorkerStates.FAILED to false
//        }
//    }
//
//
//    private fun buildWorkerResult(
//        workerState: WorkerStates,
//        isSuccess: Boolean,
//        failureArray: Array<String?>
//    ): Result {
//        val outputData = Data.Builder()
//            .putString(NotificationUtils.NOTIFICATION_WORKER_STATE, workerState.name)
//            .putStringArray(NotificationUtils.NOTIFICATION_WORKER_FAILURES, failureArray)
//            .build()
//
//        return if (isSuccess) Result.success(outputData) else Result.failure(outputData)
//    }
//
//
//    private fun handlePrayerSuccess(
//        prayerEntities: PrayerEntities,
//        locationData: LocationData
//    ): Boolean {
//        schedulePrayerAlarms(prayerEntities)
//        scheduleMidnightAlarms()
//        return sendNotification(prayerEntities, locationData)
//    }
//
//
//    private fun schedulePrayerAlarms(prayerEntities: PrayerEntities) {
//        // schedule fallback work manager for next prayer
//        val nextPrayerMillis: Long = prayerDataUseCase.getNextPrayer(prayerEntities).millis
//        if (nextPrayerMillis > System.currentTimeMillis() + 2_000) {
//            val oneTimeWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
//                .setInitialDelay(
//                    nextPrayerMillis - System.currentTimeMillis() + 10000,
//                    TimeUnit.MILLISECONDS
//                )
//                .build()
//
//            WorkManager.getInstance(context).enqueueUniqueWork(
//                NotificationUtils.PRAYERS_WORK_REQUEST_TAG,
//                ExistingWorkPolicy.REPLACE,
//                oneTimeWorkRequest
//            )
//
//            Log.d(TAG,"Scheduling Notification Fallback Worker")
//        }
//
//        // schedule alarms for all prayers
//        val prayerDataList: List<PrayerData> = prayerEntities.toPrayerDataList()
//        val intent = Intent(context, AlarmReceiver::class.java)
//        intent.action = AppUtils.NOTIFICATION_ALARM_ACTION
//
//        val canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            alarmManager.canScheduleExactAlarms()
//        } else {
//            true
//        }
//
//        prayerDataList.forEach { prayerData ->
//            if (prayerData.millis > System.currentTimeMillis() + 2_000) {
//                scheduleSingleAlarm(prayerData, intent, canScheduleExactAlarms)
//            }
//        }
//    }
//
//
//    private fun scheduleSingleAlarm(
//        prayerData: PrayerData,
//        intent: Intent,
//        canScheduleExactAlarms: Boolean
//    ) {
//        val millis = prayerData.millis
//
//        val pendingIntent = PendingIntent.getBroadcast(
//            context,
//            prayerData.type.name.hashCode(),
//            intent,
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//        if (canScheduleExactAlarms) {
//            alarmManager.setExactAndAllowWhileIdle(
//                AlarmManager.RTC_WAKEUP,
//                millis,
//                pendingIntent
//            )
//        } else {
//
//            alarmManager.setAndAllowWhileIdle(
//                AlarmManager.RTC_WAKEUP,
//                millis,
//                pendingIntent
//            )
//        }
//
//        Log.d(TAG,"Scheduling Notification Alarm")
//
//    }
//
//
//    private fun checkForPermissions(): MutableList<String> {
//        val permissionList = mutableListOf<String>()
//
//        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
//        val isIgnoringBatteryOptimizations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            powerManager.isIgnoringBatteryOptimizations(context.packageName)
//        } else {
//            true
//        }
//
//        if (!isIgnoringBatteryOptimizations) {
//            permissionList.add(NotificationFailureCauses.BATTERY_PERMISSION_DENIED.name)
//        }
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            permissionList.add(NotificationFailureCauses.NOTIFICATION_PERMISSION_DENIED.name)
//        }
//
//        return permissionList
//    }
//
//
//    private fun scheduleMidnightAlarms() {
//        val midnightMillis = AppUtils.getMidnightTomorrowPlusSeconds(3)
//
//        val intent = Intent(context, AlarmReceiver::class.java)
//        intent.action = AppUtils.NOTIFICATION_MIDNIGHT_ALARM_ACTION
//
//        val pendingIntent = PendingIntent.getBroadcast(
//            context, NotificationUtils.NOTIFICATION_MIDNIGHT_REQUEST_CODE, intent,
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//
//        // fallback work manager for midnight schedule
//        if (midnightMillis > System.currentTimeMillis()) {
//            val oneTimeWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
//                .setInitialDelay(
//                    midnightMillis - System.currentTimeMillis() + 10000,
//                    TimeUnit.MILLISECONDS
//                )
//                .build()
//
//            WorkManager.getInstance(context).enqueueUniqueWork(
//                NotificationUtils.MIDNIGHT_WORK_REQUEST_TAG,
//                ExistingWorkPolicy.REPLACE,
//                oneTimeWorkRequest
//            )
//        }
//
//
//        // schedule an alarm for midnight
//        val canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            alarmManager.canScheduleExactAlarms()
//        } else {
//            true
//        }
//
//        if (canScheduleExactAlarms) {
//            alarmManager.setExactAndAllowWhileIdle(
//                AlarmManager.RTC_WAKEUP,
//                midnightMillis,
//                pendingIntent
//            )
//        } else {
//            alarmManager.setAndAllowWhileIdle(
//                AlarmManager.RTC_WAKEUP,
//                midnightMillis,
//                pendingIntent
//            )
//        }
//    }
//
//
//    private fun sendNotification(
//        prayerEntities: PrayerEntities,
//        locationData: LocationData
//    ): Boolean {
//        // open the app when the notification is clicked
//        val startActivityIntent = Intent(context, MainActivity::class.java)
//        startActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        val startActivityPendingIntent = PendingIntent.getActivity(
//            context, 0, startActivityIntent,
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//
//        // notification data
//        val nextPrayerData = prayerDataUseCase.getNextPrayer(prayerEntities)
//        val nextPrayerDiff = nextPrayerData.millis - System.currentTimeMillis()
//        val base = SystemClock.elapsedRealtime() + nextPrayerDiff
//
//        val date = AppUtils.getHijriDate(context, false)
//        val locationName = locationData.locationName ?: String()
//        val fajrTime = prayerEntities.fajr.time.formatPrayerTimes(context)
//        val sunriseTime = prayerEntities.sunrise.time.formatPrayerTimes(context)
//        val zuhrTime = prayerEntities.zuhr.time.formatPrayerTimes(context)
//        val asrTime = prayerEntities.asr.time.formatPrayerTimes(context)
//        val sunsetTime = prayerEntities.sunset.time.formatPrayerTimes(context)
//        val ishaTime = prayerEntities.isha.time.formatPrayerTimes(context)
//
//        val zuhrName = if (AppUtils.isTodayFriday()) {
//            context.getLocalizedContext().getString(R.string.jummah_name)
//        }else{
//            context.getLocalizedContext().getString(R.string.zuhr_name)
//        }
//
//
//        // collapsed notification view
//        val collapsedRemoteViews =
//            RemoteViews(context.packageName, R.layout.collapsed_prayer_notification)
//
//        collapsedRemoteViews.setTextViewText(R.id.Date, date)
//
//        if (locationName.isEmpty()) {
//            collapsedRemoteViews.setTextColor(
//                R.id.LocationDateDivider,
//                context.getColor(R.color.transparent)
//            )
//        }
//
//        collapsedRemoteViews.setTextViewText(R.id.LocationName, locationName)
//
//        collapsedRemoteViews.setTextViewText(
//            R.id.NextPrayerName,
//            AppUtils.formatPrayerTypes(context, nextPrayerData)
//        )
//        collapsedRemoteViews.setChronometerCountDown(R.id.Chronometer, true)
//        collapsedRemoteViews.setChronometer(R.id.Chronometer, base, null, true)
//
//
//        // expanded notification view
//        val expandedRemoteViews =
//            RemoteViews(context.packageName, R.layout.expanded_prayer_notification)
//        expandedRemoteViews.setTextViewText(R.id.Date, date)
//        expandedRemoteViews.setTextViewText(R.id.LocationName, locationName)
//        expandedRemoteViews.setTextViewText(
//            R.id.NextPrayerName,
//            AppUtils.formatPrayerTypes(context, nextPrayerData)
//        )
//
//        expandedRemoteViews.setChronometerCountDown(R.id.Chronometer, true)
//        expandedRemoteViews.setChronometer(R.id.Chronometer, base, null, true)
//        expandedRemoteViews.setTextViewText(R.id.Fajr, fajrTime)
//        expandedRemoteViews.setTextViewText(R.id.Sunrise, sunriseTime)
//        expandedRemoteViews.setTextViewText(R.id.Zuhr, zuhrTime)
//        expandedRemoteViews.setTextViewText(R.id.Asr, asrTime)
//        expandedRemoteViews.setTextViewText(R.id.Sunset, sunsetTime)
//        expandedRemoteViews.setTextViewText(R.id.Isha, ishaTime)
//        expandedRemoteViews.setTextViewText(
//            R.id.FajrTitle,
//            context.getLocalizedContext().getString(R.string.fajr_name)
//        )
//        expandedRemoteViews.setTextViewText(
//            R.id.SunriseTitle,
//            context.getLocalizedContext().getString(R.string.sunrise_name)
//        )
//        expandedRemoteViews.setTextViewText(
//            R.id.ZuhrTitle,
//            zuhrName
//        )
//        expandedRemoteViews.setTextViewText(
//            R.id.AsrTitle,
//            context.getLocalizedContext().getString(R.string.asr_name)
//        )
//        expandedRemoteViews.setTextViewText(
//            R.id.SunsetTitle,
//            context.getLocalizedContext().getString(R.string.sunset_name)
//        )
//        expandedRemoteViews.setTextViewText(
//            R.id.IshaTitle,
//            context.getLocalizedContext().getString(R.string.isha_name)
//        )
//
//
//        // send notification
//        val builder = NotificationCompat.Builder(context, AppUtils.PRAYER_NOTIFICATION_CHANNEL_ID)
//            .setSmallIcon(R.drawable.notification_icon)
//            .setColor(ContextCompat.getColor(context, R.color.light_green))
//            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
//            .setCustomContentView(collapsedRemoteViews)
//            .setAutoCancel(false)
//            .setCategory(NotificationCompat.CATEGORY_SERVICE)
//            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
//            .setWhen(System.currentTimeMillis())
//            .setContentIntent(startActivityPendingIntent)
//            .setAllowSystemGeneratedContextualActions(false)
//            .setCustomBigContentView(expandedRemoteViews)
//            .setPriority(NotificationCompat.PRIORITY_MAX)
//            .setOngoing(true)
//            .setOnlyAlertOnce(true)
//
//        val notificationManager = NotificationManagerCompat.from(context)
//
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            Log.e(
//                TAG,
//                "UpdateNotification sentNotification failed : permission denied"
//            )
//            return false
//        }
//        notificationManager.notify(NotificationUtils.PRAYER_NOTIFICATION_ID, builder.build())
//        Log.d(TAG, "UpdateNotification sentNotification success")
//        return true
//    }
//}