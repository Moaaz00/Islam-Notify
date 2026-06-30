package com.islamnotify.intro.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.islamnotify.intro.di.IntroModule
import com.islamnotify.intro.domain.IntroPreferencesRepository
import com.islamnotify.intro.domain.RequestedPermissions
import com.islamnotify.main.domain.PermissionDialogs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IntroDataStore @Inject constructor(
    @param:IntroModule.IntroPrefs private val dataStore: DataStore<Preferences>
) : IntroPreferencesRepository {

    object Keys {
        val LOCATION_REQUESTED = booleanPreferencesKey("LOCATION_REQUESTED")
        val NOTIFICATION_REQUESTED = booleanPreferencesKey("NOTIFICATION_REQUESTED")
    }

    override fun getRequestedFlags(): Flow<RequestedPermissions> {
        return dataStore.data.map { preferences ->
            RequestedPermissions(
                location = preferences[Keys.LOCATION_REQUESTED] ?: false,
                notification = preferences[Keys.NOTIFICATION_REQUESTED] ?: false
            )
        }
    }

    override suspend fun setRequested(permission: PermissionDialogs) {
        val key = when (permission) {
            PermissionDialogs.LOCATION -> Keys.LOCATION_REQUESTED
            PermissionDialogs.NOTIFICATION -> Keys.NOTIFICATION_REQUESTED
            // Battery never goes through the runtime-permission flow, so nothing to persist.
            PermissionDialogs.BATTERY -> return
        }
        dataStore.edit { preferences -> preferences[key] = true }
    }
}
