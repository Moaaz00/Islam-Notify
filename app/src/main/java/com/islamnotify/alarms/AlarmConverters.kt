package com.islamnotify.alarms

import android.util.Log
import androidx.room.TypeConverter
import com.islamnotify.common.domain.CrashReporterProvider
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import java.time.DayOfWeek

class AlarmConverters {

    // --- DayOfWeek Converters ---
    @TypeConverter
    fun fromDayOfWeekList(value: List<DayOfWeek>?): String? {
        if (value == null) return null
        return value.map { it.value }.joinToString(",")
    }

    @TypeConverter
    fun toDayOfWeekList(value: String?): List<DayOfWeek>? {
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            value.split(",").map {
                DayOfWeek.of(it.toInt())
            }
        } catch (e: Exception) {
            Log.e("AlarmConverters", "toDayOfWeekList: ", e)
            CrashReporterProvider.instance?.recordNonFatal(e, "value" to value)
            emptyList()
        }
    }

    // --- 1. PrayerTypes Converters ---
    @TypeConverter
    fun fromPrayerType(value: PrayerTypes?): String? {
        return value?.name // Stores the enum name (e.g., "FAJR") as TEXT in SQLite
    }

    @TypeConverter
    fun toPrayerType(value: String?): PrayerTypes? {
        if (value.isNullOrEmpty()) return null
        return try {
            // Case-insensitive lookup. This safely matches if your old DB stored
            // "Fajr" or "fajr" but your new Enum is uppercase "FAJR".
            PrayerTypes.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
        } catch (e: Exception) {
            Log.e("AlarmConverters", "toPrayerType failed for value: $value", e)
            CrashReporterProvider.instance?.recordNonFatal(e, "value" to value)
            null
        }
    }

    // --- 2. AlarmRelations Converters ---
    @TypeConverter
    fun fromAlarmRelation(value: AlarmRelations?): String? {
        return value?.name // Stores the enum name (e.g., "BEFORE") as TEXT in SQLite
    }

    @TypeConverter
    fun toAlarmRelation(value: String?): AlarmRelations? {
        if (value.isNullOrEmpty()) return null
        return try {
            // Case-insensitive lookup to match old database strings smoothly
            AlarmRelations.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
        } catch (e: Exception) {
            Log.e("AlarmConverters", "toAlarmRelation failed for value: $value", e)
            CrashReporterProvider.instance?.recordNonFatal(e, "value" to value)
            null
        }
    }

    @TypeConverter
    fun fromAlarmStatus(value: AlarmStatus?): String? {
        return value?.name // e.g. AlarmStatus.ENABLED -> "ENABLED"
    }

    @TypeConverter
    fun toAlarmStatus(value: String?): AlarmStatus? {
        if (value.isNullOrEmpty()) return null
        return try {
            AlarmStatus.valueOf(value)
        } catch (e: Exception) {
            Log.e("AlarmConverters", "toAlarmStatus failed for value: $value", e)
            CrashReporterProvider.instance?.recordNonFatal(e, "value" to value)
            null
        }
    }
}