package com.islamnotify.calendar.domain

interface CalendarRepository {
    suspend fun getHijriDate(): DateModel
    suspend fun saveConfig(transform: (DateConfig) -> DateConfig)
    suspend fun getConfig(): DateConfig
}