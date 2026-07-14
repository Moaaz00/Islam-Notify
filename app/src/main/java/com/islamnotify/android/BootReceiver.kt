package com.islamnotify.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.islamnotify.common.domain.CrashReporter
import com.islamnotify.events.domain.EventsWork
import com.islamnotify.notification.domain.NotificationWork
import com.islamnotify.sounds.domain.SoundsWork
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var notificationWork: NotificationWork
    @Inject lateinit var eventsWork: EventsWork
    @Inject lateinit var soundsWork: SoundsWork
    @Inject lateinit var crashReporter: CrashReporter

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action.equals(Intent.ACTION_BOOT_COMPLETED) || intent?.action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)){
            val pendingResult = goAsync()
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                try {
                    val isEnabled = notificationWork.isEnabled().first()
                    if (isEnabled) {
                        notificationWork.startWorkInBackground()
                        Log.d("NotificationFlow", "BootReceiver onReceive: Notification work started")
                    } else{
                        Log.d("NotificationFlow", "BootReceiver onReceive: Notification is disabled")
                    }

                    eventsWork.startWork()
                    soundsWork.cancel()
                    soundsWork.startScheduling()
                } catch (e: Exception) {
                    Log.e("NotificationFlow", "BootReceiver onReceive: ",e)
                    crashReporter.recordNonFatal(e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

}