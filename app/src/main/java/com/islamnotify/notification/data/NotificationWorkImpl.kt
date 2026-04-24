package com.islamnotify.notification.data

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.await
import androidx.work.impl.awaitWithin
import com.islamnotify.android.AlarmReceiver
import com.islamnotify.common.AppUtils
import com.islamnotify.notification.domain.NotificationWork
import com.islamnotify.notification.domain.NotificationWorkResult
import com.islamnotify.notification.util.NotificationUtils
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationWorkImpl @Inject constructor(
    @param:ApplicationContext val context: Context,
    val alarmManager: AlarmManager,
    val notificationDataStore: NotificationDataStore,
    val notificationWorkHandler: NotificationWorkHandler
) : NotificationWork {

    companion object {
        const val TAG_ONE_TIME_WORK_REQUEST = "TAG_ONE_TIME_WORK_REQUEST"
        const val TAG = "NotificationFlow"
    }


    override suspend fun startWork(): NotificationWorkResult {
        Log.d(TAG, "Repository's startWork() is starting")
        try {
            notificationDataStore.enableNotification()
            return notificationWorkHandler.doNotificationWork()
        } catch (e: Exception) {
            Log.e(TAG, "Repository's startWork() error: ", e)
            cancelNotification()
            return NotificationWorkResult.Error(null)
        }
    }


    override suspend fun startWorkInBackground() {
        try {
            notificationDataStore.enableNotification()
            val workManager = WorkManager.getInstance(context)
            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            workManager.enqueueUniqueWork(
                TAG_ONE_TIME_WORK_REQUEST,
                ExistingWorkPolicy.REPLACE,
                workRequest
            ).await()

            Log.d(TAG, "Repository's startWorkInBackground(): worker is requested")
        } catch (e: Exception) {
            Log.e(TAG, "Repository's startWorkInBackground() error: ", e)
            cancelNotification()
        }
    }


//    override suspend fun startWork(): NotificationWorkResult {
//        Log.d("NotificationFlow", "Repository's startWork() is starting")
//        try {
//            notificationDataStore.enableNotification()
//            val workManager = WorkManager.getInstance(context)
//            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>().build()
//
//            workManager.enqueueUniqueWork(
//                TAG_ONE_TIME_WORK_REQUEST,
//                ExistingWorkPolicy.REPLACE,
//                workRequest
//            )
//
//            val workInfo = workManager.getWorkInfoByIdFlow(workRequest.id).transform { workInfo ->
//                if (workInfo != null && workInfo.state.isFinished) {
//                    emit(workInfo)
//                }
//            }.first()
//
//            val stateName =
//                workInfo.outputData.getString(NotificationUtils.NOTIFICATION_WORKER_STATE)
//            val failures =
//                workInfo.outputData.getStringArray(NotificationUtils.NOTIFICATION_WORKER_FAILURES)
//            return convertOutputData(stateName, failures)
//
//
//        } catch (e: Exception) {
////            notificationDataStore.disableNotification()
//            Log.e("NotificationFlow", "startWork: ", e)
//            cancelNotification()
//            return NotificationWorkResult.Error(null)
//        }
//    }

//
//    private fun Array<String>?.toFailureCauses(): List<NotificationFailureCauses> {
//        val list = mutableListOf<NotificationFailureCauses>()
//        if (this?.contains(NotificationFailureCauses.NOTIFICATION_PERMISSION_DENIED.name) == true) {
//            list.add(NotificationFailureCauses.NOTIFICATION_PERMISSION_DENIED)
//        }
//        if (this?.contains(NotificationFailureCauses.GPS_DISABLED.name) == true) {
//            list.add(NotificationFailureCauses.GPS_DISABLED)
//        }
//        if (this?.contains(NotificationFailureCauses.BATTERY_PERMISSION_DENIED.name) == true) {
//            list.add(NotificationFailureCauses.BATTERY_PERMISSION_DENIED)
//        }
//        if (this?.contains(NotificationFailureCauses.LOCATION_PERMISSION_DENIED.name) == true) {
//            list.add(NotificationFailureCauses.LOCATION_PERMISSION_DENIED)
//        }
//
//        return list
//    }
//
//
//    private fun convertOutputData(
//        state: String?,
//        failures: Array<String>?
//    ): NotificationWorkResult {
//        return when (state) {
//            WorkerStates.LOCATION_ERROR.name -> NotificationWorkResult.LocationError(failures.toFailureCauses())
//            WorkerStates.PRAYER_ERROR.name -> NotificationWorkResult.PrayerError(failures.toFailureCauses())
//            WorkerStates.FAILED.name -> NotificationWorkResult.Failed(failures.toFailureCauses())
//            WorkerStates.SUCCESS.name -> NotificationWorkResult.Success(failures.toFailureCauses())
//            WorkerStates.GENERIC_ERROR.name -> NotificationWorkResult.Error(failures.toFailureCauses())
//            else -> {
//                NotificationWorkResult.Error(failures.toFailureCauses())
//            }
//        }
//    }
//

    override suspend fun cancel() {
        notificationDataStore.disableNotification()
        cancelNotification()
        cancelPrayerAlarms()
        cancelMidnightAlarm()
        cancelWorkManagerTasks()
    }

    override fun isEnabled(): Flow<Boolean> {
        return notificationDataStore.isNotificationEnabled()
    }


    private fun cancelNotification() {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NotificationUtils.PRAYER_NOTIFICATION_ID)
    }


    private fun cancelPrayerAlarms() {
        PrayerTypes.entries.forEach { prayerType ->

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = AppUtils.NOTIFICATION_ALARM_ACTION
            }

            val requestCode = prayerType.name.hashCode()

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )

            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
    }


    private fun cancelMidnightAlarm() {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AppUtils.NOTIFICATION_MIDNIGHT_ALARM_ACTION
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NotificationUtils.NOTIFICATION_MIDNIGHT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }


    private fun cancelWorkManagerTasks() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(TAG_ONE_TIME_WORK_REQUEST)
        workManager.cancelUniqueWork(NotificationUtils.PRAYERS_WORK_REQUEST_TAG)
        workManager.cancelUniqueWork(NotificationUtils.MIDNIGHT_WORK_REQUEST_TAG)
    }
}