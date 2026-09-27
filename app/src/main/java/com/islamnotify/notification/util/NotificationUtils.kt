package com.islamnotify.notification.util

object NotificationUtils {
    const val NOTIFICATION_MIDNIGHT_REQUEST_CODE = 1200
    const val NOTIFICATION_PROGRESS_REQUEST_CODE = 1201
    const val PRAYER_NOTIFICATION_ID = 2000

    /** How often the progress bar moves while the phone is in use. */
    const val PROGRESS_UPDATE_INTERVAL_MILLIS = 10 * 60 * 1000L

    const val PRAYERS_WORK_REQUEST_TAG = "NOTIFICATION_FALLBACK_WORK_MANAGER"
    const val MIDNIGHT_WORK_REQUEST_TAG = "NOTIFICATION_MIDNIGHT_FALLBACK_WORK_MANAGER"
}
