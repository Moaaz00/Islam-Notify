package com.islamnotify.prayer_times.domain.model

data class PrayerData(
    var type: PrayerTypes = PrayerTypes.EMPTY,
    var name: String = String(),
    var time: String = String(),
    var nextDayMillis: Long = 0L,
    var millis: Long = 0L,
    var previousDayMillis: Long = 0L
)
