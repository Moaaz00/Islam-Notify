package com.islamnotify.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.islamnotify.events.domain.EventsWork
import com.islamnotify.notification.domain.NotificationWork
import com.islamnotify.sounds.domain.SoundsWork
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimeChangedReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Inject
    lateinit var notificationWork: NotificationWork
    @Inject
    lateinit var eventsWork: EventsWork
    @Inject
    lateinit var soundsWork: SoundsWork

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action.equals(Intent.ACTION_TIME_CHANGED) || intent?.action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    val isEnabled = notificationWork.isEnabled().first()
                    if (isEnabled) {
                        Log.d(
                            "NotificationFlow",
                            "TimeChangedReceiver onReceive: Notification work started"
                        )
                        notificationWork.startWorkInBackground()
                    } else {
                        Log.d(
                            "NotificationFlow",
                            "TimeChangedReceiver onReceive: Notification is disabled"
                        )
                    }

                    eventsWork.startWork()
                    soundsWork.cancel()
                    soundsWork.startScheduling()
                } catch (e: Exception) {
                    Log.e("NotificationFlow", "TimeChangedReceiver onReceive: ", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

}