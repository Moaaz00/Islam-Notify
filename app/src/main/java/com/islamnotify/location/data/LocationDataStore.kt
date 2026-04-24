package com.islamnotify.location.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.islamnotify.location.di.LocationDataModules
import com.islamnotify.location.domain.model.LocationData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocationDataStore @Inject constructor(
    @param:LocationDataModules.LocationPrefs private val dataStore: DataStore<Preferences>
) {

    private object Keys {
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val NAME = stringPreferencesKey("location_name")
        val LAST_COUNTRY_CODE = stringPreferencesKey("last_country_code")
        val TIMESTAMP = longPreferencesKey("location_timestamp")
    }

    suspend fun saveLocation(locationData: LocationData) {
        dataStore.edit { prefs ->
            prefs[Keys.LONGITUDE] = locationData.longitude
            prefs[Keys.LATITUDE] = locationData.latitude
            if (locationData.locationName != null) {
                prefs[Keys.NAME] = locationData.locationName
            }
            if (locationData.countryCode != null) {
                prefs[Keys.LAST_COUNTRY_CODE] = locationData.countryCode
            }
            prefs[Keys.TIMESTAMP] = locationData.timestamp
        }
    }

    fun getLocation(): Flow<LocationData?> {
        return dataStore.data.map { prefs ->
            val latitude = prefs[Keys.LATITUDE]
            val longitude = prefs[Keys.LONGITUDE]
            val countryCode = prefs[Keys.LAST_COUNTRY_CODE]
            val name = prefs[Keys.NAME]
            val timestamp = prefs[Keys.TIMESTAMP]

            if (latitude == null || longitude == null || timestamp == null) {
                return@map null
            }


            LocationData(
                latitude = latitude,
                longitude = longitude,
                locationName = name,
                countryCode = countryCode,
                timestamp = timestamp
            )
        }
    }

    fun getCountryCode(): Flow<String?>{
        return dataStore.data.map { prefs ->
            Log.d("LocationRepository", "getCountryCode: country code = ${prefs[Keys.LAST_COUNTRY_CODE]}")
            prefs[Keys.LAST_COUNTRY_CODE]
        }
    }

}