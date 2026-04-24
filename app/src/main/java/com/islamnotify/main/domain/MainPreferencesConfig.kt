package com.islamnotify.main.domain

import com.islamnotify.ui.theme.AppThemeTypes

data class MainPreferencesConfig(
    var theme: AppThemeTypes,
    var showIntro: Boolean
)
