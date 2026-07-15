package com.islamnotify.main.presentation

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.islamnotify.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan2.CalculationMethod
import com.islamnotify.calendar.domain.CalendarRepository
import com.islamnotify.calendar.domain.DateConfig
import com.islamnotify.common.AppUtils
import com.islamnotify.common.AppUtils.getLocalizedContext
import com.islamnotify.common.domain.CrashReporter
import com.islamnotify.events.domain.EventsWork
import com.islamnotify.events.util.EventsUtils
import com.islamnotify.main.domain.MainPreferencesConfig
import com.islamnotify.main.domain.MainPreferencesRepository
import com.islamnotify.main.domain.PermissionDialogs
import com.islamnotify.notification.domain.NotificationWorkState
import com.islamnotify.notification.domain.NotificationFailureCauses
import com.islamnotify.notification.domain.NotificationWork
import com.islamnotify.notification.domain.NotificationWorkResult
import com.islamnotify.prayer_times.domain.LocationPrayerResult
import com.islamnotify.prayer_times.domain.PrayerDataUseCase
import com.islamnotify.prayer_times.domain.model.CountDownDataModel
import com.islamnotify.prayer_times.domain.model.NextPrayerData
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import com.islamnotify.sounds.domain.SoundStates
import com.islamnotify.sounds.domain.SoundsConfig
import com.islamnotify.sounds.domain.SoundsWork
import com.islamnotify.sounds.utils.SoundsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class PermissionDialogState(
    val notificationMissing: Boolean,
    val batteryMissing: Boolean,
    val locationMissing: Boolean
)

