package com.islamnotify.main.presentation

import com.islamnotify.location.domain.model.LocationData
import com.islamnotify.location.domain.model.LocationFailureCause
import com.islamnotify.prayer_times.domain.model.PrayerEntities

sealed interface LocationPrayerState {
    object Loading: LocationPrayerState
    data class Success(val prayerData: PrayerEntities, val locationData: LocationData): LocationPrayerState
    data class Initial(val prayerData: PrayerEntities, val locationData: LocationData?): LocationPrayerState
    data class LocationStale(val prayerData: PrayerEntities, val locationData: LocationData, val failureCause: LocationFailureCause): LocationPrayerState
    data class LocationError(val failureCause: LocationFailureCause): LocationPrayerState
    object PrayerError: LocationPrayerState
}