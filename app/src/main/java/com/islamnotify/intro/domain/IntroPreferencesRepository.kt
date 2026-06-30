package com.islamnotify.intro.domain

import com.islamnotify.main.domain.PermissionDialogs
import kotlinx.coroutines.flow.Flow

/**
 * Tracks whether the runtime permissions have already been requested at least once.
 *
 * Required to detect the "permanently declined" state: Android's
 * shouldShowRequestPermissionRationale() returns false both *before* the first request
 * and *after* the user permanently denies (or denies twice on Android 11+). Combining
 * the persisted "was requested" flag with the rationale check lets us tell the two cases
 * apart so we can fall back to the app settings screen.
 */
interface IntroPreferencesRepository {
    fun getRequestedFlags(): Flow<RequestedPermissions>
    suspend fun setRequested(permission: PermissionDialogs)
}

data class RequestedPermissions(
    val location: Boolean = false,
    val notification: Boolean = false
)
