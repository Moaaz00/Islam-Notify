package com.islamnotify.notification.domain

import kotlinx.coroutines.flow.Flow

interface NotificationWork {
    suspend fun startWork(): NotificationWorkResult
    suspend fun startWorkInBackground()
    suspend fun cancel()
    fun isEnabled(): Flow<Boolean>
}