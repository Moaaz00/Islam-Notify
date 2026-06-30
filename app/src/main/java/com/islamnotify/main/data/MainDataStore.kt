package com.islamnotify.main.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.islamnotify.main.di.MainModule
import com.islamnotify.main.domain.MainPreferencesConfig
import com.islamnotify.main.domain.MainPreferencesRepository
import com.islamnotify.notification.di.NotificationModules
import com.islamnotify.ui.theme.AppThemeTypes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MainDataStore @Inject constructor(@param:MainModule.MainPrefs val dataStore: DataStore<Preferences>): MainPreferencesRepository {

    object Keys{
        val THEME = stringPreferencesKey("THEME")
        val SHOW_INTRO = booleanPreferencesKey("SHOW_INTRO")
        val DONT_ASK_NOTIFICATION = booleanPreferencesKey("DONT_ASK_NOTIFICATION")
        val DONT_ASK_BATTERY = booleanPreferencesKey("DONT_ASK_BATTERY")
    }

    override suspend fun saveConfig(transform: (MainPreferencesConfig) -> MainPreferencesConfig) {
        val config = getConfig().first()
        val newConfig = transform(config)
        dataStore.edit { preferences ->
            preferences[Keys.THEME] = newConfig.theme.name
            preferences[Keys.SHOW_INTRO] = newConfig.showIntro
            preferences[Keys.DONT_ASK_NOTIFICATION] = newConfig.dontAskNotification
            preferences[Keys.DONT_ASK_BATTERY] = newConfig.dontAskBattery
        }
    }

    override fun getConfig(): Flow<MainPreferencesConfig> {
        return dataStore.data.map { preferences ->
            MainPreferencesConfig(
                theme = AppThemeTypes.valueOf(preferences[Keys.THEME] ?: AppThemeTypes.GREEN_LIGHT.name),
                showIntro = preferences[Keys.SHOW_INTRO] ?: true,
                dontAskNotification = preferences[Keys.DONT_ASK_NOTIFICATION] ?: false,
                dontAskBattery = preferences[Keys.DONT_ASK_BATTERY] ?: false
            )
        }
    }
}