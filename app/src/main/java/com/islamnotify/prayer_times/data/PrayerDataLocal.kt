package com.islamnotify.prayer_times.data

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.SunnahTimes
import com.batoulapps.adhan2.data.DateComponents
import com.islamnotify.common.domain.CrashReporter
import com.islamnotify.prayer_times.domain.model.PrayerConfig
import com.islamnotify.prayer_times.domain.model.PrayerTimes
import com.islamnotify.prayer_times.util.PrayerUtils.addMinutesToTime
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class PrayerDataLocal(private val crashReporter: CrashReporter) {
    fun fetchPrayerTimes(
        latitude: Double,
        longitude: Double,
        zoneId: ZoneId,
        method: CalculationMethod,
        prayerConfig: PrayerConfig
    ): PrayerTimes {

        val coordinates = Coordinates(latitude = latitude, longitude = longitude)
        val currentDate: LocalDate = LocalDate.now()
        val dateComponents = DateComponents(
            year = currentDate.year,
            month = currentDate.monthValue,
            day = currentDate.dayOfMonth
        )

        val fetchedPrayerTimes =
            com.batoulapps.adhan2.PrayerTimes(coordinates, dateComponents, method.parameters)
        val sunnahTimes = SunnahTimes(fetchedPrayerTimes)
        val prayerTimes = PrayerTimes()

        prayerTimes.fajr = fetchedPrayerTimes.fajr.formatPrayer(zoneId).addMinutesToTime(prayerConfig.fajrOffset)
        prayerTimes.iqamaFajr = prayerTimes.fajr.addMinutesToTime(prayerConfig.iqamaFajrOffset)
        prayerTimes.sunrise = fetchedPrayerTimes.sunrise.formatPrayer(zoneId).addMinutesToTime(prayerConfig.sunriseOffset)
        prayerTimes.duha = prayerTimes.sunrise.addMinutesToTime(prayerConfig.duhaSunriseOffset)
        prayerTimes.zuhr = fetchedPrayerTimes.dhuhr.formatPrayer(zoneId).addMinutesToTime(prayerConfig.zuhrOffset)
        prayerTimes.iqamaZuhr = prayerTimes.zuhr.addMinutesToTime(prayerConfig.iqamaZuhrOffset)
        prayerTimes.asr = fetchedPrayerTimes.asr.formatPrayer(zoneId).addMinutesToTime(prayerConfig.asrOffset)
        prayerTimes.iqamaAsr = prayerTimes.asr.addMinutesToTime(prayerConfig.iqamaAsrOffset)
        prayerTimes.sunset = fetchedPrayerTimes.maghrib.formatPrayer(zoneId).addMinutesToTime(prayerConfig.sunsetOffset)
        prayerTimes.iqamaSunset = prayerTimes.sunset.addMinutesToTime(prayerConfig.iqamaSunsetOffset)
        prayerTimes.isha = fetchedPrayerTimes.isha.formatPrayer(zoneId).addMinutesToTime(prayerConfig.ishaOffset)
        prayerTimes.iqamaIsha = prayerTimes.isha.addMinutesToTime(prayerConfig.iqamaIshaOffset)
        prayerTimes.lastThird = sunnahTimes.lastThirdOfTheNight.formatPrayer(zoneId).addMinutesToTime(prayerConfig.lastThirdOffset)
        prayerTimes.midnight = sunnahTimes.middleOfTheNight.formatPrayer(zoneId).addMinutesToTime(prayerConfig.midnightOffset)

        return prayerTimes
    }

    fun Instant.formatPrayer(zoneId: ZoneId): String {
        try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
            val zonedDateTime = this.toJavaInstant().atZone(zoneId)
            return formatter.format(zonedDateTime)

        } catch (e: Exception) {
            // Formatting a calculated prayer instant failed — was silent; the empty string it returns
            // corrupts downstream prayer times, so this is a real defect worth reporting.
            crashReporter.recordNonFatal(e)
            return String()
        }
    }

}