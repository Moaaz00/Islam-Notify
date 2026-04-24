package com.islamnotify.prayer_times.domain.model

data class NextPrayerData(
    var type: PrayerTypes = PrayerTypes.EMPTY,
    var name: String = String(),
    var time: String = String(),
    var millis: Long = 0L
)

