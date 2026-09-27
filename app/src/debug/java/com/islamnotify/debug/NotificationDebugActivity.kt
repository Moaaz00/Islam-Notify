package com.islamnotify.debug

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.islamnotify.R
import com.islamnotify.calendar.domain.CalendarRepository
import com.islamnotify.common.AppUtils.getLocalizedContext
import com.islamnotify.notification.data.PrayerNotificationRenderer
import com.islamnotify.notification.domain.NotificationWork
import com.islamnotify.notification.domain.PrayerNotificationCalculator
import com.islamnotify.notification.util.NotificationUtils
import com.islamnotify.prayer_times.domain.model.PrayerConfig
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import com.islamnotify.prayer_times.domain.model.nextEventTypes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * Debug builds only. Posts the prayer notification as it would look at a chosen moment, so every
 * case can be checked without waiting for real prayer times. Also driven from adb for tests:
 * `adb shell am start -n com.islamnotify/.debug.NotificationDebugActivity -e scenario <key>`.
 */
@AndroidEntryPoint
class NotificationDebugActivity : AppCompatActivity() {

    @Inject lateinit var notificationWork: NotificationWork
    @Inject lateinit var calendarRepository: CalendarRepository

    private data class Scenario(
        val key: String,
        val label: String,
        val time: String,
        val date: LocalDate = LocalDate.now(),
        val events: List<PrayerTypes> = ALL_EVENTS,
        val times: Map<PrayerTypes, LocalTime> = CAIRO,
        val city: String = "Cairo",
        val longestDate: Boolean = false
    )

    private val scenarios = listOf(
        Scenario("midday", "12:50 · Asr next, its iqama after", "12:50"),
        Scenario("iqama", "15:40 · Iqama hero, Asr row bold", "15:40"),
        Scenario("duha", "06:10 · Duha hero, Dhuhr row bold", "06:10"),
        Scenario("after_isha", "21:00 · Midnight hero, tomorrow's table", "21:00"),
        Scenario("past_midnight", "00:30 · Last third hero", "00:30"),
        Scenario(
            "friday", "Friday 10:00 · Jumu'ah", "10:00",
            date = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
        ),
        Scenario("prayers_only", "21:00 · Prayers only → tomorrow's Fajr", "21:00", events = PRAYERS_ONLY),
        Scenario(
            "longest", "18:05 · Longest names, city and date", "18:05",
            city = "Sheikh Zayed City, Giza Governorate", longestDate = true
        ),
        Scenario(
            "long_countdown", "Long night · 13-hour countdown", "18:00:01",
            events = PRAYERS_ONLY, times = LONG_NIGHT
        ),
        Scenario("no_city", "12:50 · No city", "12:50", city = "")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.getStringExtra(EXTRA_SCENARIO)?.let { key ->
            lifecycleScope.launch {
                if (key == REAL) postReal() else scenarios.find { it.key == key }?.let { post(it) }
                finish()
            }
            return
        }

        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    Column(
                        Modifier
                            .safeDrawingPadding()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Prayer notification", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Posts the notification as it would look at each moment (sample Cairo " +
                                "times). Pull down the shade to see it.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = { lifecycleScope.launch { postReal() } },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Real data, now") }
                        scenarios.forEach { scenario ->
                            OutlinedButton(
                                onClick = { lifecycleScope.launch { post(scenario) } },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(scenario.label) }
                        }
                    }
                }
            }
        }
    }

    private suspend fun postReal() {
        val result = notificationWork.startWork()
        Log.d(TAG, "real: $result")
        Toast.makeText(this, "Real notification: ${result::class.simpleName}", Toast.LENGTH_SHORT).show()
    }

    private suspend fun post(scenario: Scenario) {
        val now = scenario.date.atTime(LocalTime.parse(scenario.time)).atZone(ZoneId.systemDefault())
        val state = PrayerNotificationCalculator.compute(scenario.times, scenario.events, now)
        if (state == null) {
            Log.e(TAG, "${scenario.key}: no state")
            return
        }
        val notification = PrayerNotificationRenderer(this).build(
            state = state,
            nowMillis = now.toInstant().toEpochMilli(),
            hijriDate = if (scenario.longestDate) longestHijriDate() else todayHijriDate(),
            locationName = scenario.city
        )
        try {
            NotificationManagerCompat.from(this).notify(NotificationUtils.PRAYER_NOTIFICATION_ID, notification)
            Log.d(TAG, "${scenario.key}: posted hero=${state.hero.type} following=${state.following.type} progress=${state.progress}")
        } catch (e: SecurityException) {
            Log.e(TAG, "${scenario.key}: notification permission missing", e)
            Toast.makeText(this, "Notification permission missing", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun todayHijriDate(): String {
        val date = calendarRepository.getHijriDate()
        return "${date.formatedDayOfMonth} ${date.monthName} ${date.formatedYear}"
    }

    /** Day 29 of the month with the longest name in the app's language. */
    private fun longestHijriDate(): String {
        val localized = getLocalizedContext()
        val numbers = NumberFormat.getInstance(localized.resources.configuration.locales[0])
            .apply { isGroupingUsed = false }
        val month = localized.resources.getStringArray(R.array.hijri_months_names).maxBy { it.length }
        return "${numbers.format(29)} $month ${numbers.format(1448)}"
    }

    private companion object {
        const val TAG = "NotificationDebug"
        const val EXTRA_SCENARIO = "scenario"
        const val REAL = "real"

        val ALL_EVENTS = PrayerConfig().nextEventTypes()
        val PRAYERS_ONLY = PrayerConfig(
            showNextIqama = false, showNextLastThird = false, showNextMidnight = false,
            showNextDuha = false, showNextSunrise = false
        ).nextEventTypes()

        fun times(vararg pairs: Pair<PrayerTypes, String>) =
            pairs.associate { (type, time) -> type to LocalTime.parse(time) }

        val CAIRO = times(
            PrayerTypes.FAJR to "04:30", PrayerTypes.IQAMA_FAJR to "04:55",
            PrayerTypes.SUNRISE to "06:00", PrayerTypes.DUHA to "06:20",
            PrayerTypes.ZUHR to "12:00", PrayerTypes.IQAMA_ZUHR to "12:20",
            PrayerTypes.ASR to "15:30", PrayerTypes.IQAMA_ASR to "15:50",
            PrayerTypes.SUNSET to "18:00", PrayerTypes.IQAMA_SUNSET to "18:15",
            PrayerTypes.ISHA to "19:30", PrayerTypes.IQAMA_ISHA to "19:50",
            PrayerTypes.MIDNIGHT to "23:15", PrayerTypes.LAST_THIRD to "01:30"
        )

        // A northern winter: Isha 18:00, Fajr 07:59 the next morning.
        val LONG_NIGHT = times(
            PrayerTypes.FAJR to "07:59", PrayerTypes.SUNRISE to "09:40",
            PrayerTypes.ZUHR to "12:10", PrayerTypes.ASR to "13:20",
            PrayerTypes.SUNSET to "14:45", PrayerTypes.ISHA to "18:00"
        )
    }
}
