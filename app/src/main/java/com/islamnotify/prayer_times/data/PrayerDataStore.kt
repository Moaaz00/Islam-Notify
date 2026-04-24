package com.islamnotify.prayer_times.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.batoulapps.adhan2.CalculationMethod
import com.islamnotify.prayer_times.di.PrayerDataModules
import com.islamnotify.prayer_times.domain.model.PrayerConfig
import com.islamnotify.prayer_times.domain.model.PrayerTimes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PrayerDataStore @Inject constructor(@param:PrayerDataModules.PrayerPrefs val dataStore: DataStore<Preferences>) {

    private object Keys {
        val FAJR = stringPreferencesKey("fajr")
        val IQAMA_FAJR = stringPreferencesKey("iqama_fajr")
        val SUNRISE = stringPreferencesKey("sunrise")
        val DUHA = stringPreferencesKey("duha")
        val ZUHR = stringPreferencesKey("zuhr")
        val IQAMA_ZUHR = stringPreferencesKey("iqama_zuhr")
        val ASR = stringPreferencesKey("asr")
        val IQAMA_ASR = stringPreferencesKey("iqama_asr")
        val SUNSET = stringPreferencesKey("sunset")
        val IQAMA_SUNSET = stringPreferencesKey("iqama_sunset")
        val ISHA = stringPreferencesKey("isha")
        val IQAMA_ISHA = stringPreferencesKey("iqama_isha")
        val MIDNIGHT = stringPreferencesKey("midnight")
        val LAST_THIRD = stringPreferencesKey("last_third")

        val FAJR_OFFSET = intPreferencesKey("fajr_offset_in_minutes")
        val IQAMA_FAJR_OFFSET = intPreferencesKey("iqama_fajr_offset_in_minutes")
        val SUNRISE_OFFSET = intPreferencesKey("sunrise_offset_in_minutes")
        val DUHA_SUNRISE_OFFSET = intPreferencesKey("duha_sunrise_offset_in_minutes")
        val ZUHR_OFFSET = intPreferencesKey("zuhr_offset_in_minutes")
        val IQAMA_ZUHR_OFFSET = intPreferencesKey("iqama_zuhr_offset_in_minutes")
        val ASR_OFFSET = intPreferencesKey("asr_offset_in_minutes")
        val IQAMA_ASR_OFFSET = intPreferencesKey("iqama_asr_offset_in_minutes")
        val SUNSET_OFFSET = intPreferencesKey("sunset_offset_in_minutes")
        val IQAMA_SUNSET_OFFSET = intPreferencesKey("iqama_sunset_offset_in_minutes")
        val ISHA_OFFSET = intPreferencesKey("isha_offset_in_minutes")
        val IQAMA_ISHA_OFFSET = intPreferencesKey("iqama_isha_offset_in_minutes")
        val MIDNIGHT_OFFSET = intPreferencesKey("midnight_offset_in_minutes")
        val LAST_THIRD_OFFSET = intPreferencesKey("last_third_offset_in_minutes")
//        val CALCULATION_METHOD_NAME = stringPreferencesKey("calculation_method_name")

        val SHOW_NEXT_LAST_THIRD = booleanPreferencesKey("show_next_last_third")
        val SHOW_NEXT_MIDNIGHT = booleanPreferencesKey("show_next_midnight")
        val SHOW_NEXT_DUHA = booleanPreferencesKey("show_next_duha")
        val SHOW_NEXT_SUNRISE = booleanPreferencesKey("show_next_sunrise")
        val SHOW_NEXT_IQAMA = booleanPreferencesKey("show_next_iqama")
        val AUTO_CALCULATION_METHOD = stringPreferencesKey("auto_calculation_method")
        val MANUAL_CALCULATION_METHOD = stringPreferencesKey("manual_calculation_method")
        val IS_AUTO_CALCULATION_METHOD_ENABLED =
            booleanPreferencesKey("is_auto_calculation_method_enabled")
    }


//    suspend fun enableAutomaticCalculationMethod(){
//        dataStore.edit { prefs ->
//            if (prefs.contains(Keys.CALCULATION_METHOD_NAME)){
//                prefs.remove(Keys.CALCULATION_METHOD_NAME)
//            }
//        }
//    }
//
//    fun isAutoCalculationMethodEnabled(): Flow<Boolean>{
//        return dataStore.data.map { prefs ->
//            !prefs.contains(Keys.CALCULATION_METHOD_NAME)
//        }
//    }

    suspend fun savePrayerConfig(config: PrayerConfig) {
        dataStore.edit { prefs ->
            prefs[Keys.FAJR_OFFSET] = config.fajrOffset
            prefs[Keys.IQAMA_FAJR_OFFSET] = config.iqamaFajrOffset
            prefs[Keys.SUNRISE_OFFSET] = config.sunriseOffset
            prefs[Keys.DUHA_SUNRISE_OFFSET] = config.duhaSunriseOffset
            prefs[Keys.ZUHR_OFFSET] = config.zuhrOffset
            prefs[Keys.IQAMA_ZUHR_OFFSET] = config.iqamaZuhrOffset
            prefs[Keys.ASR_OFFSET] = config.asrOffset
            prefs[Keys.IQAMA_ASR_OFFSET] = config.iqamaAsrOffset
            prefs[Keys.SUNSET_OFFSET] = config.sunsetOffset
            prefs[Keys.IQAMA_SUNSET_OFFSET] = config.iqamaSunsetOffset
            prefs[Keys.ISHA_OFFSET] = config.ishaOffset
            prefs[Keys.IQAMA_ISHA_OFFSET] = config.iqamaIshaOffset
            prefs[Keys.MIDNIGHT_OFFSET] = config.midnightOffset
            prefs[Keys.LAST_THIRD_OFFSET] = config.lastThirdOffset
            prefs[Keys.SHOW_NEXT_LAST_THIRD] = config.showNextLastThird
            prefs[Keys.SHOW_NEXT_MIDNIGHT] = config.showNextMidnight
            prefs[Keys.SHOW_NEXT_DUHA] = config.showNextDuha
            prefs[Keys.SHOW_NEXT_SUNRISE] = config.showNextSunrise
            prefs[Keys.SHOW_NEXT_IQAMA] = config.showNextIqama
            prefs[Keys.IS_AUTO_CALCULATION_METHOD_ENABLED] = config.isAutoCalculationMethodEnabled
            prefs[Keys.MANUAL_CALCULATION_METHOD] = config.manualCalculationMethod.name
//            config.method?.let { method ->
//                prefs[Keys.CALCULATION_METHOD_NAME] = method.name
//            }
        }
    }

//    fun getAutoCalculationMethod(): Flow<CalculationMethod?>{
//        return dataStore.data.map { prefs ->
//            val method = prefs[Keys.AUTO_CALCULATION_METHOD]
//            if (method != null) {
//                CalculationMethod.valueOf(method)
//            }
//            else{
//                null
//            }
//        }
//    }

    suspend fun saveAutoCalculationMethod(countryCode: String?) {
        dataStore.edit { prefs ->
            prefs[Keys.AUTO_CALCULATION_METHOD] = getMethodFromCountryCode(countryCode).name
        }
    }

    fun getPrayerConfig(countryCode: String?): Flow<PrayerConfig> {
        Log.d("CalculationMethod", "getPrayerConfig: countryCode = $countryCode")
        return dataStore.data.map { prefs ->
            val defaults = PrayerConfig()
            val fajrOffset = prefs[Keys.FAJR_OFFSET]
            val iqamaFajrOffset = prefs[Keys.IQAMA_FAJR_OFFSET]
            val sunriseOffset = prefs[Keys.SUNRISE_OFFSET]
            val duhaSunriseOffset = prefs[Keys.DUHA_SUNRISE_OFFSET]
            val zuhrOffset = prefs[Keys.ZUHR_OFFSET]
            val iqamaZuhrOffset = prefs[Keys.IQAMA_ZUHR_OFFSET]
            val asrOffset = prefs[Keys.ASR_OFFSET]
            val iqamaAsrOffset = prefs[Keys.IQAMA_ASR_OFFSET]
            val sunsetOffset = prefs[Keys.SUNSET_OFFSET]
            val iqamaSunsetOffset = prefs[Keys.IQAMA_SUNSET_OFFSET]
            val ishaOffset = prefs[Keys.ISHA_OFFSET]
            val iqamaIshaOffset = prefs[Keys.IQAMA_ISHA_OFFSET]
            val midnightOffset = prefs[Keys.MIDNIGHT_OFFSET]
            val lastThirdOffset = prefs[Keys.LAST_THIRD_OFFSET]
//            val calculationMethodName = prefs[Keys.CALCULATION_METHOD_NAME]
            val showNextLastThird = prefs[Keys.SHOW_NEXT_LAST_THIRD]
            val showNextMidnight = prefs[Keys.SHOW_NEXT_MIDNIGHT]
            val showNextDuha = prefs[Keys.SHOW_NEXT_DUHA]
            val showNextSunrise = prefs[Keys.SHOW_NEXT_SUNRISE]
            val showNextIqama = prefs[Keys.SHOW_NEXT_IQAMA]
            val isAutoCalculationMethodEnabled = prefs[Keys.IS_AUTO_CALCULATION_METHOD_ENABLED] ?: defaults.isAutoCalculationMethodEnabled
            val autoCalculationMethod = prefs[Keys.AUTO_CALCULATION_METHOD]
            val manualCalculationMethodName = prefs[Keys.MANUAL_CALCULATION_METHOD]

//                val method: CalculationMethod = if (autoCalculationMethod == true) getMethodFromCountryCode(countryCode) else when (calculationMethodName) {
//                    CalculationMethod.EGYPTIAN.name -> CalculationMethod.EGYPTIAN
//                    CalculationMethod.NORTH_AMERICA.name -> CalculationMethod.NORTH_AMERICA
//                    CalculationMethod.MUSLIM_WORLD_LEAGUE.name -> CalculationMethod.MUSLIM_WORLD_LEAGUE
//                    CalculationMethod.MOON_SIGHTING_COMMITTEE.name -> CalculationMethod.MOON_SIGHTING_COMMITTEE
//                    CalculationMethod.SINGAPORE.name -> CalculationMethod.SINGAPORE
//                    CalculationMethod.DUBAI.name -> CalculationMethod.DUBAI
//                    CalculationMethod.KARACHI.name -> CalculationMethod.KARACHI
//                    CalculationMethod.KUWAIT.name -> CalculationMethod.KUWAIT
//                    CalculationMethod.QATAR.name -> CalculationMethod.QATAR
//                    CalculationMethod.TURKEY.name -> CalculationMethod.TURKEY
//                    CalculationMethod.UMM_AL_QURA.name -> CalculationMethod.UMM_AL_QURA
//                    else -> getMethodFromCountryCode(countryCode)
//                }

            val manualMethod: CalculationMethod = CalculationMethod.entries.find { it.name == manualCalculationMethodName }?: defaults.manualCalculationMethod

            val method: CalculationMethod = when {
                isAutoCalculationMethodEnabled -> {
                    getMethodFromCountryCode(countryCode)
                }
                else -> manualMethod
            }

            PrayerConfig(
                fajrOffset = fajrOffset ?: defaults.fajrOffset,
                iqamaFajrOffset = iqamaFajrOffset ?: defaults.iqamaFajrOffset,
                sunriseOffset = sunriseOffset ?: defaults.sunriseOffset,
                duhaSunriseOffset = duhaSunriseOffset ?: defaults.duhaSunriseOffset,
                zuhrOffset = zuhrOffset ?: defaults.zuhrOffset,
                iqamaZuhrOffset = iqamaZuhrOffset ?: defaults.iqamaZuhrOffset,
                asrOffset = asrOffset ?: defaults.asrOffset,
                iqamaAsrOffset = iqamaAsrOffset ?: defaults.iqamaAsrOffset,
                sunsetOffset = sunsetOffset ?: defaults.sunsetOffset,
                iqamaSunsetOffset = iqamaSunsetOffset ?: defaults.iqamaSunsetOffset,
                ishaOffset = ishaOffset ?: defaults.ishaOffset,
                iqamaIshaOffset = iqamaIshaOffset ?: defaults.iqamaIshaOffset,
                midnightOffset = midnightOffset ?: defaults.midnightOffset,
                lastThirdOffset = lastThirdOffset ?: defaults.lastThirdOffset,
                showNextLastThird = showNextLastThird ?: defaults.showNextLastThird,
                showNextMidnight = showNextMidnight ?: defaults.showNextMidnight,
                showNextDuha = showNextDuha ?: defaults.showNextDuha,
                showNextSunrise = showNextSunrise ?: defaults.showNextSunrise,
                showNextIqama = showNextIqama ?: defaults.showNextIqama,
                autoCalculationMethod = try {
                    if (autoCalculationMethod != null) CalculationMethod.valueOf(
                        autoCalculationMethod
                    ) else null
                } catch (_: Exception) {
                    null
                },
                manualCalculationMethod = manualMethod,
                isAutoCalculationMethodEnabled = isAutoCalculationMethodEnabled
                    ?: defaults.isAutoCalculationMethodEnabled,
                method = method
            )
        }
    }


    suspend fun savePrayerTimes(times: PrayerTimes) {
        dataStore.edit { prefs ->
            prefs[Keys.FAJR] = times.fajr
            prefs[Keys.IQAMA_FAJR] = times.iqamaFajr
            prefs[Keys.SUNRISE] = times.sunrise
            prefs[Keys.DUHA] = times.duha
            prefs[Keys.ZUHR] = times.zuhr
            prefs[Keys.IQAMA_ZUHR] = times.iqamaZuhr
            prefs[Keys.ASR] = times.asr
            prefs[Keys.IQAMA_ASR] = times.iqamaAsr
            prefs[Keys.SUNSET] = times.sunset
            prefs[Keys.IQAMA_SUNSET] = times.iqamaSunset
            prefs[Keys.ISHA] = times.isha
            prefs[Keys.IQAMA_ISHA] = times.iqamaIsha
            prefs[Keys.MIDNIGHT] = times.midnight
            prefs[Keys.LAST_THIRD] = times.lastThird
        }
    }


    fun getPrayerTimes(): Flow<PrayerTimes?> {
        return dataStore.data.map { prefs ->

            val fajr = prefs[Keys.FAJR]
            val iqamaFajr = prefs[Keys.IQAMA_FAJR]
            val sunrise = prefs[Keys.SUNRISE]
            val duha = prefs[Keys.DUHA]
            val dhuhr = prefs[Keys.ZUHR]
            val iqamaDhuhr = prefs[Keys.IQAMA_ZUHR]
            val asr = prefs[Keys.ASR]
            val iqamaAsr = prefs[Keys.IQAMA_ASR]
            val maghrib = prefs[Keys.SUNSET]
            val iqamaMaghrib = prefs[Keys.IQAMA_SUNSET]
            val isha = prefs[Keys.ISHA]
            val iqamaIsha = prefs[Keys.IQAMA_ISHA]
            val midnight = prefs[Keys.MIDNIGHT]
            val lastThird = prefs[Keys.LAST_THIRD]


            val mandatoryKeys = listOf(
                Keys.FAJR, Keys.SUNRISE, Keys.ZUHR, Keys.ASR,
                Keys.SUNSET, Keys.ISHA, Keys.LAST_THIRD
            )

            if (!mandatoryKeys.all { prefs.contains(it) }) {
                Log.w("InitialPrayerTimes", "DataStore missing mandatory keys")
                return@map null
            }

            Log.d(
                "InitialPrayerTimes",
                "DataStore sent non null initial times: fajr = $fajr, sunrise = $sunrise, zuhr = $dhuhr, asr = $asr, sunset = $maghrib, isha = $isha, midnight = $midnight, last third = $lastThird"
            )
            PrayerTimes(
                fajr = fajr ?: String(),
                iqamaFajr = iqamaFajr ?: String(),
                sunrise = sunrise ?: String(),
                duha = duha ?: String(),
                zuhr = dhuhr ?: String(),
                iqamaZuhr = iqamaDhuhr ?: String(),
                asr = asr ?: String(),
                iqamaAsr = iqamaAsr ?: String(),
                sunset = maghrib ?: String(),
                iqamaSunset = iqamaMaghrib ?: String(),
                isha = isha ?: String(),
                iqamaIsha = iqamaIsha ?: String(),
                midnight = midnight ?: String(),
                lastThird = lastThird ?: String()
            )

        }
    }


    private fun getMethodFromCountryCode(countryCode: String?): CalculationMethod {
        when (countryCode) {
            "SA" -> return CalculationMethod.UMM_AL_QURA

            "US", "CA" -> return CalculationMethod.NORTH_AMERICA

            "GB" -> return CalculationMethod.MOON_SIGHTING_COMMITTEE

            "EG", "SD" -> return CalculationMethod.EGYPTIAN

            "PK", "IN", "BD", "AF" -> return CalculationMethod.KARACHI

            "TR" -> return CalculationMethod.TURKEY

            "SG" -> return CalculationMethod.SINGAPORE

            "AE" -> return CalculationMethod.DUBAI

            "KW" -> return CalculationMethod.KUWAIT

            "QA" -> return CalculationMethod.QATAR

            "FR", "DE", "ID", "MY" -> {
                Log.d("CalculationMethod", "getMethodFromCountryCode: ${CalculationMethod.MUSLIM_WORLD_LEAGUE} the country code is either FR, DE, ID, or MY")
                return CalculationMethod.MUSLIM_WORLD_LEAGUE
            }

            null -> {
                Log.d("CalculationMethod", "getMethodFromCountryCode: ${CalculationMethod.MUSLIM_WORLD_LEAGUE} the country code is null")
                return CalculationMethod.MUSLIM_WORLD_LEAGUE
            }

            else -> {
                Log.d("CalculationMethod", "getMethodFromCountryCode: ${CalculationMethod.MUSLIM_WORLD_LEAGUE} the country code doesn't exist in the enum/code")
                return CalculationMethod.MUSLIM_WORLD_LEAGUE
            }
        }
    }

}
