package com.islamnotify.alarms

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import java.time.DayOfWeek

@Entity(tableName = "prayer_alarms")
data class PrayerAlarm(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val prayer: PrayerTypes,
    val relation: AlarmRelations,

    @ColumnInfo(name = "offset_minutes")
    val offsetMinutes: Int,

    @ColumnInfo(name = "status")
    val status: AlarmStatus,

    @ColumnInfo(name = "display_name")
    val displayName: String,

    @ColumnInfo(name = "days_of_week")
    val daysOfWeek: List<DayOfWeek>, // Mapped by TypeConverter

    @ColumnInfo(name = "next_trigger_time")
    val nextTriggerTime: Long,

    @ColumnInfo(name = "ringtone_uri")
    val ringtoneUri: String?, // Nullable in case no custom ringtone is chosen

    @ColumnInfo(name = "snooze_duration_minutes")
    val snoozeDurationMinutes: Int,

    @ColumnInfo(name = "alarm_duration_minutes")
    val alarmDurationMinutes: Int = 5,

    @ColumnInfo(name = "current_snooze_count")
    val currentSnoozeCount: Int,

    @ColumnInfo(name = "max_snooze_allowed")
    val maxSnoozeAllowed: Int,

    @ColumnInfo(name = "is_auto_snoozing_enabled")
    val isAutoSnoozingEnabled: Boolean
)