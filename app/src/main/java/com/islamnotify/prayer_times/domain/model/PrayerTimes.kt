package com.islamnotify.prayer_times.domain.model

data class PrayerTimes(
    var fajr: String = String(),
    var iqamaFajr: String = String(),
    var sunrise: String = String(),
    var duha: String = String(),
    var zuhr: String = String(),
    var iqamaZuhr: String = String(),
    var asr: String = String(),
    var iqamaAsr: String = String(),
    var sunset: String = String(),
    var iqamaSunset: String = String(),
    var isha: String = String(),
    var iqamaIsha: String = String(),
    var midnight: String = String(),
    var lastThird: String = String()
)