package com.islamnotify.settings.presentation

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan2.CalculationMethod
import com.islamnotify.calendar.domain.CalendarRepository
import com.islamnotify.common.AppUtils.getLocalizedContext
import com.islamnotify.common.domain.CrashReporter
import com.islamnotify.events.domain.EventFlags
import com.islamnotify.events.domain.EventsPreferenceKeys
import com.islamnotify.events.domain.EventsWork
import com.islamnotify.main.domain.MainPreferencesConfig
import com.islamnotify.main.domain.MainPreferencesRepository
import com.islamnotify.notification.domain.NotificationWork
import com.islamnotify.prayer_times.domain.PrayerDataUseCase
import com.islamnotify.prayer_times.domain.model.PrayerConfig
import com.islamnotify.sounds.domain.SoundsWork
import com.islamnotify.ui.theme.AppThemeTypes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.Boolean
import kotlin.String
import com.islamnotify.R
import com.islamnotify.sounds.domain.AZAN_SOUNDS
import com.islamnotify.sounds.domain.IQAMA_SOUNDS
import com.islamnotify.sounds.domain.NOTIFY_SOUNDS
import com.islamnotify.sounds.domain.SoundOption
import java.util.Locale

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val prayerDataUseCase: PrayerDataUseCase,
    private val notificationWork: NotificationWork,
    val eventsWork: EventsWork,
    val preferencesRepository: MainPreferencesRepository,
    val soundsWork: SoundsWork,
    val calendarRepository: CalendarRepository,
    private val crashReporter: CrashReporter
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val preferencesConfig: StateFlow<MainPreferencesConfig?> =
        preferencesRepository.getConfig().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        val localizedContext = context.getLocalizedContext()

        // get language
        val locales = AppCompatDelegate.getApplicationLocales()
        val currentLanguageOption: LanguageOption
        val language: String
        if (locales.isEmpty) {
            currentLanguageOption = LanguageOption.AUTO
            language = localizedContext.getString(R.string.settings_language_system_default)
        } else {
            val langCode = locales.get(0)?.language
            currentLanguageOption = when (langCode) {
                "en" -> LanguageOption.ENGLISH
                "ar" -> LanguageOption.ARABIC
                else -> LanguageOption.AUTO
            }
            language = langCode?.mapToString(localizedContext, false)
                ?: context.getString(R.string.settings_unknown_place_holder)
        }
        _uiState.update { it.copy(language = language, currentLanguageOption = currentLanguageOption) }


        // Launch Prayer Config independently
        viewModelScope.launch {
            try {
                // Consider adding a timeout here if the repository doesn't have one
                val prayerConfig = prayerDataUseCase.getPrayerConfig()
                _uiState.update {
                    Log.d(
                        "Auto Calc Method",
                        "SettingsViewModel init: Auto = ${prayerConfig.autoCalculationMethod}, manual = ${prayerConfig.method}"
                    )
                    it.copy(
                        isAutoCalcEnabled = prayerConfig.isAutoCalculationMethodEnabled,
                        autoCalcMethod = prayerConfig.autoCalculationMethod?.toDisplayString(
                            localizedContext
                        ),
                        calculationMethod = prayerConfig.manualCalculationMethod.toDisplayString(
                            localizedContext
                        ),
                        currentManualMethod = prayerConfig.manualCalculationMethod,
                        showNextSunrise = prayerConfig.showNextSunrise,
                        showNextDuha = prayerConfig.showNextDuha,
                        showNextIqama = prayerConfig.showNextIqama,
                        showNextMidnight = prayerConfig.showNextMidnight,
                        showNextLastThird = prayerConfig.showNextLastThird
                    )
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Prayer config fetch failed", e)
                crashReporter.recordNonFatal(e)
            }
        }

        // viewModelScope.launch Calendar Config independently
        viewModelScope.launch {
            val config = calendarRepository.getConfig()
            _uiState.update {
                it.copy(
                    currentHijriOffset = config.hijriOffset,
                    hijriDateAdjustment = formatHijriSubtitle(config.hijriOffset, localizedContext)
                )
            }
        }

        // Collect Sounds Flow (Reactive: UI updates whenever sound settings change)
        viewModelScope.launch {
            soundsWork.getSoundsConfig().collect { config ->
                val localizedContext = context.getLocalizedContext()
                _uiState.update {
                    it.copy(
                        isAzanSoundEnabled = config.isAzanEnabled,
                        isIqamaSoundEnabled = config.isIqamaEnabled,
                        azanSoundResId = config.azanSoundUriString?.substringAfterLast("/")?.toIntOrNull(),
                        iqamaSoundResId = config.iqamaSoundUriString?.substringAfterLast("/")?.toIntOrNull(),
                        notifySoundResId = config.notifySoundUriString?.substringAfterLast("/")?.toIntOrNull(),
                        azanSoundName = deriveSoundName(config.azanSoundUriString, AZAN_SOUNDS, localizedContext),
                        iqamaSoundName = deriveSoundName(config.iqamaSoundUriString, IQAMA_SOUNDS, localizedContext),
                        notifySoundName = deriveSoundName(config.notifySoundUriString, NOTIFY_SOUNDS, localizedContext)
                    )
                }
            }
        }

        // Collect Notification status Flow
        viewModelScope.launch {
            notificationWork.isEnabled().collect { enabled ->
                _uiState.update { it.copy(isNotificationEnabled = enabled) }
            }
        }

        // Collect Events Flow
        viewModelScope.launch {
            eventsWork.getEventFlags().collect { flags ->
                _uiState.update { it.copy(isEventsEnabled = flags.isAllEnabled, eventFlags = flags) }
            }
        }

        // collect theme
        viewModelScope.launch {
            preferencesRepository.getConfig().collect { flags ->
                _uiState.update { it.copy(theme = flags.theme.mapToString(localizedContext)) }
            }
        }

    }


    fun onHijriOffsetChanged(offset: Int) {
        val localizedContext = context.getLocalizedContext()
        _uiState.update {
            it.copy(
                currentHijriOffset = offset,
                hijriDateAdjustment = formatHijriSubtitle(offset, localizedContext)
            )
        }
        viewModelScope.launch {
            calendarRepository.saveConfig { it.copy(hijriOffset = offset) }
        }
    }

    private fun formatHijriSubtitle(offset: Int, context: Context): String {
        return "${context.getString(R.string.settings_hijri_date_offset_subtitle_part1)} $offset ${context.getString(R.string.settings_hijri_date_offset_subtitle_part2)}"
    }

    fun onManualCalculationMethodChanged(method: CalculationMethod) {
        val localizedContext = context.getLocalizedContext()
        _uiState.update {
            it.copy(
                currentManualMethod = method,
                calculationMethod = method.toDisplayString(localizedContext)
            )
        }
        viewModelScope.launch {
            prayerDataUseCase.savePrayerConfig {
                it.copy(manualCalculationMethod = method)
            }
            // Only reschedule when notifications are already on; startWork() would
            // otherwise re-enable the (disabled) notification toggle.
            if (_uiState.value.isNotificationEnabled) {
                notificationWork.startWork()
            }
        }
    }

    fun AppThemeTypes.mapToString(context: Context): String {
        return when (this) {
            AppThemeTypes.GREEN_DARK -> context.getString(R.string.settings_theme_green_dark)
            AppThemeTypes.GREEN_LIGHT -> context.getString(R.string.settings_theme_green_light)
            AppThemeTypes.PINK_DARK -> context.getString(R.string.settings_theme_pink_dark)
            AppThemeTypes.PINK_LIGHT -> context.getString(R.string.settings_theme_pink_light)
            AppThemeTypes.BLUE_DARK -> context.getString(R.string.settings_theme_blue_dark)
            AppThemeTypes.BLUE_LIGHT -> context.getString(R.string.settings_theme_blue_light)
            AppThemeTypes.YELLOW_DARK -> context.getString(R.string.settings_theme_yellow_dark)
            AppThemeTypes.YELLOW_LIGHT -> context.getString(R.string.settings_theme_yellow_light)
            AppThemeTypes.BROWN_DARK -> context.getString(R.string.settings_theme_brown_dark)
            AppThemeTypes.BROWN_LIGHT -> context.getString(R.string.settings_theme_brown_light)
            AppThemeTypes.RED_DARK -> context.getString(R.string.settings_theme_red_dark)
            AppThemeTypes.RED_LIGHT -> context.getString(R.string.settings_theme_red_light)
            else -> context.getString(R.string.settings_unknown_place_holder)
        }
    }

    private fun String.mapToString(context: Context, isAuto: Boolean): String {
        return when (this) {
            "en" -> {
                if (isAuto) "${context.getString(R.string.settings_language_en)} ${
                    context.getString(
                        R.string.settings_language_auto_part
                    )
                }"
                else context.getString(R.string.settings_language_en)
            }

            "ar" -> {
                if (isAuto) "${context.getString(R.string.settings_language_ar)} ${
                    context.getString(
                        R.string.settings_language_auto_part
                    )
                }"
                else context.getString(R.string.settings_language_ar)
            }

            else -> context.getString(R.string.settings_unknown_place_holder)
        }
    }

    fun onToggleAutoCalculation(enabled: Boolean) {
        _uiState.update { it.copy(isAutoCalcEnabled = enabled) }
        viewModelScope.launch {
            prayerDataUseCase.savePrayerConfig {
                it.copy(isAutoCalculationMethodEnabled = enabled)
            }
            // Switching auto/manual changes the active calculation method and thus the
            // prayer times, so the pending alarms must be rescheduled — but only when
            // notifications are already on (startWork() would otherwise re-enable them).
            if (_uiState.value.isNotificationEnabled) {
                notificationWork.startWork()
            }
        }
    }

    fun onToggleAzanSound(enabled: Boolean) {
        _uiState.update { it.copy(isAzanSoundEnabled = enabled) }
        viewModelScope.launch {
            soundsWork.saveConfig {
                it.copy(isAzanEnabled = enabled)
            }
        }
    }

    fun onToggleIqamaSound(enabled: Boolean) {
        _uiState.update { it.copy(isIqamaSoundEnabled = enabled) }
        viewModelScope.launch {
            soundsWork.saveConfig {
                it.copy(isIqamaEnabled = enabled)
            }
        }
    }

    fun markNotificationPermRequested() {
        _uiState.update { it.copy(hasRequestedNotificationPerm = true) }
    }

    fun onToggleNotification(enabled: Boolean) {
        _uiState.update { it.copy(isNotificationEnabled = enabled) }
        viewModelScope.launch {
            if (enabled) {
                notificationWork.startWork()
            } else {
                notificationWork.cancel()
            }
        }
    }

    fun onToggleEventsNotification(enabled: Boolean) {
        _uiState.update { it.copy(isEventsEnabled = enabled) }
        viewModelScope.launch {
            eventsWork.toggleFlag(EventsPreferenceKeys.IS_ALL_ENABLED, enabled)
        }
    }

    fun onThemeChanged(theme: AppThemeTypes) {
        viewModelScope.launch {
            preferencesRepository.saveConfig {
                it.copy(theme = theme)
            }
        }
    }

    fun onPrayerVisibilityFlagsChanged(transform: (PrayerConfig) -> PrayerConfig) {
        val partial = PrayerConfig(
            showNextSunrise = _uiState.value.showNextSunrise,
            showNextDuha = _uiState.value.showNextDuha,
            showNextIqama = _uiState.value.showNextIqama,
            showNextMidnight = _uiState.value.showNextMidnight,
            showNextLastThird = _uiState.value.showNextLastThird
        )
        val updated = transform(partial)
        _uiState.update {
            it.copy(
                showNextSunrise = updated.showNextSunrise,
                showNextDuha = updated.showNextDuha,
                showNextIqama = updated.showNextIqama,
                showNextMidnight = updated.showNextMidnight,
                showNextLastThird = updated.showNextLastThird
            )
        }
        viewModelScope.launch {
            prayerDataUseCase.savePrayerConfig(transform)
            // Only reschedule when notifications are already on; startWork() would
            // otherwise re-enable the (disabled) notification toggle.
            if (_uiState.value.isNotificationEnabled) {
                notificationWork.startWork()
            }
        }
    }

    fun onLanguageChanged(option: LanguageOption) {
        val localizedContext = context.getLocalizedContext()
        val displayString = when (option) {
            LanguageOption.ENGLISH -> localizedContext.getString(R.string.settings_language_en)
            LanguageOption.ARABIC -> localizedContext.getString(R.string.settings_language_ar)
            LanguageOption.AUTO -> localizedContext.getString(R.string.settings_language_system_default)
        }
        _uiState.update { it.copy(currentLanguageOption = option, language = displayString) }
        val localeList = when (option) {
            LanguageOption.ENGLISH -> LocaleListCompat.create(Locale("en"))
            LanguageOption.ARABIC -> LocaleListCompat.create(Locale("ar"))
            LanguageOption.AUTO -> LocaleListCompat.getEmptyLocaleList()
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun onSelectAzanSound(rawResId: Int) {
        viewModelScope.launch {
            soundsWork.saveConfig {
                it.copy(azanSoundUriString = "android.resource://${context.packageName}/$rawResId")
            }
        }
    }

    fun onSelectIqamaSound(rawResId: Int) {
        viewModelScope.launch {
            soundsWork.saveConfig {
                it.copy(iqamaSoundUriString = "android.resource://${context.packageName}/$rawResId")
            }
        }
    }

    fun onSelectNotifySound(rawResId: Int) {
        viewModelScope.launch {
            soundsWork.saveConfig {
                it.copy(notifySoundUriString = "android.resource://${context.packageName}/$rawResId")
            }
        }
    }

    private fun deriveSoundName(uriString: String?, options: List<SoundOption>, context: Context): String {
        val resId = uriString?.substringAfterLast("/")?.toIntOrNull()
        val option = options.find { it.rawResId == resId } ?: options.first()
        return context.getString(option.nameResId)
    }

    fun onEventsSelectionChanged(flags: EventFlags) {
        _uiState.update { it.copy(eventFlags = flags) }
        viewModelScope.launch {
            eventsWork.toggleFlag(EventsPreferenceKeys.MONDAY_FASTING, flags.mondayFasting)
            eventsWork.toggleFlag(EventsPreferenceKeys.THURSDAY_FASTING, flags.thursdayFasting)
            eventsWork.toggleFlag(EventsPreferenceKeys.WHITE_DAYS_FASTING, flags.whiteDaysFasting)
            eventsWork.toggleFlag(EventsPreferenceKeys.ARFA_FASTING, flags.arafaFasting)
            eventsWork.toggleFlag(EventsPreferenceKeys.TSUA_FASTING, flags.tasuaFasting)
            eventsWork.toggleFlag(EventsPreferenceKeys.ASHORA_FASTING, flags.ashoraFasting)
            eventsWork.toggleFlag(EventsPreferenceKeys.SHAWWAL_FASTING, flags.shawwalFasting)
            eventsWork.toggleFlag(EventsPreferenceKeys.RAMADAN_EVENT, flags.ramadanEvent)
            eventsWork.toggleFlag(EventsPreferenceKeys.RAMADAN_LAST_10_DAYS_EVENT, flags.ramdanLast10DaysEvent)
            eventsWork.toggleFlag(EventsPreferenceKeys.DHU_AL_HIJJA_FIRST_10_DAYS_EVENT, flags.dhuAlHijjahFirst10DaysEvent)
            eventsWork.toggleFlag(EventsPreferenceKeys.FRIDAY_EVENT, flags.fridayEvent)
            eventsWork.toggleFlag(EventsPreferenceKeys.EID_AL_FITR_EVENT, flags.eidAlFitrEvent)
            eventsWork.toggleFlag(EventsPreferenceKeys.EID_AL_ADHA_EVENT, flags.eidAlAdhaEvent)
            eventsWork.toggleFlag(EventsPreferenceKeys.MUHARRAM_EVENT, flags.muharramEvent)
            eventsWork.toggleFlag(EventsPreferenceKeys.RAJAB_EVENT, flags.rajabEvent)
            eventsWork.toggleFlag(EventsPreferenceKeys.DHU_AL_QIDA_EVENT, flags.dhuAlQidaEvent)
            eventsWork.toggleFlag(EventsPreferenceKeys.DHU_AL_HIJJA_EVENT, flags.dhuAlHijjahEvent)
        }
    }

}