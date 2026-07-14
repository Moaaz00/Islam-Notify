package com.islamnotify.common

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.islamnotify.R
import com.islamnotify.common.domain.CrashReporterProvider
import com.islamnotify.events.domain.EventFlags
import com.islamnotify.prayer_times.domain.model.NextPrayerData
import com.islamnotify.prayer_times.domain.model.PrayerData
import com.islamnotify.prayer_times.domain.model.PrayerEntities
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import com.islamnotify.sounds.domain.SoundStates
import com.islamnotify.sounds.domain.SoundsWork
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.chrono.HijrahChronology
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.util.Locale

object AppUtils {

    const val NOTIFICATION_ALARM_ACTION = "NOTIFICATION_ALARM_ACTION"

    const val NOTIFICATION_MIDNIGHT_ALARM_ACTION = "NOTIFICATION_MIDNIGHT_ALARM_ACTION"

    const val PRAYER_NOTIFICATION_CHANNEL_ID = "PRAYER_NOTIFICATION_CHANNEL_ID"
    const val EVENTS_NOTIFICATION_CHANNEL_ID = "EVENTS_NOTIFICATION_CHANNEL_ID"
    const val SOUNDS_NOTIFICATION_CHANNEL_ID = "SOUNDS_NOTIFICATION_CHANNEL_ID"
    const val OTHERS_NOTIFICATION_CHANNEL_ID = "OTHERS_NOTIFICATION_CHANNEL_ID"

    const val ALARM_NOTIFICATION_CHANNEL = "ALARM_NOTIFICATION_CHANNEL"

    fun Context.getLocalizedContext(): Context {
        val locales: LocaleListCompat = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return this
        val locale = locales.get(0) ?: Locale.getDefault()

        val config = Configuration(this.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return this.createConfigurationContext(config)
    }


    fun PrayerEntities.toPrayerDataList(): List<PrayerData> {
        return listOf(
            this.fajr,
            this.iqamaFajr,
            this.sunrise,
            this.duha,
            this.zuhr,
            this.iqamaZuhr,
            this.asr,
            this.iqamaAsr,
            this.sunset,
            this.iqamaSunset,
            this.isha,
            this.iqamaIsha,
            this.midnight,
            this.lastThird
        )
    }

    fun formatPrayerTypes(appContext: Context, nextPrayerData: NextPrayerData): String {
        val localizedContext = appContext.getLocalizedContext()

        return when (nextPrayerData.type) {
            PrayerTypes.FAJR -> {
                localizedContext.getString(R.string.fajr_name)
            }

            PrayerTypes.IQAMA_FAJR -> {
                localizedContext.getString(R.string.iqama_fajr_name)
            }

            PrayerTypes.SUNRISE -> {
                localizedContext.getString(R.string.sunrise_name)
            }

            PrayerTypes.DUHA -> {
                localizedContext.getString(R.string.duha_name)
            }

            PrayerTypes.ZUHR -> {
                if (AppUtils.isTodayFriday()) {
                    localizedContext.getString(R.string.jummah_name)
                } else {
                    localizedContext.getString(R.string.zuhr_name)
                }
            }

            PrayerTypes.IQAMA_ZUHR -> {
                localizedContext.getString(R.string.iqama_zuhr_name)
            }

            PrayerTypes.ASR -> {
                localizedContext.getString(R.string.asr_name)
            }

            PrayerTypes.IQAMA_ASR -> {
                localizedContext.getString(R.string.iqama_asr_name)
            }

            PrayerTypes.SUNSET -> {
                localizedContext.getString(R.string.sunset_name)
            }

            PrayerTypes.IQAMA_SUNSET -> {
                localizedContext.getString(R.string.iqama_sunset_name)
            }

            PrayerTypes.ISHA -> {
                localizedContext.getString(R.string.isha_name)
            }

            PrayerTypes.IQAMA_ISHA -> {
                localizedContext.getString(R.string.iqama_isha_name)
            }

            PrayerTypes.LAST_THIRD -> {
                localizedContext.getString(R.string.last_third_name)
            }

            PrayerTypes.MIDNIGHT -> {
                localizedContext.getString(R.string.midnight_name)
            }

            PrayerTypes.EMPTY -> {
                ""
            }
        }

    }

//    fun String.formatPrayerTimes(): String {
//        return try {
//            val dateTimeFormatter24 = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
//            val dateTimeFormatter12 = DateTimeFormatter.ofPattern("hh:mm", Locale.getDefault())
//            val time24 = dateTimeFormatter24.parse(this)
//            dateTimeFormatter12.format(time24)
//        } catch (_: Exception) {
//            this
//        }
//    }


    fun String.formatPrayerTimes(appContext: Context): String {
        return try {
            val localizedContext = appContext.getLocalizedContext()
            val inputFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
            val time = LocalTime.parse(this, inputFormatter)

            val locale: Locale = localizedContext.resources.configuration.locales[0]

            val formatter = DateTimeFormatter.ofPattern("hh:mm")
                .withLocale(locale)
                .withDecimalStyle(DecimalStyle.of(locale))

            time.format(formatter)

        } catch (e: Exception) {
            Log.e("formating prayer times", "formatting error", e)
            CrashReporterProvider.instance?.recordNonFatal(e)
            this
        }
    }


    fun getMidnightTomorrowPlusSeconds(secondsToAdd: Int): Long {
        val tomorrow = LocalDate.now().plusDays(1)
        return tomorrow.atStartOfDay(ZoneId.systemDefault()).plusSeconds(secondsToAdd.toLong())
            .toInstant().toEpochMilli()
    }


    fun getHijriDate(appContext: Context, withDay: Boolean): String {
        val date = LocalDate.now()
        val hijriDate = HijrahChronology.INSTANCE.date(date)
        val day = hijriDate.get(ChronoField.DAY_OF_MONTH)
        val monthNumber = hijriDate.get(ChronoField.MONTH_OF_YEAR)
        val year = hijriDate.get(ChronoField.YEAR)

        val localizedContext = appContext.getLocalizedContext()
        val monthName: String =
            localizedContext.resources.getStringArray(R.array.hijri_months_names)[monthNumber - 1]
        val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())

        val locale: Locale = localizedContext.resources.configuration.locales[0]
        val numberFormat = NumberFormat.getInstance(locale)
        numberFormat.isGroupingUsed = false

        // Format the day and year integers
        val arabicDay = numberFormat.format(day.toLong())
        val arabicYear = numberFormat.format(year.toLong())

        return if (!withDay) "$arabicDay $monthName $arabicYear"
        else "$dayName, $arabicDay $monthName $arabicYear"
    }

    fun isTodayFriday(): Boolean {
        val date = LocalDate.now()
        return date.dayOfWeek == DayOfWeek.FRIDAY
    }


}