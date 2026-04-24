package com.islamnotify.location.domain

import com.islamnotify.location.domain.model.LocationData
import com.islamnotify.location.domain.model.LocationResult
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun getLastKnownLocation(): LocationResult // get the last location or cached location
    suspend fun getCurrentLocation(): LocationResult // get the current location, last location, or cached location
    suspend fun getCachedLocation(): LocationData?
}