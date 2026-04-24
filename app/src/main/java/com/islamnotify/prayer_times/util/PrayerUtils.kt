package com.islamnotify.prayer_times.util

import android.util.Log
import com.islamnotify.prayer_times.domain.model.PrayerData
import com.islamnotify.prayer_times.domain.model.PrayerEntities
import com.islamnotify.prayer_times.domain.model.PrayerTimes
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object PrayerUtils {

    const val CURRENT_DAY: Long = 0L
    const val NEXT_DAY: Long = 1L
    const val PREVIOUS_DAY: Long = -1L


    fun String.addMinutesToTime(minutesToAdd: Int): String {
        if (this.isEmpty()) {
            return this
        }

        try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
            val localTime = LocalTime.parse(this, formatter)
            val newTime = localTime.plusMinutes(minutesToAdd.toLong())
            return newTime.format(formatter)
        } catch (e: Exception) {
            Log.e(
                "addMinutesToTime",
                "Error parsing time string: $this. Please ensure it's in HH:mm format.",
                e
            )
            return this
        }
    }


    fun PrayerTimes.toPrayersEntities(log: String): PrayerEntities {
        Log.d("FetchPrayerTimes", "toPrayersEntities: called from $log")
        return PrayerEntities(
            fajr = buildPrayerData(PrayerTypes.FAJR, this.fajr),
            iqamaFajr = buildPrayerData(PrayerTypes.IQAMA_FAJR, this.iqamaFajr),
            sunrise = buildPrayerData(PrayerTypes.SUNRISE, this.sunrise),
            duha = buildPrayerData(PrayerTypes.DUHA, this.duha),
            zuhr = buildPrayerData(PrayerTypes.ZUHR, this.zuhr),
            iqamaZuhr = buildPrayerData(PrayerTypes.IQAMA_ZUHR, this.iqamaZuhr),
            asr = buildPrayerData(PrayerTypes.ASR, this.asr),
            iqamaAsr = buildPrayerData(PrayerTypes.IQAMA_ASR, this.iqamaAsr),
            sunset = buildPrayerData(PrayerTypes.SUNSET, this.sunset),
            iqamaSunset = buildPrayerData(PrayerTypes.IQAMA_SUNSET, this.iqamaSunset),
            isha = buildPrayerData(PrayerTypes.ISHA, this.isha),
            iqamaIsha = buildPrayerData(PrayerTypes.IQAMA_ISHA, this.iqamaIsha),
            midnight = buildPrayerData(PrayerTypes.MIDNIGHT, this.midnight),
            lastThird = buildPrayerData(PrayerTypes.LAST_THIRD, this.lastThird)
        )
    }


    fun buildPrayerData(type: PrayerTypes, time: String): PrayerData {
        return PrayerData(
            type = type,
            time = time,
            millis = time.prayerTimeToMillis(CURRENT_DAY),
            nextDayMillis = time.prayerTimeToMillis(NEXT_DAY),
            previousDayMillis = time.prayerTimeToMillis(PREVIOUS_DAY)
        )
    }


    fun String.prayerTimeToMillis(increment: Long) : Long {
        try {
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
            val targetTime = LocalTime.parse(this, timeFormatter)
            val currentDate = LocalDate.now()
            val targetDateTime = LocalDateTime.of(currentDate, targetTime).plusDays(increment)

            val zonedTime = targetDateTime.atZone(ZoneId.systemDefault())
            return zonedTime.toInstant().toEpochMilli()
        }catch (e: Exception){
            Log.e("PrayerDataUtils", "prayerTimeToMillis: ", e)
            return 0L
        }
    }

}