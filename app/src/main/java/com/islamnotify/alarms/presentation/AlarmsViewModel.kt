package com.islamnotify.alarms.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamnotify.alarms.AlarmRelations
import com.islamnotify.alarms.AlarmStatus
import com.islamnotify.alarms.AlarmsRepository
import com.islamnotify.alarms.PrayerAlarm
import com.islamnotify.alarms.PrayerAlarmDao
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val dao: PrayerAlarmDao,
    private val repository: AlarmsRepository
) : ViewModel() {

    val alarmsState: StateFlow<List<PrayerAlarm>> = dao.getAllAlarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var currentEditingAlarm by mutableStateOf<PrayerAlarm?>(null)
        private set

    fun loadAlarm(id: Int) {
        if (id == -1) {
            currentEditingAlarm = PrayerAlarm(
                prayer = PrayerTypes.EMPTY,
                relation = AlarmRelations.BEFORE,
                offsetMinutes = 0,
                status = AlarmStatus.ENABLED, // Changed default from isEnabled = true
                displayName = "",
                daysOfWeek = emptyList(),
                nextTriggerTime = System.currentTimeMillis(),
                ringtoneUri = null,
                snoozeDurationMinutes = 5,
                currentSnoozeCount = 0,
                maxSnoozeAllowed = 3,
                isAutoSnoozingEnabled = true
            )
        } else {
            viewModelScope.launch {
                currentEditingAlarm = dao.getAlarmById(id)
            }
        }
    }

    // Replaced toggleAlarmEnabled with this status-based toggle:
    fun toggleAlarmStatus(alarm: PrayerAlarm) {
        viewModelScope.launch {
            val newStatus = if (alarm.status == AlarmStatus.ENABLED) {
                AlarmStatus.DISABLED
            } else {
                AlarmStatus.ENABLED
            }

            // 1. Update the database state
            dao.insertOrUpdateAlarm(alarm.copy(status = newStatus))

            // 2. If the user enabled it, trigger the schedule
            if (newStatus == AlarmStatus.ENABLED) {
                repository.schedulePrayerAlarm(alarm.id)
            } else {
                repository.cancelPrayerAlarm(alarm.id)
            }
        }
    }

    fun updateEditingAlarmName(newName: String) {
        currentEditingAlarm = currentEditingAlarm?.copy(displayName = newName)
    }

    fun updateEditingAlarmRelation(newRelation: AlarmRelations) {
        currentEditingAlarm = currentEditingAlarm?.copy(relation = newRelation)
    }

    fun updateEditingAlarmPrayer(newPrayer: PrayerTypes) {
        currentEditingAlarm = currentEditingAlarm?.copy(prayer = newPrayer)
    }

    // --- Added setter functions for new configuration fields ---

    fun updateEditingAlarmOffset(offset: Int) {
        currentEditingAlarm = currentEditingAlarm?.copy(offsetMinutes = offset)
    }

    fun toggleEditingAlarmDay(day: DayOfWeek) {
        currentEditingAlarm = currentEditingAlarm?.let { alarm ->
            val currentDays = alarm.daysOfWeek
            val newDays = if (currentDays.contains(day)) {
                currentDays - day
            } else {
                currentDays + day
            }
            // DayOfWeek implements Comparable natively, so .sorted() organizes them Monday -> Sunday
            alarm.copy(daysOfWeek = newDays.sorted())
        }
    }

    fun updateEditingAlarmRingtone(uri: String?) {
        currentEditingAlarm = currentEditingAlarm?.copy(ringtoneUri = uri)
    }

    fun updateEditingAlarmSnoozeDuration(minutes: Int) {
        currentEditingAlarm = currentEditingAlarm?.copy(snoozeDurationMinutes = minutes)
    }

    fun updateEditingAlarmMaxSnooze(allowed: Int) {
        currentEditingAlarm = currentEditingAlarm?.copy(maxSnoozeAllowed = allowed)
    }

    fun updateEditingAlarmAutoSnooze(enabled: Boolean) {
        currentEditingAlarm = currentEditingAlarm?.copy(isAutoSnoozingEnabled = enabled)
    }

    fun saveAlarm() {
        val alarm = currentEditingAlarm ?: return
        viewModelScope.launch {
            // 1. Save to database and capture the generated ID
            val resultId = dao.insertOrUpdateAlarm(alarm)

            val finalAlarmId: Int = if (resultId == -1L) alarm.id else resultId.toInt()


            Log.d("PrayerAlarmScheduler", "trying to save alarm with if: $finalAlarmId")
            // 2. Trigger the scheduling via the Repository
            repository.schedulePrayerAlarm(finalAlarmId)

            // 3. Clear editing state
            currentEditingAlarm = null
        }
    }

    fun deleteAlarm() {
        val alarm = currentEditingAlarm ?: return
        viewModelScope.launch {
            dao.deleteAlarm(alarm)
            currentEditingAlarm = null
        }
    }
}