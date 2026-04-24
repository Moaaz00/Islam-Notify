package com.islamnotify.events.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.islamnotify.android.AlarmReceiver
import com.islamnotify.events.domain.EventFlags
import com.islamnotify.events.domain.EventsPreferenceKeys
import com.islamnotify.events.domain.EventsWork
import com.islamnotify.events.util.EventsUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class EventsWorkImpl @Inject constructor(
    @param:ApplicationContext val context: Context,
    val dataStore: EventsDataStore,
    val alarmManager: AlarmManager
) : EventsWork {
    override fun startWork() {
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<EventsWorker>().build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            EventsUtils.ONE_TIME_WORK_REQUEST_TAG,
            ExistingWorkPolicy.REPLACE,
            oneTimeWorkRequest
        )

        Log.d("EventsFlow", "startWork: worker is requested")
    }


    override suspend fun toggleFlag(
        key: Preferences.Key<Boolean>,
        enable: Boolean
    ) {
        dataStore.toggleEvent(
            key = key,
            enable = enable
        )

        // enable/disable the master switch
        if (key == EventsPreferenceKeys.IS_ALL_ENABLED) {
            if (enable) {
                startWork()
            } else {
                cancelAllSchedules()
            }
            return
        }

        // check for master switch
        val flags = getEventFlags().first()
        if (!flags.isAllEnabled) {
            Log.w("EventsFlow", "toggleEvent: master switch is disabled")
            return
        }

        // individual event
        if (enable) {
            startWork()
        } else {
            if (EventsUtils.areAnySubEventsEnabled(flags)) {
                cancelAllSchedules()
                startWork()
            } else {
                cancelAllSchedules()
            }
        }
    }

    override suspend fun getEventFlags(): Flow<EventFlags> {
        return dataStore.getEventsFlags()
    }


    private fun cancelAllSchedules() {
        // 1. Cancel WorkManager Midnight fallback check
        WorkManager.getInstance(context)
            .cancelUniqueWork(EventsUtils.MIDNIGHT_WORK_REQUEST_TAG)

        // 2. List of all possible event request codes
        val allEventRequestCodes = listOf(
            EventsUtils.MONDAY_REQUEST_CODE_NOTIFICATION_ID,
            EventsUtils.THURSDAY_REQUEST_CODE_NOTIFICATION_ID,
            EventsUtils.WHITE_DAYS_REQUEST_CODE_NOTIFICATION_ID,
            EventsUtils.ARAFAH_REQUEST_CODE_NOTIFICATION_ID,
            EventsUtils.TASUA_REQUEST_CODE_NOTIFICATION_ID,
            EventsUtils.ASHORA_REQUEST_CODE_NOTIFICATION_ID,
            EventsUtils.SHAWWAL_REQUEST_CODE_NOTIFICATION_ID,
            EventsUtils.RAMADAN_REQUEST_CODE_NOTIFICATION_ID,
            EventsUtils.RAMADAN_LAST_10_DAYS_REQUEST_CODE_NOTIFICATION_ID,
            EventsUtils.DHU_AL_HIJJA_FIRST_10_DAYS_REQUEST_CODE_NOTIFICATION_ID,
            EventsUtils.EID_AL_FITR_REQUEST_CODE_NOTIFICATION_ID,
            EventsUtils.EID_AL_ADHA_REQUEST_CODE_NOTIFICATION_ID,
            EventsUtils.FRIDAY_REQUEST_CODE_NOTIFICATION_ID,
            EventsUtils.MUHARRAM_REQUEST_CODE_NOTIFICATION_ID,
            EventsUtils.RAJAB_REQUEST_CODE_NOTIFICATION_ID,
            EventsUtils.DHU_AL_QIDA_REQUEST_CODE_NOTIFICATION_ID,
            EventsUtils.DHU_AL_HIJJA_REQUEST_CODE_NOTIFICATION_ID
        )

        // 3. Cancel all individual event alarms
        allEventRequestCodes.forEach { code ->
            cancelAlarm(code, EventsUtils.EVENTS_ACTION)
        }

        // 4. Cancel the Midnight check alarm
        cancelAlarm(
            EventsUtils.REQUEST_EVENTS_WORKER_REQUEST_CODE,
            EventsUtils.REQUEST_EVENTS_WORKER_ACTION
        )

    }

    private fun cancelAlarm(code: Int, action: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = action

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            code,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
}