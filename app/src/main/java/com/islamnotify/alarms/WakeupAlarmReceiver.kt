package com.islamnotify.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.islamnotify.events.util.EventsUtils
import com.islamnotify.sounds.data.SoundsMediaService
import com.islamnotify.sounds.utils.SoundsUtils

class WakeupAlarmReceiver() : BroadcastReceiver() {

    companion object{
        const val TAG = "PrayerAlarmScheduler"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action.equals(Constants.PRAYER_ALARM_ACTION)){
            val alarmId = intent?.getIntExtra(Constants.KEY_ALARM_ID, -1)
            Log.d(TAG, "alarm id sent from work manager to receiver: $alarmId")

            if (context == null){
                Log.e(TAG, "onReceive: failed, context is null")
                return
            }

            if (alarmId == -1){
                Log.e(TAG, "onReceive: failed, alarm id is -1")
                return
            }

            val pendingResult = goAsync()

            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                action = Constants.PRAYER_ALARM_ACTION
                putExtra(Constants.SERVICE_KEY_ALARM_ID, alarmId)
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                Log.d(TAG, "onReceive: Service started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "onReceive: Service failed to start", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}