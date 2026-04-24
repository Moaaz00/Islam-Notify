package com.islamnotify.settings.presentation

data class SettingsUiState(
    val isAutoCalcEnabled: Boolean = true,
    val autoCalcMethod: String? = null,
    val calculationMethod: String = String(),
    val hijriDateAdjustment: String = String(),
    val isAzanSoundEnabled: Boolean = true,
    val isIqamaSoundEnabled: Boolean = true,
    val azanSoundName: String = String(),
    val iqamaSoundName: String = String(),
    val notifySoundName: String = String(),
    val isNotificationEnabled: Boolean = true,
    val isEventsEnabled: Boolean = true,
    val language: String = String(),
    val theme: String = String()
)

