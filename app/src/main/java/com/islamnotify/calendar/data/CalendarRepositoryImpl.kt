package com.islamnotify.calendar.data

import android.content.Context
import com.islamnotify.R
import com.islamnotify.calendar.domain.CalendarRepository
import com.islamnotify.calendar.domain.DateConfig
import com.islamnotify.calendar.domain.DateModel
import com.islamnotify.common.AppUtils.getLocalizedContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject


class CalendarRepositoryImpl @Inject constructor(
    @param:ApplicationContext val context: Context,
    val dataStore: CalendarDataStore
): CalendarRepository {
    override suspend fun getHijriDate(): DateModel {
        val hijriOffset: Int = dataStore.getConfig().first().hijriOffset
        val localizedContext = context.getLocalizedContext()
        val locale: Locale = localizedContext.resources.configuration.locales[0]

        val date = LocalDate.now()
        val hijriDate = HijrahChronology.INSTANCE.date(date)
            .plus(hijriOffset.toLong(), ChronoUnit.DAYS)

        val dayOfMonth = hijriDate.get(ChronoField.DAY_OF_MONTH)
        val monthNumber = hijriDate.get(ChronoField.MONTH_OF_YEAR)
        val year = hijriDate.get(ChronoField.YEAR)

        val monthName: String =
            localizedContext.resources.getStringArray(R.array.hijri_months_names)[monthNumber - 1]
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)

        val numberFormat = NumberFormat.getInstance(locale)
        numberFormat.isGroupingUsed = false

        // Format the day and year integers for arabic integers
        val formatedDayOfMonth = numberFormat.format(dayOfMonth.toLong())
        val formatedYear = numberFormat.format(year.toLong())

        return DateModel(
            formatedDayOfMonth = formatedDayOfMonth,
            dayOfWeek = dayOfWeek,
            monthName = monthName,
            formatedMonthNumber = "$monthNumber",
            formatedYear = formatedYear,
            dayOfMonth = dayOfMonth,
            monthNumber = monthNumber,
            year = year
        )
    }


    override suspend fun getConfig(): DateConfig {
        return dataStore.getConfig().first()
    }

    override suspend fun saveConfig(transform: (DateConfig) -> DateConfig) {
        val config = dataStore.getConfig().first()
        val newConfig = transform(config)
        dataStore.saveConfig(newConfig)
    }
}