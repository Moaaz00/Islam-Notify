package com.islamnotify.alarms

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PrayerAlarm::class], version = 2, exportSchema = false)
@TypeConverters(AlarmConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prayerAlarmDao(): PrayerAlarmDao
}
