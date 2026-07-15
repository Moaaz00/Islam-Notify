package com.islamnotify.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.islamnotify.R
import com.islamnotify.common.AppUtils.getLocalizedContext
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registers the app's notification channels. Called from [com.islamnotify.android.Application.onCreate]
 * so channels exist in every process — including background receiver/worker processes that never open
 * the UI. Without this, an expedited worker's foreground notification would post to a not-yet-created
 * channel and crash with "Bad notification for startForeground".
 */
@Singleton
class NotificationChannelInitializer @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val localizedContext = context.getLocalizedContext()
            val prayersName = localizedContext.getString(R.string.prayer_notification_channel_name)
            val prayersDescription =
                localizedContext.getString(R.string.prayer_notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH

            val eventsName = localizedContext.getString(R.string.events_notification_channel_name)
            val eventsDescription =
                localizedContext.getString(R.string.events_notification_channel_description)

            val prayersChannel = NotificationChannel(
                AppUtils.PRAYER_NOTIFICATION_CHANNEL_ID,
                prayersName,
                importance
            )
            prayersChannel.setSound(null, null)
            prayersChannel.description = prayersDescription
            prayersChannel.enableVibration(false)
            prayersChannel.vibrationPattern = null
            prayersChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC

            val eventsChannel =
                NotificationChannel(AppUtils.EVENTS_NOTIFICATION_CHANNEL_ID, eventsName, importance)
            eventsChannel.description = eventsDescription
            eventsChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC

            val mediaName = localizedContext.getString(R.string.sounds_notification_channel_name)
            val mediaDescription =
                localizedContext.getString(R.string.sounds_notification_channel_description)
            val mediaChannel = NotificationChannel(
                AppUtils.SOUNDS_NOTIFICATION_CHANNEL_ID,
                mediaName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = mediaDescription
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setSound(null, null)
                enableVibration(false)
                vibrationPattern = null
            }

            val othersChannel = NotificationChannel(
                AppUtils.OTHERS_NOTIFICATION_CHANNEL_ID,
                localizedContext.getString(R.string.others_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setSound(null, null)
                enableVibration(false)
                vibrationPattern = null
            }

            val alarmChannel = NotificationChannel(
                AppUtils.ALARM_NOTIFICATION_CHANNEL,
                localizedContext.getString(R.string.alarm_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null)
                enableVibration(true)
            }

            // Register the channels with the system
            context.getSystemService(NotificationManager::class.java).apply {
                createNotificationChannels(
                    listOf(
                        prayersChannel,
                        eventsChannel,
                        mediaChannel,
                        othersChannel,
                        alarmChannel
                    )
                )
            }

        }
    }
}
