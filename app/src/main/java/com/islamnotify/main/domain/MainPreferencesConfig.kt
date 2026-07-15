package com.islamnotify.main.domain

import com.islamnotify.ui.theme.AppThemeTypes

data class MainPreferencesConfig(
    var theme: AppThemeTypes,
    var showIntro: Boolean,
    var dontAskNotification: Boolean = false,
    var dontAskBattery: Boolean = false,
    var dontAskLocation: Boolean = false
)
