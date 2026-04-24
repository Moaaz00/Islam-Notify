package com.islamnotify.settings.presentation

sealed class SettingsEvent {
    // Calculation Events
    data class OnToggleAutoCalculation(val enabled: Boolean) : SettingsEvent()
    object OnClickCalculationMethod : SettingsEvent()

    // Time Adjustment Events
    object OnClickPrayerTimeOffsets : SettingsEvent()
    object OnClickHijriAdjustment : SettingsEvent()

    // Sound Events
    data class OnToggleAzanSounds(val enabled: Boolean) : SettingsEvent()
    data class OnToggleIqamaSounds(val enabled: Boolean) : SettingsEvent()
    object OnClickAzanSoundPicker : SettingsEvent()
    object OnClickIqamaSoundPicker : SettingsEvent()
    object OnClickNotifySoundPicker : SettingsEvent()

    // Events Settings
    data class OnToggleEventNotifications(val enabled: Boolean) : SettingsEvent()
    object OnClickManageEvents : SettingsEvent()

    // Notification Settings
    data class OnToggleNotification(val enabled: Boolean) : SettingsEvent()
    object OnClickManageNotifications : SettingsEvent()


    // Preferences
    object OnClickLanguage : SettingsEvent()
    object OnClickTheme : SettingsEvent()

    // Global
    object OnBackClick : SettingsEvent()
}