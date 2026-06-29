package com.islamnotify.alarms

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerAlarmDao {
    @Upsert
    suspend fun insertOrUpdateAlarm(alarm: PrayerAlarm): Long

    @Delete
    suspend fun deleteAlarm(alarm: PrayerAlarm)

    @Query("SELECT * FROM prayer_alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): PrayerAlarm?

    @Query("SELECT * FROM prayer_alarms ORDER BY next_trigger_time ASC")
    fun getAllAlarms(): Flow<List<PrayerAlarm>>

    @Query("SELECT * FROM prayer_alarms WHERE status = 'ENABLED'") // Changed is_enabled = 1 to status = 'ENABLED'
    suspend fun getAllActiveAlarms(): List<PrayerAlarm>

    @Query("UPDATE prayer_alarms SET status = :status WHERE id = :id")
    suspend fun updateAlarmStatus(id: Int, status: AlarmStatus)

    @Query("UPDATE prayer_alarms SET next_trigger_time = :nextTriggerTime WHERE id = :id")
    suspend fun updateNextTriggerTime(id: Int, nextTriggerTime: Long)

    // 2. Update only the current snooze count
    @Query("UPDATE prayer_alarms SET current_snooze_count = :snoozeCount WHERE id = :id")
    suspend fun updateCurrentSnoozeCount(id: Int, snoozeCount: Int)

}