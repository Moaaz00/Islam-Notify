package com.islamnotify.intro.presentation

data class IntroUiState(
    val currentPage: Int = 0,
    val locationGranted: Boolean = false,
    val notificationGranted: Boolean = false,
    val batteryGranted: Boolean = false,
    // Whether the runtime permission has been requested at least once (persisted).
    // Used together with shouldShowRequestPermissionRationale to detect permanent denial.
    val locationRequested: Boolean = false,
    val notificationRequested: Boolean = false,
    val showBatterySheet: Boolean = false,
    val showSkipWarning: Boolean = false
)
