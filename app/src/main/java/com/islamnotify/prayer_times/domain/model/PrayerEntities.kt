package com.islamnotify.prayer_times.domain.model

data class PrayerEntities(
    var fajr: PrayerData,
    var iqamaFajr: PrayerData,
    var sunrise: PrayerData,
    var duha: PrayerData,
    var zuhr: PrayerData,
    var iqamaZuhr: PrayerData,
    var asr: PrayerData,
    var iqamaAsr: PrayerData,
    var sunset: PrayerData,
    var iqamaSunset: PrayerData,
    var isha: PrayerData,
    var iqamaIsha: PrayerData,
    var midnight: PrayerData,
    var lastThird: PrayerData
)