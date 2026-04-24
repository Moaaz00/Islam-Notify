package com.islamnotify.main.domain

import kotlinx.coroutines.flow.Flow

interface MainPreferencesRepository {
    suspend fun saveConfig(transform: (MainPreferencesConfig) -> MainPreferencesConfig)
    fun getConfig(): Flow<MainPreferencesConfig>
}