@HiltViewModel
class MainViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val prayerDataUseCase: PrayerDataUseCase,
    private val notificationWork: NotificationWork,
    private val mainPreferencesRepository: MainPreferencesRepository,
    eventsWork: EventsWork,
    val soundsWork: SoundsWork,
    val calendarRepository: CalendarRepository,
    private val crashReporter: CrashReporter
) : ViewModel() {


    private val _uiState = MutableStateFlow<LocationPrayerState>(LocationPrayerState.Loading)
    val uiState: StateFlow<LocationPrayerState> = _uiState

    private val _notificationState =
        MutableStateFlow<NotificationWorkState>(NotificationWorkState.Loading)
    val notificationState: StateFlow<NotificationWorkState> = _notificationState


    //    private val _preferencesState = MutableStateFlow(MainPreferencesConfig())
    val preferencesConfig: StateFlow<MainPreferencesConfig?> =
        mainPreferencesRepository.getConfig().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )


    private val _notificationFailures = MutableStateFlow<List<NotificationFailureCauses>?>(null)
    val notificationFailures: StateFlow<List<NotificationFailureCauses>?> = _notificationFailures


    private val _countDown = MutableStateFlow(CountDownDataModel(0,0,0))
    val countDown: StateFlow<CountDownDataModel> = _countDown.asStateFlow()

    private val _nextPrayer = MutableStateFlow(NextPrayerData())
    val nextPrayer: StateFlow<NextPrayerData> = _nextPrayer.asStateFlow()

    private var countDownJob: Job? = null

    private val _hijriDate = MutableStateFlow(String())
    val hijriDate = _hijriDate.asStateFlow()


    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _permissionDialogState = MutableStateFlow<PermissionDialogState?>(null)
    val permissionDialogState: StateFlow<PermissionDialogState?> = _permissionDialogState.asStateFlow()

    // Set when the user explicitly dismisses the reminder dialog (Cancel / Don't Ask Again).
    // In-memory and session-scoped: it prevents re-opening the dialog on resume, but resets on a
    // cold start. It does NOT block resume-driven updates while the dialog is still open.
    private var permissionsDialogDismissed = false

    val soundsConfigState: StateFlow<SoundsConfig?> = soundsWork.getSoundsConfig()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )


    val isPermanentNotificationEnabled: StateFlow<Boolean> = notificationWork.isEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true // Default value
        )


    val prayerSoundStates: StateFlow<Map<PrayerTypes, SoundStates?>?> = soundsConfigState
        .map { config ->
            mapOf(
                PrayerTypes.FAJR to config?.fajrSoundState,
                PrayerTypes.SUNRISE to config?.sunriseSoundState,
                PrayerTypes.ZUHR to config?.zuhrSoundState,
                PrayerTypes.ASR to config?.asrSoundState,
                PrayerTypes.SUNSET to config?.sunsetSoundState,
                PrayerTypes.ISHA to config?.ishaSoundState,
                PrayerTypes.LAST_THIRD to config?.lastThirdSoundState
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )


    init {
        populateData()
        observeNextPrayerChanges()

        viewModelScope.launch(Dispatchers.IO) {
            val hijriData = calendarRepository.getHijriDate()
            _hijriDate.value =
                "${hijriData.dayOfWeek}, ${hijriData.formatedDayOfMonth} ${hijriData.monthName} ${hijriData.formatedYear}"

            if (notificationWork.isEnabled().first()) {
                startNotificationWork()
            }


            val soundsConfig = soundsWork.getSoundsConfig().first()
            if (SoundsUtils.shouldStartSoundWork(soundsConfig)) {
                soundsWork.startScheduling()
            }


            val eventConfig = eventsWork.getEventFlags().first()
            if (eventConfig.isAllEnabled && EventsUtils.areAnySubEventsEnabled(eventConfig)) {
                eventsWork.startWork()
            }

        }
    }

    fun toggleNotificationState(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                notificationWork.startWork()
            } else {
                notificationWork.cancel()
            }
        }
    }

    fun refreshData() {

        viewModelScope.launch {
            _isRefreshing.value = true

            fetchPrayerData()

            val hijriData = calendarRepository.getHijriDate()
            _hijriDate.value =
                "${hijriData.dayOfWeek}, ${hijriData.formatedDayOfMonth} ${hijriData.monthName} ${hijriData.formatedYear}"

            // Only reschedule when notifications are already on; startNotificationWork()
            // would otherwise re-enable the (disabled) notification toggle.
            if (notificationWork.isEnabled().first()) {
                startNotificationWork()
            }

            delay(700)
            _isRefreshing.value = false
        }
    }

    fun togglePrayerSoundState(prayerType: PrayerTypes) {
        viewModelScope.launch {
            soundsWork.toggleSoundState(prayerType)
        }
    }

    fun startNotificationWork() {
        viewModelScope.launch {
            when (val result = notificationWork.startWork()) {
                is NotificationWorkResult.Success -> {
                    _notificationState.value = NotificationWorkState.Success
                    _notificationFailures.value = result.failures
                }

                is NotificationWorkResult.LocationError -> {
                    _notificationState.value = NotificationWorkState.LocationError
                    _notificationFailures.value = result.failures
                }

                is NotificationWorkResult.PrayerError -> {
                    _notificationState.value = NotificationWorkState.PrayerError
                    _notificationFailures.value = result.failures
                }

//                is NotificationWorkResult.Failed -> {
//                    _notificationState.value = NotificationWorkState.Failed
//                    _notificationFailures.value = result.failures
//                }

                is NotificationWorkResult.Error -> {
                    crashReporter.log("MainViewModel.startNotificationWork: Error state (failures=${result.failures})")
                    _notificationState.value = NotificationWorkState.Error
                    _notificationFailures.value = result.failures
                }
            }
        }
    }

    private fun populateData() {
        viewModelScope.launch {
            loadInitialData()
            Log.d("FetchPrayerTimes", "called from refreshData()")
            fetchPrayerData()
        }
    }


    private fun observeNextPrayerChanges() {
        viewModelScope.launch {
            _nextPrayer.collectLatest { nextPrayerData ->
                if (nextPrayerData.millis > 0) {
                    startCountDown(nextPrayerData.millis)
                }
            }
        }
    }


    fun startCountDown(nextPrayerMillis: Long) {
        countDownJob?.cancel()
        val localizedContext = context.getLocalizedContext()
        countDownJob = viewModelScope.launch {
            while (isActive) {

                val hoursSymbol = localizedContext.getString(R.string.remaining_hour_symbol)
                val minutesSymbol = localizedContext.getString(R.string.remaining_minutes_symbol)
                val secondsSymbol = localizedContext.getString(R.string.remaining_seconds_symbol)

                val now = System.currentTimeMillis()
                val difference = nextPrayerMillis - now

                if (difference > 0) {
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(difference) % 60
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(difference) % 60
                    val hours = TimeUnit.MILLISECONDS.toHours(difference)

//                    _countDown.value = String.format(
//                        Locale.getDefault(),
//                        "%02d:%02d:%02d",
//                        hours,
//                        minutes,
//                        seconds
//                    )
                    _countDown.value = CountDownDataModel(hours.toInt(), minutes.toInt(), seconds.toInt())

                    delay(1000)

                } else {
                    _countDown.value = CountDownDataModel(0,0,0)
                    populateData()
                    break
                }
            }
        }
    }

    fun refreshPermissionState(notificationGranted: Boolean, batteryGranted: Boolean, locationGranted: Boolean) {
        viewModelScope.launch {
            val config = mainPreferencesRepository.getConfig().first()
            val notificationMissing = !notificationGranted && !config.dontAskNotification
            val batteryMissing = !batteryGranted && !config.dontAskBattery
            val locationMissing = !locationGranted && !config.dontAskLocation

            if (notificationMissing || batteryMissing || locationMissing) {
                // Keep the dialog in sync while it's open, but don't reopen it after the user
                // explicitly dismissed it this session (Cancel / Don't Ask Again).
                if (!permissionsDialogDismissed) {
                    _permissionDialogState.value = PermissionDialogState(notificationMissing, batteryMissing, locationMissing)
                }
            } else {
                val wasShowingDialog = _permissionDialogState.value != null
                _permissionDialogState.value = null
                permissionsDialogDismissed = false // all clear → allow future reminders again
                // Only reschedule when notifications are already on; startNotificationWork()
                // would otherwise re-enable the (disabled) notification toggle.
                if (wasShowingDialog && notificationWork.isEnabled().first()) {
                    startNotificationWork()
                }
            }
        }
    }

    fun dismissPermissionsDialog(dontAskAgain: Boolean) {
        viewModelScope.launch {
            if (dontAskAgain) {
                val currentState = _permissionDialogState.value
                mainPreferencesRepository.saveConfig { config ->
                    config.copy(
                        dontAskNotification = config.dontAskNotification || (currentState?.notificationMissing == true),
                        dontAskBattery = config.dontAskBattery || (currentState?.batteryMissing == true),
                        dontAskLocation = config.dontAskLocation || (currentState?.locationMissing == true)
                    )
                }
            }
            permissionsDialogDismissed = true
            _permissionDialogState.value = null
        }
    }

    fun setToLoading() {
        _uiState.value = LocationPrayerState.Loading
    }

    private suspend fun fetchPrayerData() {
        when (val result = prayerDataUseCase.getPrayerDataWithCurrentLocation()) {
            is LocationPrayerResult.PrayerError -> {
                crashReporter.log("MainViewModel.fetchPrayerData: PrayerError state shown to user")
                _uiState.value = LocationPrayerState.PrayerError
            }

            is LocationPrayerResult.LocationError -> {
                crashReporter.log("MainViewModel.fetchPrayerData: LocationError (${result.failureCause}) shown to user")
                _uiState.value = LocationPrayerState.LocationError(result.failureCause)
            }

            is LocationPrayerResult.LocationStale -> {
                _uiState.value = LocationPrayerState.LocationStale(
                    prayerData = result.prayerData,
                    locationData = result.locationData,
                    failureCause = result.failureCause
                )
                _nextPrayer.value = prayerDataUseCase.getNextPrayer(result.prayerData)
            }

            is LocationPrayerResult.Success -> {
                _uiState.value = LocationPrayerState.Success(
                    prayerData = result.prayerData,
                    locationData = result.locationData
                )
                _nextPrayer.value = prayerDataUseCase.getNextPrayer(result.prayerData)
            }
        }
    }


    fun fetchPrayerDataAsync() {
        viewModelScope.launch {
            Log.d("FetchPrayerTimes", "called from fetchPrayerDataAsync()")
            fetchPrayerData()
        }
    }

    fun initialTheme(): MainPreferencesConfig {
        return runBlocking {
            mainPreferencesRepository.getConfig().first()
        }
    }

    fun loadInitialData() {
        runBlocking {
            val result = prayerDataUseCase.loadInitialData()

            if (result.prayerData != null) {
                _uiState.value = LocationPrayerState.Initial(
                    prayerData = result.prayerData!!,
                    locationData = result.locationData
                )
                _nextPrayer.value = prayerDataUseCase.getNextPrayer(result.prayerData!!)
            } else {
                _uiState.value = LocationPrayerState.Loading
            }
        }

    }
}