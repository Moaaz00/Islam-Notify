package com.islamnotify.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamnotify.notification.domain.NotificationWork
import com.islamnotify.prayer_times.domain.PrayerDataUseCase
import com.islamnotify.prayer_times.domain.model.PrayerConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrayerOffsetViewModel @Inject constructor(
    private val prayerDataUseCase: PrayerDataUseCase,
    private val notificationWork: NotificationWork
) : ViewModel() {

    private val _config = MutableStateFlow(PrayerConfig())
    val config: StateFlow<PrayerConfig> = _config.asStateFlow()

    init {
        viewModelScope.launch {
            _config.value = prayerDataUseCase.getPrayerConfig()
        }
    }

    fun onOffsetChanged(transform: (PrayerConfig) -> PrayerConfig) {
        _config.update { transform(it) }
        viewModelScope.launch {
            prayerDataUseCase.savePrayerConfig { transform(it) }
            // Only reschedule when notifications are already on; startWork() would
            // otherwise re-enable the (disabled) notification toggle.
            if (notificationWork.isEnabled().first()) {
                notificationWork.startWork()
            }
        }
    }
}
