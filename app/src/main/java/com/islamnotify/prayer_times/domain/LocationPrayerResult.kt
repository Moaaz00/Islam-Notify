package com.islamnotify.prayer_times.domain

import com.islamnotify.location.domain.model.LocationData
import com.islamnotify.location.domain.model.LocationFailureCause
import com.islamnotify.prayer_times.domain.model.PrayerEntities

interface LocationPrayerResult {
    data class Success(val prayerData: PrayerEntities, val locationData: LocationData): LocationPrayerResult
    data class LocationStale(val prayerData: PrayerEntities, val locationData: LocationData, val failureCause: LocationFailureCause): LocationPrayerResult
    data class LocationError(val failureCause: LocationFailureCause): LocationPrayerResult
    object PrayerError: LocationPrayerResult
}