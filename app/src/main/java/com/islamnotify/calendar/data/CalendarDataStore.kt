package com.islamnotify.calendar.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.islamnotify.calendar.di.CalendarModules
import com.islamnotify.calendar.domain.DateConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CalendarDataStore @Inject constructor(
    @param:CalendarModules.CalendarPrefs val dataStore: DataStore<Preferences>
) {
    object Keys {
        val HIJRI_OFFSET = intPreferencesKey("hijri_offset")
    }

    suspend fun saveConfig(config: DateConfig){
        dataStore.edit { preferences ->
            preferences[Keys.HIJRI_OFFSET] = config.hijriOffset
        }
    }

    fun getConfig(): Flow<DateConfig> {
        return dataStore.data.map { preferences ->
            DateConfig(
                hijriOffset = preferences[Keys.HIJRI_OFFSET] ?: 0
            )
        }
    }
}