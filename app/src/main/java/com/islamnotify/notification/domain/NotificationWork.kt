package com.islamnotify.notification.domain

import kotlinx.coroutines.flow.Flow

interface NotificationWork {
    suspend fun startWork(): NotificationWorkResult

    /**
     * Enqueues the notification worker.
     *
     * [expedited] must be false when called while handling BOOT_COMPLETED: below API 31 WorkManager
     * services expedited work through a foreground service, and Android 15 forbids BOOT_COMPLETED
     * receivers from starting restricted foreground service types.
     */
    suspend fun startWorkInBackground(expedited: Boolean = true)
    suspend fun cancel()
    fun isEnabled(): Flow<Boolean>
}