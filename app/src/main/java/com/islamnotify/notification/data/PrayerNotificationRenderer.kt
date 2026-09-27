package com.islamnotify.notification.data

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.SystemClock
import android.text.SpannableString
import android.text.Spanned
import android.text.format.DateFormat
import android.text.style.StyleSpan
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.text.layoutDirection
import com.islamnotify.R
import com.islamnotify.common.AppUtils
import com.islamnotify.common.AppUtils.getLocalizedContext
import com.islamnotify.main.presentation.MainActivity
import com.islamnotify.notification.domain.EventOccurrence
import com.islamnotify.notification.domain.PrayerNotificationCalculator
import com.islamnotify.notification.domain.PrayerNotificationState
import com.islamnotify.notification.domain.RowState
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.util.Locale

/**
 * Builds the prayer notification (Figma "Final Version") from a [PrayerNotificationState].
 *
 * Collapsed: ① Hijri date • city, ② "Asr  2:34:12".
 * Expanded: ⑤ Hijri date ↔ city, ④ hero, ⑥ progress bar, ⑦ 2 × 3 table. The system header
 * above shows only the app name.
 *
 * Every line stays one line (the notification has a fixed height too): times and the countdown
 * are never cut; names are cut with "…", and in the date/city lines the city goes first.
 */
class PrayerNotificationRenderer(private val context: Context) {

