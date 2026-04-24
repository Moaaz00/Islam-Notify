package com.islamnotify.notification.domain

sealed interface NotificationWorkResult {
    data class LocationError(val failures: List<NotificationFailureCauses>?): NotificationWorkResult
    data class PrayerError(val failures: List<NotificationFailureCauses>?): NotificationWorkResult
    data class Error(val failures: List<NotificationFailureCauses>?): NotificationWorkResult
//    data class Failed(val failures: List<NotificationFailureCauses>?): NotificationWorkResult
    data class Success(val failures: List<NotificationFailureCauses>?): NotificationWorkResult
}


