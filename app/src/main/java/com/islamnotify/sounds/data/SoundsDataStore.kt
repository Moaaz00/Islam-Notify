package com.islamnotify.sounds.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import com.islamnotify.sounds.di.SoundsModules
import com.islamnotify.sounds.domain.SoundStates
import com.islamnotify.sounds.domain.SoundsConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SoundsDataStore(
    @param:SoundsModules.SoundPrefs val dataStore: DataStore<Preferences>
) {

    object Keys {
        val IS_AZAN_ENABLED = booleanPreferencesKey("is_azan_enabled")
        val IS_IQAMA_ENABLED = booleanPreferencesKey("is_iqama_enabled")
        val IS_PLAY_WHILE_MUTE = booleanPreferencesKey("is_play_while_mute")

        val AZAN_SOUND_URI = stringPreferencesKey("azan_sound_uri")
        val IQAMA_SOUND_URI = stringPreferencesKey("iqama_sound_uri")
        val NOTIFY_SOUND_URI = stringPreferencesKey("notify_sound_uri")

        val FAJR_SOUND_STATE = stringPreferencesKey("fajr_sound_state")
        val IQAMA_FAJR_SOUND_STATE = stringPreferencesKey("iqama_fajr_sound_state")
        val SUNRISE_SOUND_STATE = stringPreferencesKey("sunrise_sound_state")
        val DUHA_SOUND_STATE = stringPreferencesKey("duha_sound_state")
        val ZUHR_SOUND_STATE = stringPreferencesKey("zuhr_sound_state")
        val IQAMA_ZUHR_SOUND_STATE = stringPreferencesKey("iqama_zuhr_sound_state")
        val ASR_SOUND_STATE = stringPreferencesKey("asr_sound_state")
        val IQAMA_ASR_SOUND_STATE = stringPreferencesKey("iqama_asr_sound_state")
        val SUNSET_SOUND_STATE = stringPreferencesKey("sunset_sound_state")
        val IQAMA_SUNSET_SOUND_STATE = stringPreferencesKey("iqama_sunset_sound_state")
        val ISHA_SOUND_STATE = stringPreferencesKey("isha_sound_state")
        val IQAMA_ISHA_SOUND_STATE = stringPreferencesKey("iqama_isha_sound_state")
        val MIDNIGHT_SOUND_STATE = stringPreferencesKey("midnight_sound_state")
        val LAST_THIRD_SOUND_STATE = stringPreferencesKey("last_third_sound_state")
    }


    suspend fun saveConfig(soundsConfig: SoundsConfig) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_AZAN_ENABLED] = soundsConfig.isAzanEnabled
            prefs[Keys.IS_IQAMA_ENABLED] = soundsConfig.isIqamaEnabled
            prefs[Keys.IS_PLAY_WHILE_MUTE] = soundsConfig.isPlayWhileMute

            soundsConfig.azanSoundUriString?.let { prefs[Keys.AZAN_SOUND_URI] = it }
            soundsConfig.iqamaSoundUriString?.let { prefs[Keys.IQAMA_SOUND_URI] = it }
            soundsConfig.notifySoundUriString?.let { prefs[Keys.NOTIFY_SOUND_URI] = it }

            prefs[Keys.FAJR_SOUND_STATE] = soundsConfig.fajrSoundState.name
            prefs[Keys.IQAMA_FAJR_SOUND_STATE] = soundsConfig.iqamaFajrSoundState.name
            prefs[Keys.SUNRISE_SOUND_STATE] = soundsConfig.sunriseSoundState.name
            prefs[Keys.DUHA_SOUND_STATE] = soundsConfig.duhaSoundState.name
            prefs[Keys.ZUHR_SOUND_STATE] = soundsConfig.zuhrSoundState.name
            prefs[Keys.IQAMA_ZUHR_SOUND_STATE] = soundsConfig.iqamaZuhrSoundState.name
            prefs[Keys.ASR_SOUND_STATE] = soundsConfig.asrSoundState.name
            prefs[Keys.IQAMA_ASR_SOUND_STATE] = soundsConfig.iqamaAsrSoundState.name
            prefs[Keys.SUNSET_SOUND_STATE] = soundsConfig.sunsetSoundState.name
            prefs[Keys.IQAMA_SUNSET_SOUND_STATE] = soundsConfig.iqamaSunsetSoundState.name
            prefs[Keys.ISHA_SOUND_STATE] = soundsConfig.ishaSoundState.name
            prefs[Keys.IQAMA_ISHA_SOUND_STATE] = soundsConfig.iqamaIshaSoundState.name
            prefs[Keys.MIDNIGHT_SOUND_STATE] = soundsConfig.midnightSoundState.name
            prefs[Keys.LAST_THIRD_SOUND_STATE] = soundsConfig.lastThirdSoundState.name
        }
    }

    suspend fun toggleSoundState(prayerType: PrayerTypes) {
        dataStore.edit { prefs ->
            val defaults = SoundsConfig()
            val (key, defaultState) = when (prayerType) {
                PrayerTypes.FAJR -> Keys.FAJR_SOUND_STATE to defaults.fajrSoundState
                PrayerTypes.ZUHR -> Keys.ZUHR_SOUND_STATE to defaults.zuhrSoundState
                PrayerTypes.ASR -> Keys.ASR_SOUND_STATE to defaults.asrSoundState
                PrayerTypes.SUNSET -> Keys.SUNSET_SOUND_STATE to defaults.sunsetSoundState
                PrayerTypes.ISHA -> Keys.ISHA_SOUND_STATE to defaults.ishaSoundState
                PrayerTypes.SUNRISE -> Keys.SUNRISE_SOUND_STATE to defaults.sunriseSoundState
                PrayerTypes.LAST_THIRD -> Keys.LAST_THIRD_SOUND_STATE to defaults.lastThirdSoundState
                else -> null
            } ?: return@edit

            val currentStateName = prefs[key] ?: defaultState.name
            val currentState = try {
                SoundStates.valueOf(currentStateName)
            } catch (_: Exception) {
                defaultState
            }

            val nextState = when (prayerType) {
                PrayerTypes.FAJR, PrayerTypes.ZUHR, PrayerTypes.ASR, PrayerTypes.SUNSET, PrayerTypes.ISHA -> {
                    when (currentState) {
                        SoundStates.AZAN -> SoundStates.NOTIFY
                        SoundStates.NOTIFY -> SoundStates.MUTE
                        else -> SoundStates.AZAN
                    }
                }

                else -> {
                    when (currentState) {
                        SoundStates.NOTIFY -> SoundStates.MUTE
                        else -> SoundStates.NOTIFY
                    }
                }
            }

            prefs[key] = nextState.name
        }
    }


    fun getConfig(): Flow<SoundsConfig> {
        return dataStore.data.map { preferences ->
            val default = SoundsConfig()

            SoundsConfig(
                isAzanEnabled = preferences[Keys.IS_AZAN_ENABLED] ?: default.isAzanEnabled,
                isIqamaEnabled = preferences[Keys.IS_IQAMA_ENABLED] ?: default.isIqamaEnabled,
                isPlayWhileMute = preferences[Keys.IS_PLAY_WHILE_MUTE] ?: default.isPlayWhileMute,

                azanSoundUriString = preferences[Keys.AZAN_SOUND_URI] ?: default.azanSoundUriString,
                iqamaSoundUriString = preferences[Keys.IQAMA_SOUND_URI]
                    ?: default.iqamaSoundUriString,
                notifySoundUriString = preferences[Keys.NOTIFY_SOUND_URI]
                    ?: default.notifySoundUriString,

                fajrSoundState = SoundStates.valueOf(
                    preferences[Keys.FAJR_SOUND_STATE] ?: default.fajrSoundState.name
                ),
                iqamaFajrSoundState = SoundStates.valueOf(
                    preferences[Keys.IQAMA_FAJR_SOUND_STATE] ?: default.iqamaFajrSoundState.name
                ),
                sunriseSoundState = SoundStates.valueOf(
                    preferences[Keys.SUNRISE_SOUND_STATE] ?: default.sunriseSoundState.name
                ),
                duhaSoundState = SoundStates.valueOf(
                    preferences[Keys.DUHA_SOUND_STATE] ?: default.duhaSoundState.name
                ),
                zuhrSoundState = SoundStates.valueOf(
                    preferences[Keys.ZUHR_SOUND_STATE] ?: default.zuhrSoundState.name
                ),
                iqamaZuhrSoundState = SoundStates.valueOf(
                    preferences[Keys.IQAMA_ZUHR_SOUND_STATE] ?: default.iqamaZuhrSoundState.name
                ),
                asrSoundState = SoundStates.valueOf(
                    preferences[Keys.ASR_SOUND_STATE] ?: default.asrSoundState.name
                ),
                iqamaAsrSoundState = SoundStates.valueOf(
                    preferences[Keys.IQAMA_ASR_SOUND_STATE] ?: default.iqamaAsrSoundState.name
                ),
                sunsetSoundState = SoundStates.valueOf(
                    preferences[Keys.SUNSET_SOUND_STATE] ?: default.sunsetSoundState.name
                ),
                iqamaSunsetSoundState = SoundStates.valueOf(
                    preferences[Keys.IQAMA_SUNSET_SOUND_STATE] ?: default.iqamaSunsetSoundState.name
                ),
                ishaSoundState = SoundStates.valueOf(
                    preferences[Keys.ISHA_SOUND_STATE] ?: default.ishaSoundState.name
                ),
                iqamaIshaSoundState = SoundStates.valueOf(
                    preferences[Keys.IQAMA_ISHA_SOUND_STATE] ?: default.iqamaIshaSoundState.name
                ),
                midnightSoundState = SoundStates.valueOf(
                    preferences[Keys.MIDNIGHT_SOUND_STATE] ?: default.midnightSoundState.name
                ),
                lastThirdSoundState = SoundStates.valueOf(
                    preferences[Keys.LAST_THIRD_SOUND_STATE] ?: default.lastThirdSoundState.name
                )
            )
        }

    }
}