    /**
     * @param nowMillis the moment [state] was computed for; the countdown is based on it.
     * @param hijriDate e.g. "22 Rajab 1448", already in the app's language.
     * @param locationName the city, or blank when unknown.
     */
    fun build(
        state: PrayerNotificationState,
        nowMillis: Long,
        hijriDate: String,
        locationName: String
    ): Notification {
        // Android draws the notification in the phone's language, but our texts follow the
        // app's own language (it can differ), and so must the layout direction.
        val localized = context.getLocalizedContext()
        val locale: Locale = localized.resources.configuration.locales[0]
        val layoutDirection = locale.layoutDirection
        val times = TimeFormats(locale, DateFormat.is24HourFormat(context))

        val heroName = eventName(localized, state.hero.type, state.hero.dateTime)

        val countdownBase = SystemClock.elapsedRealtime() + (state.hero.millis - nowMillis)

        // Collapsed
        val collapsed = RemoteViews(context.packageName, R.layout.notification_prayer_collapsed)
        collapsed.setInt(R.id.CollapsedRoot, "setLayoutDirection", layoutDirection)
        collapsed.setTextViewText(R.id.ContextDate, hijriDate)
        collapsed.setTextViewText(R.id.ContextCity, locationName)
        val cityVisibility = if (locationName.isBlank()) View.GONE else View.VISIBLE
        collapsed.setViewVisibility(R.id.ContextDot, cityVisibility)
        collapsed.setViewVisibility(R.id.ContextCity, cityVisibility)
        bindHero(collapsed, heroName, countdownBase)

        // Expanded
        val expanded = RemoteViews(context.packageName, R.layout.notification_prayer_expanded)
        expanded.setInt(R.id.ExpandedRoot, "setLayoutDirection", layoutDirection)
        bindHero(expanded, heroName, countdownBase)

        expanded.setTextViewText(R.id.ExpandedDate, hijriDate)
        expanded.setTextViewText(R.id.ExpandedCity, locationName)
        expanded.setProgressBar(
            R.id.Progress, PrayerNotificationCalculator.PROGRESS_MAX, state.progress, false
        )
        bindTable(expanded, state, localized, times)

        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, AppUtils.PRAYER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setColor(ContextCompat.getColor(context, R.color.light_green))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(collapsed)
            .setCustomBigContentView(expanded)
            // Plain-text copy for places that can't show custom views (watches, screen readers).
            .setContentTitle(heroName)
            .setContentText(times.withPeriod(state.hero))
            // The post time next to the app name looked like a prayer time.
            .setShowWhen(false)
            .setWhen(nowMillis)
            .setAutoCancel(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(openApp)
            .setAllowSystemGeneratedContextualActions(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            // Sole member of its own group, no summary posted: keeps the system's
            // auto-bundling from folding this into a group with other notifications.
            .setGroup(AppUtils.GROUP_PRAYER)
            .build()
    }

    private fun bindHero(views: RemoteViews, name: String, countdownBase: Long) {
        views.setTextViewText(R.id.HeroName, name)
        views.setChronometerCountDown(R.id.HeroCountdown, true)
        views.setChronometer(R.id.HeroCountdown, countdownBase, null, true)
    }

    private fun bindTable(
        views: RemoteViews,
        state: PrayerNotificationState,
        localized: Context,
        times: TimeFormats
    ) {
        val names = state.rows.map { eventName(localized, it.type, it.dateTime) }
        val values = state.rows.map { times.short(it.dateTime) }

        state.rows.forEachIndexed { i, row ->
            val cell = TABLE_CELLS[i]
            // Passed rows use the faded twins (see the layout); the next prayer is bold.
            val past = row.state == RowState.PAST
            val style: (String) -> CharSequence =
                if (row.state == RowState.NEXT) { text -> bold(text) } else { text -> text }
            views.setTextViewText(if (past) cell.namePast else cell.name, style(names[i]))
            views.setTextViewText(if (past) cell.timePast else cell.time, style(values[i]))
            views.setViewVisibility(cell.name, if (past) View.GONE else View.VISIBLE)
            views.setViewVisibility(cell.time, if (past) View.GONE else View.VISIBLE)
            views.setViewVisibility(cell.namePast, if (past) View.VISIBLE else View.GONE)
            views.setViewVisibility(cell.timePast, if (past) View.VISIBLE else View.GONE)
        }
    }

    private fun bold(text: String): CharSequence = SpannableString(text).apply {
        setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    /** The event's name in the app's language; Dhuhr is Jumu'ah on Fridays. */
    private fun eventName(localized: Context, type: PrayerTypes, dateTime: ZonedDateTime): String {
        val name = when (type) {
            PrayerTypes.FAJR -> R.string.fajr_name
            PrayerTypes.IQAMA_FAJR -> R.string.iqama_fajr_name
            PrayerTypes.SUNRISE -> R.string.sunrise_name
            PrayerTypes.DUHA -> R.string.duha_name
            PrayerTypes.ZUHR ->
                if (dateTime.dayOfWeek == DayOfWeek.FRIDAY) R.string.jummah_name
                else R.string.zuhr_name
            PrayerTypes.IQAMA_ZUHR -> R.string.iqama_zuhr_name
            PrayerTypes.ASR -> R.string.asr_name
            PrayerTypes.IQAMA_ASR -> R.string.iqama_asr_name
            PrayerTypes.SUNSET -> R.string.sunset_name
            PrayerTypes.IQAMA_SUNSET -> R.string.iqama_sunset_name
            PrayerTypes.ISHA -> R.string.isha_name
            PrayerTypes.IQAMA_ISHA -> R.string.iqama_isha_name
            PrayerTypes.MIDNIGHT -> R.string.midnight_name
            PrayerTypes.LAST_THIRD -> R.string.last_third_name
            PrayerTypes.EMPTY -> return ""
        }
        return localized.getString(name)
    }

    /** Hour formats following the phone's 12/24-hour setting, digits in the app's language. */
    private class TimeFormats(locale: Locale, is24Hour: Boolean) {
        private val decimalStyle = DecimalStyle.of(locale)
        private val shortFormat =
            DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm", locale)
                .withDecimalStyle(decimalStyle)
        private val periodFormat =
            DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm a", locale)
                .withDecimalStyle(decimalStyle)

        /** Table: "3:24". */
        fun short(dateTime: ZonedDateTime): String = shortFormat.format(dateTime)

        /** Plain-text copy: "3:44 PM". */
        fun withPeriod(event: EventOccurrence): String = periodFormat.format(event.dateTime)
    }

    /** One table cell's views: normal and faded ("Past") name and time. */
    private data class TableCell(val name: Int, val time: Int, val namePast: Int, val timePast: Int)

    private companion object {
        // Fajr, Sunrise, Dhuhr | Asr, Maghrib, Isha.
        val TABLE_CELLS = listOf(
            TableCell(R.id.T2Name0, R.id.T2Time0, R.id.T2Name0Past, R.id.T2Time0Past),
            TableCell(R.id.T2Name1, R.id.T2Time1, R.id.T2Name1Past, R.id.T2Time1Past),
            TableCell(R.id.T2Name2, R.id.T2Time2, R.id.T2Name2Past, R.id.T2Time2Past),
            TableCell(R.id.T2Name3, R.id.T2Time3, R.id.T2Name3Past, R.id.T2Time3Past),
            TableCell(R.id.T2Name4, R.id.T2Time4, R.id.T2Name4Past, R.id.T2Time4Past),
            TableCell(R.id.T2Name5, R.id.T2Time5, R.id.T2Name5Past, R.id.T2Time5Past)
        )
    }
}
