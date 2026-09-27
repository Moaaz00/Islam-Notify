package com.islamnotify.prayer_times.domain.model

import com.batoulapps.adhan2.CalculationMethod

data class PrayerConfig(
    var fajrOffset: Int = 0,
    var iqamaFajrOffset: Int = 25,
    var sunriseOffset: Int = 0,
    var duhaSunriseOffset: Int = 20,
    var zuhrOffset: Int = 0,
    var iqamaZuhrOffset: Int = 20,
    var asrOffset: Int = 0,
    var iqamaAsrOffset: Int = 20,
    var sunsetOffset: Int = 0,
    var iqamaSunsetOffset: Int = 15,
    var ishaOffset: Int = 0,
    var iqamaIshaOffset: Int = 20,
    var midnightOffset: Int = 0,
    var lastThirdOffset: Int = 0,

    // Currently Active Calculation Method. Equals autoCalculationMethod if auto is enabled, and equals manualCalculationMethod otherwise
    var method: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
    // which calculation method to use if auto calculation is enabled
    var autoCalculationMethod: CalculationMethod? = null,
    // which calculation method to use if auto calculation is disabled
    var manualCalculationMethod: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,

    var showNextLastThird: Boolean = true,
    var showNextMidnight: Boolean = true,
    var showNextDuha: Boolean = true,
    var showNextSunrise: Boolean = true,
    var showNextIqama: Boolean = true,
    var isAutoCalculationMethodEnabled: Boolean = true
 )

/**
 * The events counted down to ("next prayer"), filtered by the user's settings. The order breaks
 * ties between events at the same minute, so every "next event" calculation must use this list.
 */
fun PrayerConfig.nextEventTypes(): List<PrayerTypes> = buildList {
    addAll(listOf(PrayerTypes.FAJR, PrayerTypes.ZUHR, PrayerTypes.ASR, PrayerTypes.SUNSET, PrayerTypes.ISHA))

    if (showNextIqama) {
        addAll(
            listOf(
                PrayerTypes.IQAMA_FAJR,
                PrayerTypes.IQAMA_ZUHR,
                PrayerTypes.IQAMA_ASR,
                PrayerTypes.IQAMA_SUNSET,
                PrayerTypes.IQAMA_ISHA
            )
        )
    }

    if (showNextLastThird) add(PrayerTypes.LAST_THIRD)
    if (showNextMidnight) add(PrayerTypes.MIDNIGHT)
    if (showNextDuha) add(PrayerTypes.DUHA)
    if (showNextSunrise) add(PrayerTypes.SUNRISE)
}
