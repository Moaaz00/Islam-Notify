package com.islamnotify.notification.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.islamnotify.notification.di.NotificationModules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationDataStore @Inject constructor(@param:NotificationModules.NotificationPrefs val dataStore: DataStore<Preferences>) {
    object Keys{
        val NOTIFICATION_ENABLED = booleanPreferencesKey("IS_NOTIFICATION_ENABLED")
    }

    fun isNotificationEnabled(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[Keys.NOTIFICATION_ENABLED] ?: true // notification default value
        }
    }


    suspend fun enableNotification(){
        dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATION_ENABLED] = true
        }
    }


    suspend fun disableNotification(){
        dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATION_ENABLED] = false
        }
    }

}


