package com.islamnotify.events.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.islamnotify.R
import com.islamnotify.android.AlarmReceiver
import com.islamnotify.calendar.domain.CalendarRepository
import com.islamnotify.common.AppUtils
import com.islamnotify.common.AppUtils.getLocalizedContext
import com.islamnotify.common.domain.CrashReporter
import com.islamnotify.events.domain.EventFlags
import com.islamnotify.events.domain.EventsData
import com.islamnotify.events.util.EventsUtils
import com.islamnotify.prayer_times.domain.LocationPrayerResult
import com.islamnotify.prayer_times.domain.PrayerDataUseCase
import com.islamnotify.prayer_times.domain.model.PrayerEntities
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.util.concurrent.TimeUnit

@HiltWorker
class EventsWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    val prayerDataUseCase: PrayerDataUseCase,
    val alarmManager: AlarmManager,
    val dataStore: EventsDataStore,
    val calendarRepository: CalendarRepository,
    val crashReporter: CrashReporter
) : CoroutineWorker(context, workerParams) {


    override suspend fun doWork(): Result {
        try {
            scheduleEventsAlarm()
            scheduleMidnightCheck()
            Log.d("EventsFlow", "doWork: worker success")
            return Result.success()
        } catch (e: Exception) {
            Log.e("EventsFlow", "doWork: ", e)
            crashReporter.recordNonFatal(e)
            return Result.failure()
        }
    }


    private fun scheduleMidnightCheck(){
        val midnight = AppUtils.getMidnightTomorrowPlusSeconds(3)
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = EventsUtils.REQUEST_EVENTS_WORKER_ACTION

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            EventsUtils.REQUEST_EVENTS_WORKER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // fallback work manager for midnight schedule
        if (midnight > System.currentTimeMillis()) {
            val oneTimeWorkRequest = OneTimeWorkRequestBuilder<EventsWorker>()
                .setInitialDelay(
                    midnight - System.currentTimeMillis() + 10_000,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                EventsUtils.MIDNIGHT_WORK_REQUEST_TAG,
                ExistingWorkPolicy.REPLACE,
                oneTimeWorkRequest
            )
        }

        val canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canScheduleExactAlarms) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                midnight,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                midnight,
                pendingIntent
            )
        }

        Log.d("EventsFlow", "scheduling midnight worker request")
    }


    suspend fun scheduleEventsAlarm() {
        val eventsList = mutableListOf<EventsData>()

        val date: LocalDate = LocalDate.now()
        val dateModel = calendarRepository.getHijriDate()
        val hDayOfMonth = dateModel.dayOfMonth
        val hMonth = dateModel.monthNumber
        val hYear = dateModel.year

        val isRamadan = (hDayOfMonth == lastDayOfHijriMonth(hYear,8) && hMonth == 8) || hMonth == 9
        val isFirstEid = hDayOfMonth == lastDayOfHijriMonth(hYear,9) && hMonth == 9
        val isSecondEid = (hDayOfMonth in 9..12) && hMonth == 12
        val isAcceptableFasting = !isRamadan && !isFirstEid && !isSecondEid

        val eventFlags: EventFlags = dataStore.getEventsFlags().first()

        val prayerData: PrayerEntities? =
            when (val result = prayerDataUseCase.getPrayerDataWithLastLocation()) {
                is LocationPrayerResult.PrayerError -> {
                    null
                }

                is LocationPrayerResult.LocationError -> {
                    null
                }

                is LocationPrayerResult.LocationStale -> {
                    result.prayerData
                }

                is LocationPrayerResult.Success -> {
                    result.prayerData
                }

                else -> {
                    null
                }
            }

        val sunrise: Long = prayerData?.sunrise?.millis ?: getTodayTimeInMillis(6)
        val sunset: Long = prayerData?.sunset?.millis ?: getTodayTimeInMillis(19)
        val isha: Long = prayerData?.isha?.millis ?: getTodayTimeInMillis(20)


        // monday fasting
        eventsList.addEvent(
            titleResId = R.string.monday_fasting_title,
            subtitleResId = R.string.monday_thursday_fasting_subtitle,
            requestCode = EventsUtils.MONDAY_REQUEST_CODE_NOTIFICATION_ID,
            isToday = date.dayOfWeek == DayOfWeek.SUNDAY && isAcceptableFasting,
            triggerTime = isha,
            isEnabled = eventFlags.mondayFasting
        )

        // thursday fasting
        eventsList.addEvent(
            titleResId = R.string.thursday_fasting_title,
            subtitleResId = R.string.monday_thursday_fasting_subtitle,
            requestCode = EventsUtils.THURSDAY_REQUEST_CODE_NOTIFICATION_ID,
            isToday = date.dayOfWeek == DayOfWeek.WEDNESDAY && isAcceptableFasting,
            triggerTime = isha,
            isEnabled = eventFlags.thursdayFasting
        )

        //white days fasting
        eventsList.addEvent(
            titleResId = R.string.white_days_fasting_title,
            subtitleResId = R.string.white_days_fasting_subtitle,
            requestCode = EventsUtils.WHITE_DAYS_REQUEST_CODE_NOTIFICATION_ID,
            isToday = hDayOfMonth == 12 && isAcceptableFasting,
            triggerTime = isha,
            isEnabled = eventFlags.whiteDaysFasting
        )

        //arfa fasting
        eventsList.addEvent(
            titleResId = R.string.arfa_fasting_title,
            subtitleResId = R.string.arfa_fasting_subtitle,
            requestCode = EventsUtils.ARAFAH_REQUEST_CODE_NOTIFICATION_ID,
            isToday = hDayOfMonth == 8 && hMonth == 12,
            triggerTime = isha,
            isEnabled = eventFlags.arafaFasting
        )

        // tasua fasting
        eventsList.addEvent(
            titleResId = R.string.tasua_fasting_title,
            subtitleResId = R.string.tasua_fasting_subtitle,
            requestCode = EventsUtils.TASUA_REQUEST_CODE_NOTIFICATION_ID,
            isToday = hDayOfMonth == 8 && hMonth == 1,
            triggerTime = isha,
            isEnabled = eventFlags.tasuaFasting
        )

        //ashora fasting
        eventsList.addEvent(
            titleResId = R.string.ashora_fasting_title,
            subtitleResId = R.string.ashora_fasting_subtitle,
            requestCode = EventsUtils.ASHORA_REQUEST_CODE_NOTIFICATION_ID,
            isToday = hDayOfMonth == 9 && hMonth == 1,
            triggerTime = isha,
            isEnabled = eventFlags.ashoraFasting
        )

        // shawwal fasting
        eventsList.addEvent(
            titleResId = R.string.shawwal_fasting_title,
            subtitleResId = R.string.shawwal_fasting_subtitle,
            requestCode = EventsUtils.SHAWWAL_REQUEST_CODE_NOTIFICATION_ID,
            isToday = hDayOfMonth == 1 && hMonth == 10,
            triggerTime = isha,
            isEnabled = eventFlags.shawwalFasting
        )

        // ramadan
        eventsList.addEvent(
            titleResId = R.string.ramadan_event_title,
            subtitleResId = R.string.ramadan_event_subtitle,
            requestCode = EventsUtils.RAMADAN_REQUEST_CODE_NOTIFICATION_ID,
            isToday = hDayOfMonth == lastDayOfHijriMonth(hYear,8) && hMonth == 8,
            triggerTime = sunset,
            isEnabled = eventFlags.ramadanEvent
        )


        // ramadan last 10 days
        eventsList.addEvent(
            titleResId = R.string.ramadan_last_10_days_event_title,
            subtitleResId = R.string.ramadan_last_10_days_event_subtitle,
            requestCode = EventsUtils.RAMADAN_LAST_10_DAYS_REQUEST_CODE_NOTIFICATION_ID,
            isToday = hDayOfMonth == 19 && hMonth == 9,
            triggerTime = sunset,
            isEnabled = eventFlags.ramdanLast10DaysEvent
        )

        // first 10 days of dhu al hijjah
        eventsList.addEvent(
            titleResId = R.string.dhu_al_hijjah_first_10_days_event_title,
            subtitleResId = R.string.dhu_al_hijjah_first_10_days_event_subtitle,
            requestCode = EventsUtils.DHU_AL_HIJJA_FIRST_10_DAYS_REQUEST_CODE_NOTIFICATION_ID,
            isToday = hDayOfMonth == lastDayOfHijriMonth(hYear,11) && hMonth == 11,
            triggerTime = sunset,
            isEnabled = eventFlags.dhuAlHijjahFirst10DaysEvent
        )

        // eid al fitr
        eventsList.addEvent(
            titleResId = R.string.eid_al_fitr_event_title,
            subtitleResId = R.string.eids_event_subtitle,
            requestCode = EventsUtils.EID_AL_FITR_REQUEST_CODE_NOTIFICATION_ID,
            isToday = hDayOfMonth == lastDayOfHijriMonth(hYear,9) && hMonth == 9,
            triggerTime = isha,
            isEnabled = eventFlags.eidAlFitrEvent
        )

        // eid al adha
        eventsList.addEvent(
            titleResId = R.string.eid_al_adha_event_title,
            subtitleResId = R.string.eids_event_subtitle,
            requestCode = EventsUtils.EID_AL_ADHA_REQUEST_CODE_NOTIFICATION_ID,
            isToday = hDayOfMonth == 9 && hMonth == 12,
            triggerTime = isha,
            isEnabled = eventFlags.eidAlAdhaEvent
        )

        // friday
        eventsList.addEvent(
            titleResId = R.string.friday_event_title,
            subtitleResId = R.string.friday_event_subtitle,
            requestCode = EventsUtils.FRIDAY_REQUEST_CODE_NOTIFICATION_ID,
            isToday = date.dayOfWeek == DayOfWeek.FRIDAY,
            triggerTime = sunrise,
            isEnabled = eventFlags.fridayEvent
        )

        // muharram sacred month
        eventsList.addEvent(
            titleResId = R.string.muharram_event_title,
            subtitleResId = R.string.sacred_months_event_subtitle,
            requestCode = EventsUtils.MUHARRAM_REQUEST_CODE_NOTIFICATION_ID,
            isToday = hDayOfMonth == lastDayOfHijriMonth(hYear,12) && hMonth == 12,
            triggerTime = sunset,
            isEnabled = eventFlags.muharramEvent
        )

        // rajab sacred month
        eventsList.addEvent(
            titleResId = R.string.rajab_event_title,
            subtitleResId = R.string.sacred_months_event_subtitle,
            requestCode = EventsUtils.RAJAB_REQUEST_CODE_NOTIFICATION_ID,
            isToday = hDayOfMonth == lastDayOfHijriMonth(hYear,6) && hMonth == 6,
            triggerTime = sunset,
            isEnabled = eventFlags.rajabEvent
        )

        // dhu al qadah sacred month
        eventsList.addEvent(
            titleResId = R.string.dhu_al_qadah_event_title,
            subtitleResId = R.string.sacred_months_event_subtitle,
            requestCode = EventsUtils.DHU_AL_QIDA_REQUEST_CODE_NOTIFICATION_ID,
            isToday = hDayOfMonth == lastDayOfHijriMonth(hYear,10) && hMonth == 10,
            triggerTime = sunset,
            isEnabled = eventFlags.dhuAlQidaEvent
        )

        // dhu al hijjah sacred month
        eventsList.addEvent(
            titleResId = R.string.dhu_al_hijjah_event_title,
            subtitleResId = R.string.sacred_months_event_subtitle,
            requestCode = EventsUtils.DHU_AL_HIJJA_REQUEST_CODE_NOTIFICATION_ID,
            isToday = hDayOfMonth == lastDayOfHijriMonth(hYear,11) && hMonth == 11,
            triggerTime = sunset,
            isEnabled = eventFlags.dhuAlHijjahEvent
        )

        eventsList.forEach { eventsData ->
            if (eventsData.isToday && eventsData.isEnabled && eventFlags.isAllEnabled) {
                scheduleAlarm(
                    title = eventsData.title,
                    subtitle = eventsData.subtitle,
                    requestCode = eventsData.requestCode,
                    triggerTime = eventsData.triggerTime
                )
            }

            if (eventsData.isToday && !eventsData.isEnabled){
                Log.d("EventsFlow", "scheduleEventsAlarm: event with the request code ${eventsData.requestCode} is disabled")
            }
        }
    }


    private fun scheduleAlarm(
        title: String,
        subtitle: String,
        requestCode: Int,
        triggerTime: Long
    ) {

        if (triggerTime <= System.currentTimeMillis()) {
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = EventsUtils.EVENTS_ACTION
        intent.putExtra(EventsUtils.INTENT_TITLE_EXTRA, title)
        intent.putExtra(EventsUtils.INTENT_SUBTITLE_EXTRA, subtitle)
        intent.putExtra(EventsUtils.INTENT_NOTIFICATION_ID_EXTRA, requestCode)


        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canScheduleExactAlarms) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime + 10_000,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime + 10_000, pendingIntent)
        }

        Log.d("EventsFlow", "scheduled an alarm for an event with the request code: $requestCode")

    }

    fun MutableList<EventsData>.addEvent(
        titleResId: Int,
        subtitleResId: Int,
        requestCode: Int,
        isToday: Boolean,
        triggerTime: Long,
        isEnabled: Boolean
    ) {
        val localizedContext: Context = context.getLocalizedContext()
        this.add(
            EventsData(
                title = localizedContext.getString(titleResId),
                subtitle = localizedContext.getString(subtitleResId),
                requestCode = requestCode,
                isToday = isToday,
                triggerTime = triggerTime,
                isEnabled = isEnabled
            )
        )
    }

    private fun lastDayOfHijriMonth(hYear: Int, hMonth: Int): Int {
        return HijrahDate.of(
            hYear,
            hMonth,
            1
        ).lengthOfMonth()
    }

    private fun getTodayTimeInMillis(hourOfDay: Int): Long {
        return LocalDate.now()
            .atTime(hourOfDay, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}