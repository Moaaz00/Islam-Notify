package com.islamnotify.android

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.islamnotify.common.AppUtils
import com.islamnotify.events.domain.EventsWork
import com.islamnotify.events.util.EventsUtils
import com.islamnotify.notification.domain.NotificationWork
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.islamnotify.R
import com.islamnotify.notification.data.NotificationWorkImpl.Companion.TAG_ONE_TIME_WORK_REQUEST
import com.islamnotify.notification.data.NotificationWorker
import com.islamnotify.sounds.data.SoundsMediaService
import com.islamnotify.sounds.data.SoundsWorker
import com.islamnotify.sounds.domain.SoundsWork
import com.islamnotify.sounds.utils.SoundsUtils
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject
    lateinit var notificationWork: NotificationWork

    @Inject
    lateinit var eventsWork: EventsWork

    @Inject
    lateinit var soundsWork: SoundsWork

    companion object {
        private var lastTriggerTime: Long = 0
        const val TAG_SOUNDS_UNIQUE_WORK_REQUEST = "TAG_SOUNDS_UNIQUE_WORK_REQUEST"
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.action.equals(SoundsUtils.ACTION_SOUNDS_MIDNIGHT_REQUEST) && context != null) {
            val workRequest = OneTimeWorkRequestBuilder<SoundsWorker>().build()
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniqueWork(
                TAG_SOUNDS_UNIQUE_WORK_REQUEST,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            Log.d("SoundsFlow", "onReceive: Midnight Request received")
        }

        if (
            intent?.action.equals(SoundsUtils.ACTION_AZAN_SOUND) ||
            intent?.action.equals(SoundsUtils.ACTION_NOTIFY_SOUND) ||
            intent?.action.equals(SoundsUtils.ACTION_IQAMA_SOUND)
        ) {
            if (context == null) {
                Log.e("SoundsFlow", "onReceive: context is null")
                return
            }


            val pendingResult = goAsync()

            scope.launch {
                val soundsConfig = soundsWork.getSoundsConfig().first()
                if (intent?.action.equals(SoundsUtils.ACTION_AZAN_SOUND) && !soundsConfig.isAzanEnabled) {
                    Log.d(
                        "SoundsFlow",
                        "onReceive: azan is disabled. returning without starting azan"
                    )
                    pendingResult.finish()
                    return@launch
                }

                if (intent?.action.equals(SoundsUtils.ACTION_IQAMA_SOUND) && !soundsConfig.isIqamaEnabled) {
                    Log.d(
                        "SoundsFlow",
                        "onReceive: iqama is disabled. returning without starting iqama"
                    )
                    pendingResult.finish()
                    return@launch
                }

                if (SoundsUtils.shouldStartSoundWork(soundsConfig)) {
                    val serviceIntent = Intent(context, SoundsMediaService::class.java).apply {
                        val prayerType =
                            intent?.getStringExtra(SoundsUtils.EXTRA_PRAYER_TYPE) // prayer name
                        action = intent?.action // azhan/iqama/notify
                        putExtra(SoundsUtils.EXTRA_PRAYER_TYPE, prayerType)
                    }

                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }
                        Log.d("SoundsFlow", "onReceive: Service started successfully")
                    } catch (e: Exception) {
                        Log.e("SoundsFlow", "onReceive: Service failed to start", e)
                    } finally {
                        pendingResult.finish()
                    }
                } else {
                    pendingResult.finish()
                }
            }
        }

        if (intent?.action.equals(AppUtils.NOTIFICATION_ALARM_ACTION)) {
            if (System.currentTimeMillis() - lastTriggerTime < 2_000) {
                Log.d(
                    "NotificationFlow",
                    "AlarmReceiver onReceive: an alarm has been already fired recently"
                )
                return
            }

            val pendingResult = goAsync()
            scope.launch {
                try {
                    val isEnabled = notificationWork.isEnabled().first()
                    if (isEnabled) {
                        Log.d(
                            "NotificationFlow",
                            "AlarmReceiver onReceive: Notification work started"
                        )
                        notificationWork.startWorkInBackground()
                        lastTriggerTime = System.currentTimeMillis()
                    } else {
                        Log.d(
                            "NotificationFlow",
                            "AlarmReceiver onReceive: Notification is disabled"
                        )
                    }
                } catch (e: Exception) {
                    Log.e("NotificationFlow", "AlarmReceiver onReceive: ", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }

        if (intent?.action.equals(AppUtils.NOTIFICATION_MIDNIGHT_ALARM_ACTION)) {
            val pendingResult = goAsync()
            scope.launch {
                val isEnabled = notificationWork.isEnabled().first()
                if (context != null && isEnabled) {
                    notificationWork.startWorkInBackground()
                }
                pendingResult.finish()
            }
        }


        if (intent?.action.equals(EventsUtils.REQUEST_EVENTS_WORKER_ACTION)) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    eventsWork.startWork()
                    Log.d("EventsFlow", "AlarmReceiver onReceive: Request Worker success")
                } catch (e: Exception) {
                    Log.e("EventsFlow", "AlarmReceiver onReceive: Request Worker Failed", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }


        if (intent?.action.equals(EventsUtils.EVENTS_ACTION)) {
            val pendingResult = goAsync()
            try {
                val title = intent?.getStringExtra(EventsUtils.INTENT_TITLE_EXTRA) ?: String()
                val subtitle =
                    intent?.getStringExtra(EventsUtils.INTENT_SUBTITLE_EXTRA) ?: String()
                val notificationId =
                    intent?.getIntExtra(EventsUtils.INTENT_NOTIFICATION_ID_EXTRA, 9999) ?: 9999

                context?.let {
                    val builder =
                        NotificationCompat.Builder(context, AppUtils.EVENTS_NOTIFICATION_CHANNEL_ID)

                    val bigTextStyle = NotificationCompat.BigTextStyle()
                        .setBigContentTitle(title)
                        .bigText(subtitle)

                    builder.setSmallIcon(R.drawable.notification_icon)
                        .setColor(ContextCompat.getColor(context, R.color.light_green))
                        .setStyle(bigTextStyle)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(title)
                        .setContentText(subtitle)

                    val notificationManager = NotificationManagerCompat.from(context)

                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }

                    notificationManager.notify(
                        notificationId,
                        builder.build()
                    )

                    Log.d("EventsFlow", "AlarmReceiver onReceive: notification is sent")
                }

            } catch (e: Exception) {
                Log.e("EventsFlow", "AlarmReceiver onReceive: Notification Failed ", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

