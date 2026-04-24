package com.islamnotify.notification.domain

sealed interface NotificationWorkState {
    object LocationError: NotificationWorkState
    object PrayerError: NotificationWorkState
    object Error: NotificationWorkState
    object Failed: NotificationWorkState
    object Success: NotificationWorkState
    object Loading: NotificationWorkState
}