package com.islamnotify.notification.domain

import com.islamnotify.prayer_times.domain.model.PrayerTypes
import java.time.ZonedDateTime

/** One occurrence of an event (prayer, iqama, sunrise, duha, midnight, last third) on a specific day. */
data class EventOccurrence(
    val type: PrayerTypes,
    val dateTime: ZonedDateTime
) {
    val millis: Long get() = dateTime.toInstant().toEpochMilli()
}

enum class RowState { PAST, NEXT, UPCOMING }

/** One row of the expanded notification's prayer table. */
data class TableRow(
    val type: PrayerTypes,
    val dateTime: ZonedDateTime,
    val state: RowState
)

/**
 * Everything the prayer notification shows, derived from the prayer times and "now".
 * Pure data: strings, formatting and views are built from it in the data layer.
 */
data class PrayerNotificationState(
    /** The next event after now (the countdown). */
    val hero: EventOccurrence,
    /** The event right after the hero (the "next event" line). */
    val following: EventOccurrence,
    /** The last event at or before now (the start of the progress bar). */
    val previous: EventOccurrence,
    /** Elapsed share of previous → hero, 0..[PrayerNotificationCalculator.PROGRESS_MAX]. */
    val progress: Int,
    /** Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha of the prayer day the hero belongs to. */
    val rows: List<TableRow>
)
