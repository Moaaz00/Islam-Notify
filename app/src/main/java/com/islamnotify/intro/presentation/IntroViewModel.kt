package com.islamnotify.intro.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamnotify.intro.domain.IntroPreferencesRepository
import com.islamnotify.main.domain.MainPreferencesRepository
import com.islamnotify.main.domain.PermissionDialogs
import com.islamnotify.ui.theme.AppThemeTypes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class IntroViewModel @Inject constructor(
    private val mainPreferencesRepository: MainPreferencesRepository,
    private val introPreferencesRepository: IntroPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IntroUiState())
    val uiState: StateFlow<IntroUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val flags = introPreferencesRepository.getRequestedFlags().first()
            _uiState.update {
                it.copy(
                    locationRequested = flags.location,
                    notificationRequested = flags.notification
                )
            }
        }
    }

    /** Read the saved theme synchronously so the intro is themed from the first frame. */
    fun initialTheme(): AppThemeTypes = runBlocking {
        mainPreferencesRepository.getConfig().first().theme
    }

    fun setPage(page: Int) = _uiState.update { it.copy(currentPage = page) }

    /** Refreshes live permission state, e.g. after returning from the system settings screen. */
    fun refreshPermissionStates(
        location: Boolean,
        notification: Boolean,
        battery: Boolean
    ) = _uiState.update {
        it.copy(
            locationGranted = location,
            notificationGranted = notification,
            batteryGranted = battery
        )
    }

    fun onPermissionResult(permission: PermissionDialogs, granted: Boolean) {
        _uiState.update {
            when (permission) {
                PermissionDialogs.LOCATION -> it.copy(locationGranted = granted)
                PermissionDialogs.NOTIFICATION -> it.copy(notificationGranted = granted)
                PermissionDialogs.BATTERY -> it.copy(batteryGranted = granted)
            }
        }
    }

    fun markRequested(permission: PermissionDialogs) {
        viewModelScope.launch { introPreferencesRepository.setRequested(permission) }
        _uiState.update {
            when (permission) {
                PermissionDialogs.LOCATION -> it.copy(locationRequested = true)
                PermissionDialogs.NOTIFICATION -> it.copy(notificationRequested = true)
                PermissionDialogs.BATTERY -> it
            }
        }
    }

    fun showBatterySheet() = _uiState.update { it.copy(showBatterySheet = true) }
    fun dismissBatterySheet() = _uiState.update { it.copy(showBatterySheet = false) }

    fun showSkipWarning() = _uiState.update { it.copy(showSkipWarning = true) }
    fun dismissSkipWarning() = _uiState.update { it.copy(showSkipWarning = false) }

    /** Persists that the intro is done, then invokes [onDone] (used to launch MainActivity). */
    fun completeIntro(onDone: () -> Unit) {
        viewModelScope.launch {
            mainPreferencesRepository.saveConfig { it.copy(showIntro = false) }
            onDone()
        }
    }
}
