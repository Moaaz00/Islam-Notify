package com.islamnotify.notification.domain

import com.islamnotify.prayer_times.domain.model.PrayerEntities
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * Decides what the prayer notification shows at a given moment. Pure Kotlin so every case
 * (after Isha, past midnight, iqama, Duha, Friday…) can be unit-tested with a fake "now".
 */
object PrayerNotificationCalculator {

    const val PROGRESS_MAX = 1000

    /** The six rows of the table, in display order. */
    val TABLE_TYPES = listOf(
        PrayerTypes.FAJR,
        PrayerTypes.SUNRISE,
        PrayerTypes.ZUHR,
        PrayerTypes.ASR,
        PrayerTypes.SUNSET,
        PrayerTypes.ISHA
    )

    // Days around "now" to generate occurrences for. The following event can be up to a day
    // after the hero, which itself can be up to a day after now.
    private val DAY_OFFSETS = -1L..2L

    /**
     * @param times the time of day of every event (at least the six table prayers).
     * @param events the events the user wants counted down to, in the same order
     *   [com.islamnotify.prayer_times.domain.PrayerDataUseCase.getNextPrayer] uses, so that
     *   equal times resolve to the same event as the alarms.
     * @return null when there is no usable time to count down to.
     */
    fun compute(
        times: Map<PrayerTypes, LocalTime>,
        events: List<PrayerTypes>,
        now: ZonedDateTime
    ): PrayerNotificationState? {
        val nowMillis = now.toInstant().toEpochMilli()
        val occurrences = events.flatMap { occurrencesOf(it, times, now) }
        if (occurrences.isEmpty()) return null

        // minByOrNull/maxByOrNull keep the first of equal elements: list order breaks ties.
        val hero = occurrences.filter { it.millis > nowMillis }.minByOrNull { it.millis } ?: return null
        val following = occurrences.filter { it.millis > hero.millis }.minByOrNull { it.millis } ?: return null
        val previous = occurrences.filter { it.millis <= nowMillis }.maxByOrNull { it.millis } ?: return null

        val span = hero.millis - previous.millis
        val progress = ((nowMillis - previous.millis) * PROGRESS_MAX / span)
            .coerceIn(0L, PROGRESS_MAX.toLong()).toInt()

        val rows = tableRows(hero, times, now, nowMillis) ?: return null

        return PrayerNotificationState(
            hero = hero,
            following = following,
            previous = previous,
            progress = progress,
            rows = rows
        )
    }

    /**
     * The table shows the prayer day (Fajr onwards) of the row the hero belongs to, so after
     * Isha it moves to tomorrow, and an Isha iqama still shows tonight's Isha in bold.
     */
    private fun tableRows(
        hero: EventOccurrence,
        times: Map<PrayerTypes, LocalTime>,
        now: ZonedDateTime,
        nowMillis: Long
    ): List<TableRow>? {
        val boldType = rowOf(hero.type)
        val boldOccurrences = occurrencesOf(boldType, times, now)
        val bold = if (hero.type in IQAMA_TYPES) {
            // The iqama's own prayer: the latest one at or before the iqama.
            boldOccurrences.filter { it.millis <= hero.millis }.maxByOrNull { it.millis }
        } else {
            // The prayer itself, or for Duha / Midnight / Last third the next prayer after it.
            boldOccurrences.filter { it.millis >= hero.millis }.minByOrNull { it.millis }
        } ?: return null

        // The Fajr that starts the bold row's prayer day.
        val dayStart = occurrencesOf(PrayerTypes.FAJR, times, now)
            .filter { it.millis <= bold.millis }
            .maxByOrNull { it.millis } ?: return null

        return TABLE_TYPES.map { type ->
            val occurrence = occurrencesOf(type, times, now)
                .filter { it.millis >= dayStart.millis }
                .minByOrNull { it.millis } ?: return null
            val state = when {
                type == boldType -> RowState.NEXT
                occurrence.millis <= nowMillis -> RowState.PAST
                else -> RowState.UPCOMING
            }
            TableRow(type, occurrence.dateTime, state)
        }
    }

    /** The table row an event belongs to. */
    fun rowOf(type: PrayerTypes): PrayerTypes = when (type) {
        PrayerTypes.IQAMA_FAJR -> PrayerTypes.FAJR
        PrayerTypes.IQAMA_ZUHR -> PrayerTypes.ZUHR
        PrayerTypes.IQAMA_ASR -> PrayerTypes.ASR
        PrayerTypes.IQAMA_SUNSET -> PrayerTypes.SUNSET
        PrayerTypes.IQAMA_ISHA -> PrayerTypes.ISHA
        // No row of their own: the next prayer after them is highlighted.
        PrayerTypes.DUHA -> PrayerTypes.ZUHR
        PrayerTypes.MIDNIGHT, PrayerTypes.LAST_THIRD -> PrayerTypes.FAJR
        else -> type
    }

    private val IQAMA_TYPES = setOf(
        PrayerTypes.IQAMA_FAJR,
        PrayerTypes.IQAMA_ZUHR,
        PrayerTypes.IQAMA_ASR,
        PrayerTypes.IQAMA_SUNSET,
        PrayerTypes.IQAMA_ISHA
    )

    private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

    /** The time of day of every event. Events whose stored time can't be parsed are left out. */
    fun eventTimes(prayers: PrayerEntities): Map<PrayerTypes, LocalTime> = listOf(
        prayers.fajr, prayers.iqamaFajr, prayers.sunrise, prayers.duha,
        prayers.zuhr, prayers.iqamaZuhr, prayers.asr, prayers.iqamaAsr,
        prayers.sunset, prayers.iqamaSunset, prayers.isha, prayers.iqamaIsha,
        prayers.midnight, prayers.lastThird
    ).mapNotNull { prayer ->
        try {
            prayer.type to LocalTime.parse(prayer.time, TIME_FORMAT)
        } catch (_: DateTimeParseException) {
            null
        }
    }.toMap()

    private fun occurrencesOf(
        type: PrayerTypes,
        times: Map<PrayerTypes, LocalTime>,
        now: ZonedDateTime
    ): List<EventOccurrence> {
        val time = times[type] ?: return emptyList()
        val today = now.toLocalDate()
        return DAY_OFFSETS.map { offset ->
            EventOccurrence(type, today.plusDays(offset).atTime(time).atZone(now.zone))
        }
    }
}
