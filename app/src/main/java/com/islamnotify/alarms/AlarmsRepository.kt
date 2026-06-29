package com.islamnotify.alarms

interface AlarmsRepository {
    suspend fun schedulePrayerAlarm(alarmId: Int)
    fun cancelPrayerAlarm(alarmId: Int)
}