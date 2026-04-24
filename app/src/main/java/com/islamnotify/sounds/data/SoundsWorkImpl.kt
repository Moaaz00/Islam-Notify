package com.islamnotify.sounds.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.islamnotify.android.AlarmReceiver
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import com.islamnotify.sounds.domain.SoundStates
import com.islamnotify.sounds.domain.SoundsConfig
import com.islamnotify.sounds.domain.SoundsWork
import com.islamnotify.sounds.utils.SoundsUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SoundsWorkImpl @Inject constructor(
    @param:ApplicationContext val context: Context,
    val alarmManager: AlarmManager,
    val soundsDataStore: SoundsDataStore
) : SoundsWork {

    companion object {
        const val WORK_REQUEST_TAG = "SOUNDS_ONE_TIME_WORK_REQUEST_TAG"
        const val TAG = "SoundsFlow"
    }

    override fun startScheduling() {
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<SoundsWorker>().build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_REQUEST_TAG,
            ExistingWorkPolicy.REPLACE,
            oneTimeWorkRequest
        )

        Log.d(TAG, "startWork: worker is requested")
    }

    override fun cancel() {
        try {
            val workManager = WorkManager.getInstance(context)

            // 1. Cancel all WorkManager workers (Primary and Midnight Fallback)
            workManager.cancelUniqueWork(WORK_REQUEST_TAG)
            workManager.cancelUniqueWork(SoundsUtils.MIDNIGHT_WORK_REQUEST_TAG)
            Log.d(TAG, "cancel: WorkManager tasks cancelled")

            // 2. Cancel Midnight AlarmManager Request
            val midnightIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = SoundsUtils.ACTION_SOUNDS_MIDNIGHT_REQUEST
            }
            val midnightPendingIntent = PendingIntent.getBroadcast(
                context,
                SoundsUtils.REQUEST_CODE_SOUNDS_MIDNIGHT_REQUEST,
                midnightIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            midnightPendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }

            // 3. Cancel all Prayer Sound Alarms
            val prayerActions = listOf(
                SoundsUtils.ACTION_AZAN_SOUND,
                SoundsUtils.ACTION_IQAMA_SOUND,
                SoundsUtils.ACTION_NOTIFY_SOUND
            )

            PrayerTypes.entries.forEach { prayerType ->
                prayerActions.forEach { actionName ->
                    val intent = Intent(context, AlarmReceiver::class.java).apply {
                        action = actionName
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        prayerType.name.hashCode(),
                        intent,
                        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )

                    pendingIntent?.let {
                        alarmManager.cancel(it)
                        it.cancel()
                    }
                }
            }

            Log.d(TAG, "cancel: All alarms and workers have been cleared")
        } catch (e: Exception) {
            Log.e(TAG, "cancel: Error while cancelling sounds", e)
        }
    }

    override fun getSoundsConfig(): Flow<SoundsConfig> {
        return soundsDataStore.getConfig()
    }

    override suspend fun saveConfig(transform: (SoundsConfig) -> SoundsConfig) {
        val  config: SoundsConfig = getSoundsConfig().first()
        val updatedConfig = transform(config)
        soundsDataStore.saveConfig(updatedConfig)
    }

    override suspend fun toggleSoundState(prayerType: PrayerTypes){
        soundsDataStore.toggleSoundState(prayerType)
        cancel()
        startScheduling()
    }

}