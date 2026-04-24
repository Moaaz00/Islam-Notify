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
