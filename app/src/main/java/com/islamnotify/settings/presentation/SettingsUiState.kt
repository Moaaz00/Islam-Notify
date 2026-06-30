package com.islamnotify.settings.presentation

import com.batoulapps.adhan2.CalculationMethod
import com.islamnotify.events.domain.EventFlags

enum class LanguageOption { ENGLISH, ARABIC, AUTO }

data class SettingsUiState(
    val isAutoCalcEnabled: Boolean = true,
    val autoCalcMethod: String? = null,
    val calculationMethod: String = String(),
    val currentManualMethod: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
    val hijriDateAdjustment: String = String(),
    val currentHijriOffset: Int = 0,
    val isAzanSoundEnabled: Boolean = true,
    val isIqamaSoundEnabled: Boolean = true,
    val azanSoundName: String = String(),
    val iqamaSoundName: String = String(),
    val notifySoundName: String = String(),
    val azanSoundResId: Int? = null,
    val iqamaSoundResId: Int? = null,
    val notifySoundResId: Int? = null,
    val isNotificationEnabled: Boolean = true,
    val showNextSunrise: Boolean = true,
    val showNextDuha: Boolean = true,
    val showNextIqama: Boolean = true,
    val showNextMidnight: Boolean = true,
    val showNextLastThird: Boolean = true,
    val isEventsEnabled: Boolean = true,
    val eventFlags: EventFlags = EventFlags(),
    val currentLanguageOption: LanguageOption = LanguageOption.AUTO,
    val language: String = String(),
    val theme: String = String()
)
