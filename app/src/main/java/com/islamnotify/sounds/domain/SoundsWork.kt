package com.islamnotify.sounds.domain

import com.islamnotify.prayer_times.domain.model.PrayerTypes
import kotlinx.coroutines.flow.Flow

interface SoundsWork {
    fun startScheduling()
    fun cancel()
    fun getSoundsConfig(): Flow<SoundsConfig>
    suspend fun saveConfig(transform: (SoundsConfig) -> SoundsConfig)
    suspend fun toggleSoundState(prayerType: PrayerTypes)
}