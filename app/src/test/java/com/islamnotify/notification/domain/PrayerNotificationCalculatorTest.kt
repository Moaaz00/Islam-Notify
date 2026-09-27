package com.islamnotify.notification.domain

import com.islamnotify.prayer_times.domain.model.PrayerConfig
import com.islamnotify.prayer_times.domain.model.PrayerData
import com.islamnotify.prayer_times.domain.model.PrayerEntities
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import com.islamnotify.prayer_times.domain.model.PrayerTypes.ASR
import com.islamnotify.prayer_times.domain.model.PrayerTypes.DUHA
import com.islamnotify.prayer_times.domain.model.PrayerTypes.FAJR
import com.islamnotify.prayer_times.domain.model.PrayerTypes.IQAMA_ASR
import com.islamnotify.prayer_times.domain.model.PrayerTypes.IQAMA_ISHA
import com.islamnotify.prayer_times.domain.model.PrayerTypes.IQAMA_ZUHR
import com.islamnotify.prayer_times.domain.model.PrayerTypes.ISHA
import com.islamnotify.prayer_times.domain.model.PrayerTypes.LAST_THIRD
import com.islamnotify.prayer_times.domain.model.PrayerTypes.MIDNIGHT
import com.islamnotify.prayer_times.domain.model.PrayerTypes.SUNRISE
import com.islamnotify.prayer_times.domain.model.PrayerTypes.SUNSET
import com.islamnotify.prayer_times.domain.model.PrayerTypes.ZUHR
import com.islamnotify.prayer_times.domain.model.nextEventTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class PrayerNotificationCalculatorTest {

    private val zone = ZoneId.of("Africa/Cairo")
    private val day = LocalDate.of(2026, 1, 13) // a Tuesday, no DST in effect

    private val cairo = times(
        FAJR to "04:30", PrayerTypes.IQAMA_FAJR to "04:55", SUNRISE to "06:00", DUHA to "06:20",
        ZUHR to "12:00", IQAMA_ZUHR to "12:20", ASR to "15:30", IQAMA_ASR to "15:50",
        SUNSET to "18:00", PrayerTypes.IQAMA_SUNSET to "18:15", ISHA to "19:30",
        IQAMA_ISHA to "19:50", MIDNIGHT to "23:15", LAST_THIRD to "01:30"
    )

    private val allEvents = PrayerConfig().nextEventTypes()
    private val prayersOnly = PrayerConfig(
        showNextIqama = false, showNextLastThird = false, showNextMidnight = false,
        showNextDuha = false, showNextSunrise = false
    ).nextEventTypes()

    private fun times(vararg pairs: Pair<PrayerTypes, String>) =
        pairs.associate { (type, time) -> type to LocalTime.parse(time) }

    private fun at(time: String, date: LocalDate = day): ZonedDateTime =
        LocalDateTime.of(date, LocalTime.parse(time)).atZone(zone)

    private fun compute(
        now: ZonedDateTime,
        events: List<PrayerTypes> = allEvents,
        t: Map<PrayerTypes, LocalTime> = cairo
    ) = PrayerNotificationCalculator.compute(t, events, now) ?: throw AssertionError("no state at $now")

    private fun PrayerNotificationState.stateOf(type: PrayerTypes) = rows.single { it.type == type }.state

    @Test
    fun `midday - next prayer, its iqama after it, bar from the previous event`() {
        val s = compute(at("12:50"))

        assertEquals(ASR, s.hero.type)
        assertEquals(at("15:30"), s.hero.dateTime)
        assertEquals(IQAMA_ASR, s.following.type)
        assertEquals(IQAMA_ZUHR, s.previous.type)
        // 30 of 190 minutes passed.
        assertEquals(30 * 1000 / 190, s.progress)

        assertEquals(PrayerNotificationCalculator.TABLE_TYPES, s.rows.map { it.type })
        assertEquals(RowState.PAST, s.stateOf(FAJR))
        assertEquals(RowState.PAST, s.stateOf(SUNRISE))
        assertEquals(RowState.PAST, s.stateOf(ZUHR))
        assertEquals(RowState.NEXT, s.stateOf(ASR))
        assertEquals(RowState.UPCOMING, s.stateOf(SUNSET))
        assertEquals(RowState.UPCOMING, s.stateOf(ISHA))
        s.rows.forEach { assertEquals(day, it.dateTime.toLocalDate()) }
    }

    @Test
    fun `iqama hero - its prayer's row is bold although the prayer has passed`() {
        val s = compute(at("15:40"))

        assertEquals(IQAMA_ASR, s.hero.type)
        assertEquals(SUNSET, s.following.type)
        assertEquals(ASR, s.previous.type)
        assertEquals(RowState.NEXT, s.stateOf(ASR))
        assertEquals(RowState.PAST, s.stateOf(ZUHR))
        assertEquals(RowState.UPCOMING, s.stateOf(SUNSET))
    }

    @Test
    fun `isha iqama hero - table still shows tonight`() {
        val s = compute(at("19:40"))

        assertEquals(IQAMA_ISHA, s.hero.type)
        assertEquals(RowState.NEXT, s.stateOf(ISHA))
        s.rows.forEach { assertEquals(day, it.dateTime.toLocalDate()) }
    }

    @Test
    fun `duha hero - dhuhr row is bold, sunrise faded`() {
        val s = compute(at("06:10"))

        assertEquals(DUHA, s.hero.type)
        assertEquals(ZUHR, s.following.type)
        assertEquals(SUNRISE, s.previous.type)
        assertEquals(RowState.PAST, s.stateOf(SUNRISE))
        assertEquals(RowState.NEXT, s.stateOf(ZUHR))
    }

    @Test
    fun `after isha - midnight hero, last third next, table moves to tomorrow with fajr bold`() {
        val s = compute(at("21:00"))

        assertEquals(MIDNIGHT, s.hero.type)
        assertEquals(at("23:15"), s.hero.dateTime)
        assertEquals(LAST_THIRD, s.following.type)
        assertEquals(at("01:30", day.plusDays(1)), s.following.dateTime)
        assertEquals(IQAMA_ISHA, s.previous.type)

        assertEquals(RowState.NEXT, s.stateOf(FAJR))
        PrayerNotificationCalculator.TABLE_TYPES.drop(1).forEach {
            assertEquals(RowState.UPCOMING, s.stateOf(it))
        }
        s.rows.forEach { assertEquals(day.plusDays(1), it.dateTime.toLocalDate()) }
    }

    @Test
    fun `past midnight - last third hero belongs to tonight, bar starts at midnight yesterday`() {
        val s = compute(at("00:30"))

        assertEquals(LAST_THIRD, s.hero.type)
        assertEquals(at("01:30"), s.hero.dateTime)
        assertEquals(FAJR, s.following.type)
        assertEquals(MIDNIGHT, s.previous.type)
        assertEquals(at("23:15", day.minusDays(1)), s.previous.dateTime)
        // 75 of 135 minutes passed.
        assertEquals(75 * 1000 / 135, s.progress)
        assertEquals(RowState.NEXT, s.stateOf(FAJR))
        s.rows.forEach { assertEquals(day, it.dateTime.toLocalDate()) }
    }

    @Test
    fun `prayers only - after isha the hero is tomorrow's fajr and the line is tomorrow's dhuhr`() {
        val s = compute(at("21:00"), prayersOnly)

        assertEquals(FAJR, s.hero.type)
        assertEquals(at("04:30", day.plusDays(1)), s.hero.dateTime)
        assertEquals(ZUHR, s.following.type)
        assertEquals(ISHA, s.previous.type)
        assertEquals(RowState.NEXT, s.stateOf(FAJR))
        // Sunrise isn't counted down to but keeps its table row.
        assertEquals(RowState.UPCOMING, s.stateOf(SUNRISE))
    }

    @Test
    fun `last event of the day - the line shows tomorrow's first event`() {
        val events = PrayerConfig(
            showNextLastThird = false, showNextMidnight = false,
            showNextDuha = false, showNextSunrise = false
        ).nextEventTypes()
        val s = compute(at("19:40"), events)

        assertEquals(IQAMA_ISHA, s.hero.type)
        assertEquals(FAJR, s.following.type)
        assertEquals(at("04:30", day.plusDays(1)), s.following.dateTime)
    }

    @Test
    fun `event exactly now counts as passed, bar starts empty`() {
        val s = compute(at("15:30"))

        assertEquals(IQAMA_ASR, s.hero.type)
        assertEquals(ASR, s.previous.type)
        assertEquals(0, s.progress)
    }

    @Test
    fun `bar is full just before the event`() {
        val s = compute(at("15:30").minusSeconds(1))

        assertEquals(ASR, s.hero.type)
        assertEquals(999, s.progress)
    }

    @Test
    fun `iqama at the same minute as its prayer - prayer wins, line skips to the next time`() {
        val t = cairo + (IQAMA_ASR to LocalTime.parse("15:30"))
        val s = compute(at("15:00"), t = t)

        assertEquals(ASR, s.hero.type)
        assertEquals(SUNSET, s.following.type)
    }

    @Test
    fun `isha after midnight (high latitude) - still tonight's isha, in order`() {
        val t = times(
            FAJR to "02:40", SUNRISE to "04:00", ZUHR to "13:00",
            ASR to "17:30", SUNSET to "22:00", ISHA to "00:10"
        )
        val s = compute(at("23:00"), prayersOnly, t)

        assertEquals(ISHA, s.hero.type)
        assertEquals(at("00:10", day.plusDays(1)), s.hero.dateTime)
        assertEquals(RowState.NEXT, s.stateOf(ISHA))
        assertEquals(RowState.PAST, s.stateOf(SUNSET))
        assertEquals(at("02:40"), s.rows.first().dateTime)
        assertEquals(at("00:10", day.plusDays(1)), s.rows.last().dateTime)
    }

    @Test
    fun `daylight saving day - no crash, hero is the next real instant`() {
        // Egypt moved clocks from 00:00 to 01:00 on Friday 24 April 2026.
        val dstDay = LocalDate.of(2026, 4, 24)
        val t = cairo + (LAST_THIRD to LocalTime.parse("00:30"))
        val s = compute(at("23:30", dstDay.minusDays(1)), t = t)

        assertEquals(LAST_THIRD, s.hero.type)
        assertEquals(dstDay, s.hero.dateTime.toLocalDate())
        assertTrue(s.hero.dateTime.isAfter(at("23:30", dstDay.minusDays(1))))
    }

    @Test
    fun `missing times - no state instead of a wrong one`() {
        assertNull(PrayerNotificationCalculator.compute(emptyMap(), allEvents, at("12:00")))

        val noIsha = cairo - ISHA
        assertNull(PrayerNotificationCalculator.compute(noIsha, prayersOnly, at("12:00")))
    }

    @Test
    fun `event times are read from the stored entities, bad values skipped`() {
        fun p(type: PrayerTypes, time: String) = PrayerData(type = type, time = time)
        val entities = PrayerEntities(
            fajr = p(FAJR, "04:30"), iqamaFajr = p(PrayerTypes.IQAMA_FAJR, "04:55"),
            sunrise = p(SUNRISE, "06:00"), duha = p(DUHA, "06:20"), zuhr = p(ZUHR, "12:00"),
            iqamaZuhr = p(IQAMA_ZUHR, "12:20"), asr = p(ASR, "15:30"),
            iqamaAsr = p(IQAMA_ASR, "15:50"), sunset = p(SUNSET, "18:00"),
            iqamaSunset = p(PrayerTypes.IQAMA_SUNSET, "18:15"), isha = p(ISHA, "19:30"),
            iqamaIsha = p(IQAMA_ISHA, "19:50"), midnight = p(MIDNIGHT, ""),
            lastThird = p(LAST_THIRD, "01:30")
        )

        val parsed = PrayerNotificationCalculator.eventTimes(entities)

        assertEquals(cairo - MIDNIGHT, parsed)
    }

    @Test
    fun `event order matches the settings and the alarms`() {
        assertEquals(
            listOf(
                FAJR, ZUHR, ASR, SUNSET, ISHA, PrayerTypes.IQAMA_FAJR, IQAMA_ZUHR, IQAMA_ASR,
                PrayerTypes.IQAMA_SUNSET, IQAMA_ISHA, LAST_THIRD, MIDNIGHT, DUHA, SUNRISE
            ),
            allEvents
        )
        assertEquals(listOf(FAJR, ZUHR, ASR, SUNSET, ISHA), prayersOnly)
    }

    @Test
    fun `sweep - every minute of two days gives a consistent state`() {
        var now = at("00:00")
        val end = now.plusDays(2)
        while (now.isBefore(end)) {
            for (events in listOf(allEvents, prayersOnly)) {
                val s = compute(now, events)
                val nowMillis = now.toInstant().toEpochMilli()
                assertTrue("order at $now", s.previous.millis <= nowMillis && nowMillis < s.hero.millis)
                assertTrue("following at $now", s.hero.millis < s.following.millis)
                assertTrue("progress at $now", s.progress in 0..PrayerNotificationCalculator.PROGRESS_MAX)
                assertEquals(1, s.rows.count { it.state == RowState.NEXT })
                assertEquals(PrayerNotificationCalculator.rowOf(s.hero.type), s.rows.single { it.state == RowState.NEXT }.type)
                // Rows are in time order and within one prayer day.
                s.rows.zipWithNext { a, b -> assertTrue("rows at $now", a.dateTime.isBefore(b.dateTime)) }
                // Nothing counted down to is hidden between now and the hero.
                assertTrue("skipped an event at $now", events.none { type ->
                    val t = cairo.getValue(type)
                    (-1L..1L).any { d ->
                        val m = now.toLocalDate().plusDays(d).atTime(t).atZone(zone).toInstant().toEpochMilli()
                        m in (nowMillis + 1) until s.hero.millis
                    }
                })
            }
            now = now.plusMinutes(1)
        }
    }
}
