package com.islamnotify.prayer_times.domain.model

import com.islamnotify.location.domain.model.LocationData

data class InitialPrayerLocationData(
    var locationData: LocationData?,
    var prayerData: PrayerEntities?
)