package com.islamnotify.alarms

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.islamnotify.R
import com.islamnotify.common.AppUtils
import com.islamnotify.common.AppUtils.getLocalizedContext

class AlarmService : Service() {

    private val dismissReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action.equals(ACTION_DISMISS)) {
                //TODO: Action Dismiss
                Toast.makeText(
                    this@AlarmService,
                    "Alarm dismissed", // You can also use: getString(R.string.alarm_dismissed)
                    Toast.LENGTH_SHORT
                ).show() // Don't forget to call .show()!


                cleanUpAndStopService()
            }
       }
    }

    private val snoozeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action.equals(ACTION_SNOOZE)) {
                //TODO: Action Snooze
                Toast.makeText(
                    this@AlarmService,
                    "Alarm snoozed", // You can also use: getString(R.string.alarm_dismissed)
                    Toast.LENGTH_SHORT
                ).show() // Don't forget to call .show()!


                cleanUpAndStopService()
            }
        }
    }

    companion object{
        const val TAG = "PrayerAlarmScheduler"
        const val ACTION_DISMISS = "com.islamnotify.alarms.ACTION_DISMISS"
        const val ACTION_SNOOZE = "com.islamnotify.alarms.ACTION_SNOOZE"
    }


    override fun onCreate() {
        super.onCreate()
        val dismissFilter = IntentFilter(ACTION_DISMISS)
        ContextCompat.registerReceiver(this, dismissReceiver, dismissFilter, ContextCompat.RECEIVER_NOT_EXPORTED)

        val snoozeFilter = IntentFilter(ACTION_SNOOZE)
        ContextCompat.registerReceiver(this, snoozeReceiver, snoozeFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra(Constants.SERVICE_KEY_ALARM_ID,-1)
        if (alarmId == -1 || alarmId == null){
            Log.e(TAG, "onStartCommand: alarm id didn't get received in the service")
            cleanUpAndStopService()
            return START_NOT_STICKY
        }

        //start the service
        showForegroundNotification(alarmId)

        return START_REDELIVER_INTENT
    }


    private fun showForegroundNotification(alarmId: Int) {
        val localizedContext = this.getLocalizedContext()

        // 1. Full-screen PendingIntent targeting AlarmActivity
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(Constants.SERVICE_KEY_ALARM_ID, alarmId)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Action PendingIntents for Notification Buttons
        val dismissIntent = Intent(ACTION_DISMISS).apply {
            setPackage(packageName)
            putExtra(Constants.SERVICE_KEY_ALARM_ID, alarmId)
        }

        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId + 1000, // Unique request code to avoid conflict
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(ACTION_SNOOZE).apply {
            setPackage(packageName)
            putExtra(Constants.SERVICE_KEY_ALARM_ID, alarmId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId + 2000, // Unique request code to avoid conflict
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Build the notification with the full-screen intent and action buttons
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, AppUtils.ALARM_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(localizedContext.getString(R.string.alarm_notification_title))
                .setColor(ContextCompat.getColor(this, R.color.light_green))
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setFullScreenIntent(fullScreenPendingIntent, true) // Required for overlay
                .setOngoing(true) // Keeps the user from swiping the alarm notification away
                .setAutoCancel(false)
                .addAction(R.drawable.notification_icon, "Snooze", snoozePendingIntent) // Replace icon with your own
                .addAction(R.drawable.notification_icon, "Dismiss", dismissPendingIntent) // Replace icon with your own

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    Constants.ALARM_SERVICE_NOTIFICATION_ID,
                    builder.build(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(Constants.ALARM_SERVICE_NOTIFICATION_ID, builder.build())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Service didn't start", e)
            cleanUpAndStopService()
        }
    }


    private fun cleanUpAndStopService() {
        // TODO: general clean up and handle the alarm status
        this.unregisterReceiver(dismissReceiver)
        this.unregisterReceiver(snoozeReceiver)
        stopSelf()
    }


    override fun onBind(p0: Intent?): IBinder? = null
    override fun onDestroy() {
        cleanUpAndStopService()
        super.onDestroy()
    }
    override fun onTimeout(startId: Int) {
        cleanUpAndStopService()
        super.onTimeout(startId)
    }
}