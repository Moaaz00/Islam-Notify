package com.islamnotify.prayer_times.data

import android.content.Context
import android.util.Log
import com.batoulapps.adhan2.CalculationMethod
import com.islamnotify.common.domain.CrashReporter
import com.islamnotify.prayer_times.domain.model.PrayerConfig
import com.islamnotify.prayer_times.domain.PrayerDataRepository
import com.islamnotify.prayer_times.domain.PrayerDataResult
import com.islamnotify.prayer_times.domain.model.PrayerTimes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.time.ZoneId
import javax.inject.Inject

class PrayerDataRepositoryImpl @Inject constructor(
    @param:ApplicationContext val context: Context,
    val prayerDataLocal: PrayerDataLocal,
    val prayerDataStore: PrayerDataStore,
    val crashReporter: CrashReporter
) : PrayerDataRepository {

    override suspend fun getPrayerDataForToday(
        latitude: Double,
        longitude: Double,
        countryCode: String?
    ): PrayerDataResult {
        return try {
            val zoneId: ZoneId = ZoneId.systemDefault()
            val prayerConfig: PrayerConfig = prayerDataStore.getPrayerConfig(countryCode).first()
            Log.d("FetchPrayerTimes", "CalculationMethod: " + prayerConfig.method?.name)

            val prayerTimes = prayerDataLocal.fetchPrayerTimes(
                latitude = latitude,
                longitude = longitude,
                zoneId = zoneId,
                prayerConfig = prayerConfig,
                method = prayerConfig.method ?: CalculationMethod.MUSLIM_WORLD_LEAGUE
            )

            prayerDataStore.savePrayerTimes(prayerTimes)
            PrayerDataResult.Success(prayerTimes)
        } catch (e: Exception) {
            // Prayer calculation/IO failed — was silently swallowed. Report before falling back to cache.
            // No location context attached (no country code / coordinates) — privacy.
            Log.e("FetchPrayerTimes", "getPrayerDataForToday failed; falling back to cache", e)
            crashReporter.recordNonFatal(e)
            fetchCachedData()
        }
    }


    override suspend fun getPrayerConfig(countryCode: String?): PrayerConfig {
        countryCode?.let {prayerDataStore.saveAutoCalculationMethod(countryCode)}
        return prayerDataStore.getPrayerConfig(countryCode).first()
    }

    override suspend fun savePrayerConfig(prayerConfig: PrayerConfig) {
        prayerDataStore.savePrayerConfig(prayerConfig)
    }

    override suspend fun loadInitialData(): PrayerTimes? {
        val result = prayerDataStore.getPrayerTimes().firstOrNull()
        if (result != null){
            Log.d("InitialPrayerTimes", "Repository returned non null initial data: fajr = ${result.fajr}, sunrise = ${result.sunrise}, zuhr = ${result.zuhr}, asr = ${result.asr}, sunset = ${result.sunset}, isha = ${result.isha}, midnight = ${result.midnight}, last third = ${result.lastThird}")
        }else{
            Log.w("InitialPrayerTimes", "Repository returned null initial data")
            crashReporter.log("PrayerDataRepositoryImpl.loadInitialData: no cached prayer times")
        }
        return result
    }

    suspend fun fetchCachedData(): PrayerDataResult {
        val cached = prayerDataStore.getPrayerTimes().first()
        return if (cached != null) {
            PrayerDataResult.Success(cached)
        } else {
            PrayerDataResult.Error
        }
    }
}