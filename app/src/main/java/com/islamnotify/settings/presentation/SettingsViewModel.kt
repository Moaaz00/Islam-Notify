package com.islamnotify.settings.presentation

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan2.CalculationMethod
import com.islamnotify.calendar.domain.CalendarRepository
import com.islamnotify.common.AppUtils.getLocalizedContext
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
import java.util.Locale

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val prayerDataUseCase: PrayerDataUseCase,
    private val notificationWork: NotificationWork,
    val eventsWork: EventsWork,
    val preferencesRepository: MainPreferencesRepository,
    val soundsWork: SoundsWork,
    val calendarRepository: CalendarRepository
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
        val language: String = if (locales.isEmpty) {
            Locale.getDefault().language.mapToString(localizedContext, true)
        } else {
            locales.get(0)?.language?.mapToString(localizedContext, false)
                ?: context.getString(R.string.settings_unknown_place_holder)
        }
        _uiState.update { it.copy(language = language) }


        // TODO: To be done dynamically
        _uiState.update {
            it.copy(
                azanSoundName = "Al-Minshawy model",
                iqamaSoundName = "Al-Husary model",
                notifySoundName = "Sound-01 model"
            )
        }


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
                        autoCalcMethod = prayerConfig.autoCalculationMethod?.mapToString(
                            localizedContext
                        ),
                        calculationMethod = prayerConfig.manualCalculationMethod?.mapToString(
                            localizedContext
                        )
                            ?: context.getString(R.string.settings_unknown_place_holder)
                    )
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Prayer config fetch failed", e)
            }
        }

        // viewModelScope.launch Calendar Config independently
        viewModelScope.launch {
            val config = calendarRepository.getConfig()
            _uiState.update {
                it.copy(
                    hijriDateAdjustment = "${localizedContext.getString(R.string.settings_hijri_date_offset_subtitle_part1)} ${config.hijriOffset} ${
                        localizedContext.getString(
                            R.string.settings_hijri_date_offset_subtitle_part2
                        )
                    }"
                )
            }
        }

        // Collect Sounds Flow (Reactive: UI updates whenever sound settings change)
        viewModelScope.launch {
            soundsWork.getSoundsConfig().collect { config ->
                _uiState.update {
                    it.copy(
                        isAzanSoundEnabled = config.isAzanEnabled,
                        isIqamaSoundEnabled = config.isIqamaEnabled
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
                _uiState.update { it.copy(isEventsEnabled = flags.isAllEnabled) }
            }
        }

        // collect theme
        viewModelScope.launch {
            preferencesRepository.getConfig().collect { flags ->
                _uiState.update { it.copy(theme = flags.theme.mapToString(localizedContext)) }
            }
        }

    }


    private fun CalculationMethod.mapToString(context: Context): String {
        return when (this) {
            CalculationMethod.EGYPTIAN -> context.getString(R.string.settings_calculation_method_egypt)
            CalculationMethod.NORTH_AMERICA -> context.getString(R.string.settings_calculation_method_north_america)
            CalculationMethod.MUSLIM_WORLD_LEAGUE -> context.getString(R.string.settings_calculation_method_muslim_league)
            CalculationMethod.MOON_SIGHTING_COMMITTEE -> context.getString(R.string.settings_calculation_method_moonsighting_committee)
            CalculationMethod.SINGAPORE -> context.getString(R.string.settings_calculation_method_singapore)
            CalculationMethod.DUBAI -> context.getString(R.string.settings_calculation_method_dubai)
            CalculationMethod.KARACHI -> context.getString(R.string.settings_calculation_method_karachi)
            CalculationMethod.KUWAIT -> context.getString(R.string.settings_calculation_method_kuwait)
            CalculationMethod.QATAR -> context.getString(R.string.settings_calculation_method_qatar)
            CalculationMethod.TURKEY -> context.getString(R.string.settings_calculation_method_turkey)
            CalculationMethod.UMM_AL_QURA -> context.getString(R.string.settings_calculation_method_umm_al_qura)
            else -> context.getString(R.string.settings_unknown_place_holder)
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

}