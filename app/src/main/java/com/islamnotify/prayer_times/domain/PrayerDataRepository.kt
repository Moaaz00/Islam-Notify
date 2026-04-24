package com.islamnotify.prayer_times.domain

import com.islamnotify.prayer_times.domain.model.PrayerConfig
import com.islamnotify.prayer_times.domain.model.PrayerTimes

interface PrayerDataRepository {
    suspend fun getPrayerDataForToday(
        latitude: Double,
        longitude: Double,
        countryCode: String?
    ): PrayerDataResult
    suspend fun getPrayerConfig(countryCode: String?): PrayerConfig
    suspend fun savePrayerConfig(prayerConfig: PrayerConfig)
    suspend fun loadInitialData(): PrayerTimes?
}