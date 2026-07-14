package com.islamnotify.prayer_times.domain

import android.util.Log
import com.islamnotify.common.domain.CrashReporter
import com.islamnotify.location.domain.LocationRepository
import com.islamnotify.location.domain.model.LocationResult
import com.islamnotify.prayer_times.domain.model.InitialPrayerLocationData
import com.islamnotify.prayer_times.domain.model.NextPrayerData
import com.islamnotify.prayer_times.domain.model.PrayerConfig
import com.islamnotify.prayer_times.domain.model.PrayerData
import com.islamnotify.prayer_times.domain.model.PrayerEntities
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import com.islamnotify.prayer_times.util.PrayerUtils.toPrayersEntities
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

class PrayerDataUseCase @Inject constructor(
    val prayerDataRepository: PrayerDataRepository,
    val locationRepository: LocationRepository,
    val crashReporter: CrashReporter
) {

    suspend fun getPrayerDataWithLastLocation(): LocationPrayerResult {
        val locationResult: LocationResult = locationRepository.getLastKnownLocation()
        return getPrayerDataForToday(locationResult)
    }


    suspend fun getPrayerDataWithCurrentLocation(): LocationPrayerResult {
        val locationResult: LocationResult = locationRepository.getCurrentLocation()
        return getPrayerDataForToday(locationResult)
    }


    private suspend fun getPrayerDataForToday(locationResult: LocationResult): LocationPrayerResult {
        return when (locationResult) {
            is LocationResult.Success -> {
                val prayerResult: PrayerDataResult = prayerDataRepository.getPrayerDataForToday(
                    latitude = locationResult.locationData.latitude,
                    longitude = locationResult.locationData.longitude,
                    countryCode = locationResult.locationData.countryCode
                )

                if (prayerResult is PrayerDataResult.Success) {
                    LocationPrayerResult.Success(
                        prayerData = prayerResult.prayerTimes.toPrayersEntities("getPrayerDataForToday"),
                        locationData = locationResult.locationData
                    )
                } else {
                    // Calc failed AND no cache — the root throwable was already recorded in the repository.
                    crashReporter.log("PrayerDataUseCase: PrayerError (no prayer data and no cache)")
                    LocationPrayerResult.PrayerError
                }
            }

            is LocationResult.Stale -> {
                val prayerResult: PrayerDataResult = prayerDataRepository.getPrayerDataForToday(
                    latitude = locationResult.locationData.latitude,
                    longitude = locationResult.locationData.longitude,
                    countryCode = locationResult.locationData.countryCode
                )

                if (prayerResult is PrayerDataResult.Success) {
                    LocationPrayerResult.LocationStale(
                        prayerData = prayerResult.prayerTimes.toPrayersEntities("getPrayerDataForToday"),
                        locationData = locationResult.locationData,
                        failureCause = locationResult.failureCause
                    )
                } else {
                    crashReporter.log("PrayerDataUseCase: PrayerError with stale location (no prayer data and no cache)")
                    LocationPrayerResult.PrayerError
                }
            }

            is LocationResult.Error -> {
                // PERMISSION_DENIED / GPS_DISABLED are expected user states, not bugs — breadcrumb only.
                // The underlying throwable behind GENERIC_ERROR was already recorded in LocationRepositoryImpl.
                crashReporter.log("PrayerDataUseCase: LocationError (${locationResult.failureCause})")
                LocationPrayerResult.LocationError(locationResult.failureCause)
            }
        }
    }


    suspend fun loadInitialData(): InitialPrayerLocationData {
        val locationResult = locationRepository.getCachedLocation()
        val result = prayerDataRepository.loadInitialData()
        Log.d("InitialPrayerTimes", "loadInitialData: fajr = ${result?.fajr}, sunrise = ${result?.sunrise}, zuhr = ${result?.zuhr}, asr = ${result?.asr}, sunset = ${result?.sunset}, isha = ${result?.isha}, midnight = ${result?.midnight}, last third = ${result?.lastThird} ")

        return InitialPrayerLocationData(
            locationData = locationResult,
            prayerData = result?.toPrayersEntities("loadInitialData")
        )
    }


    suspend fun getNextPrayer(prayers: PrayerEntities): NextPrayerData {
        val prayerDataList: List<PrayerData> = prayers.toPrayerDataList()
        val now = System.currentTimeMillis()

        var nextPrayerType = PrayerTypes.EMPTY
        var nextPrayerTime = String()
        var nextPrayerMillis = Long.MAX_VALUE


        for (prayerData in prayerDataList) {
            listOf(prayerData.millis, prayerData.nextDayMillis, prayerData.previousDayMillis)
                .onEach { millis ->
                    if (millis > now && millis < nextPrayerMillis) {
                        nextPrayerMillis = millis
                        nextPrayerType = prayerData.type
                        nextPrayerTime = prayerData.time
                    }
                }
        }

        return NextPrayerData(
            type = nextPrayerType,
            time = nextPrayerTime,
            millis = nextPrayerMillis
        )
    }


    private suspend fun PrayerEntities.toPrayerDataList(): List<PrayerData> {
        val config = prayerDataRepository.getPrayerConfig(null)
        return buildList {
            addAll(listOf(fajr, zuhr, asr, sunset, isha))

            if (config.showNextIqama) {
                addAll(listOf(iqamaFajr, iqamaZuhr, iqamaAsr, iqamaSunset, iqamaIsha))
            }

            if (config.showNextLastThird) add(lastThird)
            if (config.showNextMidnight) add(midnight)
            if (config.showNextDuha) add(duha)
            if (config.showNextSunrise) add(sunrise)
        }
    }

    suspend fun savePrayerConfig(transform: (PrayerConfig) -> PrayerConfig) {
        val config = getPrayerConfig()
        val updatedConfig = transform(config)
        prayerDataRepository.savePrayerConfig(updatedConfig)
    }

//    suspend fun getPrayerConfig(): PrayerConfig {
//        return when (val locationResult: LocationResult = locationRepository.getCurrentLocation()) {
//            is LocationResult.Success -> prayerDataRepository.getPrayerConfig(locationResult.locationData.countryCode)
//            is LocationResult.Stale -> prayerDataRepository.getPrayerConfig(locationResult.locationData.countryCode)
//            else -> prayerDataRepository.getPrayerConfig(null)
//        }
//    }

//    suspend fun getPrayerConfig(): PrayerConfig {
//        return when (val locationResult = locationRepository.getLastKnownLocation()) {
//            is LocationResult.Success -> prayerDataRepository.getPrayerConfig(locationResult.locationData.countryCode)
//            is LocationResult.Stale -> prayerDataRepository.getPrayerConfig(locationResult.locationData.countryCode)
//            else -> prayerDataRepository.getPrayerConfig(null)
//        }
//    }
//
    suspend fun getPrayerConfig(): PrayerConfig {
        val cache = locationRepository.getCachedLocation()
        val locationResult = if (cache != null){
            LocationResult.Success(cache)
        }else{
            locationRepository.getLastKnownLocation()
        }
        return when (locationResult) {
            is LocationResult.Success -> prayerDataRepository.getPrayerConfig(locationResult.locationData.countryCode)
            is LocationResult.Stale -> prayerDataRepository.getPrayerConfig(locationResult.locationData.countryCode)
            else -> prayerDataRepository.getPrayerConfig(null)
        }
    }

    fun calculatePrayerMillisForNextDayX(todayPrayerMillis: Long, targetDay: DayOfWeek): Long {
        val zoneId = ZoneId.systemDefault()
        val currentTime = System.currentTimeMillis()

        // 1. Convert the baseline prayer millisecond timestamp to a ZonedDateTime
        val todayPrayerZdt = Instant.ofEpochMilli(todayPrayerMillis).atZone(zoneId)
        val currentDay = todayPrayerZdt.dayOfWeek

        // 2. Adjust the date to the correct target day of the week
        val targetZdt = if (targetDay == currentDay) {
            if (todayPrayerMillis <= currentTime) {
                // Already passed today: shift exactly 1 week forward
                todayPrayerZdt.plusWeeks(1)
            } else {
                // Still in the future today: keep today's scheduled time
                todayPrayerZdt
            }
        } else {
            // Adjust to the next chronological occurrence of the target day of the week
            todayPrayerZdt.with(TemporalAdjusters.next(targetDay))
        }

        // 3. Convert back to epoch milliseconds for the AlarmManager
        return targetZdt.toInstant().toEpochMilli()
    }

}