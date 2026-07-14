package com.islamnotify.sounds.utils

import android.util.Log
import com.islamnotify.common.AppUtils
import com.islamnotify.common.domain.CrashReporterProvider
import com.islamnotify.prayer_times.domain.model.PrayerEntities
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import com.islamnotify.sounds.data.PrayersSoundData
import com.islamnotify.sounds.domain.SoundStates
import com.islamnotify.sounds.domain.SoundsConfig

object SoundsUtils {

    const val ACTION_AZAN_SOUND = "ACTION_AZAN_SOUND"
    const val ACTION_IQAMA_SOUND = "ACTION_IQAMA_SOUND"
    const val ACTION_NOTIFY_SOUND = "ACTION_NOTIFY_SOUND"
    const val ACTION_SOUNDS_MIDNIGHT_REQUEST = "ACTION_SOUNDS_MIDNIGHT_REQUEST"
    const val REQUEST_CODE_SOUNDS_MIDNIGHT_REQUEST = 10101
    const val MIDNIGHT_WORK_REQUEST_TAG = "MIDNIGHT_WORK_REQUEST_TAG"
    const val EXTRA_PRAYER_TYPE = "prayerType"
    const val SOUNDS_NOTIFICATION_ID = 10000


    fun PrayerEntities.toSoundDataList(soundConfig: SoundsConfig): List<PrayersSoundData> {
        val list = mutableListOf(
            PrayersSoundData(
                prayerType = PrayerTypes.FAJR,
                millis = this.fajr.millis,
                soundState = soundConfig.fajrSoundState
            ),

            PrayersSoundData(
                prayerType = PrayerTypes.IQAMA_FAJR,
                millis = this.iqamaFajr.millis,
                soundState = soundConfig.iqamaFajrSoundState
            ),

            PrayersSoundData(
                prayerType = PrayerTypes.SUNRISE,
                millis = this.sunrise.millis,
                soundState = soundConfig.sunriseSoundState
            ),

            PrayersSoundData(
                prayerType = PrayerTypes.DUHA,
                millis = this.duha.millis,
                soundState = soundConfig.duhaSoundState
            ),

            PrayersSoundData(
                prayerType = PrayerTypes.ZUHR,
                millis = this.zuhr.millis,
                soundState = soundConfig.zuhrSoundState
            ),

            PrayersSoundData(
                prayerType = PrayerTypes.ASR,
                millis = this.asr.millis,
                soundState = soundConfig.asrSoundState
            ),

            PrayersSoundData(
                prayerType = PrayerTypes.IQAMA_ASR,
                millis = this.iqamaAsr.millis,
                soundState = soundConfig.iqamaAsrSoundState
            ),

            PrayersSoundData(
                prayerType = PrayerTypes.SUNSET,
                millis = this.sunset.millis,
                soundState = soundConfig.sunsetSoundState
            ),

            PrayersSoundData(
                prayerType = PrayerTypes.IQAMA_SUNSET,
                millis = this.iqamaSunset.millis,
                soundState = soundConfig.iqamaSunsetSoundState
            ),

            PrayersSoundData(
                prayerType = PrayerTypes.ISHA,
                millis = this.isha.millis,
                soundState = soundConfig.ishaSoundState
            ),

            PrayersSoundData(
                prayerType = PrayerTypes.IQAMA_ISHA,
                millis = this.iqamaIsha.millis,
                soundState = soundConfig.iqamaIshaSoundState
            ),

            PrayersSoundData(
                prayerType = PrayerTypes.MIDNIGHT,
                millis = this.midnight.millis,
                soundState = soundConfig.midnightSoundState
            ),

            PrayersSoundData(
                prayerType = PrayerTypes.LAST_THIRD,
                millis = this.lastThird.millis,
                soundState = soundConfig.lastThirdSoundState
            )
        )

        // no iqama for jumm'ah
        if (!AppUtils.isTodayFriday()) {
            list.add(
                PrayersSoundData(
                    prayerType = PrayerTypes.IQAMA_ZUHR,
                    millis = this.iqamaZuhr.millis,
                    soundState = soundConfig.iqamaZuhrSoundState
                )
            )
        }

        return list
    }


    fun shouldStartSoundWork(soundsConfig: SoundsConfig): Boolean {
        try{
            soundsConfig.apply {
                val azanEnabled = isAzanEnabled
                val iqamaEnabled = isIqamaEnabled
                val allMuted =
                    fajrSoundState == SoundStates.MUTE &&
                            iqamaFajrSoundState == SoundStates.MUTE &&
                            sunriseSoundState == SoundStates.MUTE &&
                            duhaSoundState == SoundStates.MUTE &&
                            zuhrSoundState == SoundStates.MUTE &&
                            iqamaZuhrSoundState == SoundStates.MUTE &&
                            asrSoundState == SoundStates.MUTE &&
                            iqamaAsrSoundState == SoundStates.MUTE &&
                            sunsetSoundState == SoundStates.MUTE &&
                            iqamaSunsetSoundState == SoundStates.MUTE &&
                            ishaSoundState == SoundStates.MUTE &&
                            iqamaIshaSoundState == SoundStates.MUTE &&
                            midnightSoundState == SoundStates.MUTE &&
                            lastThirdSoundState == SoundStates.MUTE

                return azanEnabled || iqamaEnabled || !allMuted
            }
        }catch (e: Exception){
            Log.e("SoundsFlow", "shouldStartSound: ", e)
            CrashReporterProvider.instance?.recordNonFatal(e)
            return false
        }
    }
}