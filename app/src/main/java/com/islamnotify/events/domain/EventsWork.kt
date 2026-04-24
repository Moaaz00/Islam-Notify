package com.islamnotify.events.domain

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface EventsWork {
    fun startWork()
    suspend fun toggleFlag(key: Preferences.Key<Boolean>, enable: Boolean)
    suspend fun getEventFlags(): Flow<EventFlags>
}