package com.islamnotify.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.annotation.Keep
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.islamnotify.main.presentation.MainScreenContent
import com.islamnotify.main.presentation.MainViewModel
import com.islamnotify.settings.presentation.Manrope
import com.islamnotify.settings.presentation.MasterSettingsScreen
import com.islamnotify.settings.presentation.LanguageOption
import com.islamnotify.settings.presentation.SettingsEvent
import com.islamnotify.settings.presentation.SettingsUiState
import com.islamnotify.settings.presentation.SettingsViewModel
import com.islamnotify.ui.theme.AppThemeTypes
import kotlinx.serialization.Serializable
import com.islamnotify.R
import com.islamnotify.common.AppUtils.getLocalizedContext
import com.islamnotify.settings.presentation.PrayerOffsetViewModel
import com.islamnotify.settings.presentation.PrayerTimesOffsetScreen
import com.islamnotify.settings.presentation.SettingsDialogs.CalculationMethodSelectionDialog
import com.islamnotify.settings.presentation.SettingsDialogs.MultiSelectDialog
import com.islamnotify.settings.presentation.SettingsDialogs.SingleSelectDialog
import com.islamnotify.settings.presentation.SettingsDialogs.SoundPickerDialog
import com.islamnotify.settings.presentation.SettingsDialogs.ThemeSelectionDialog
import com.islamnotify.sounds.domain.AZAN_SOUNDS
import com.islamnotify.sounds.domain.IQAMA_SOUNDS
import com.islamnotify.sounds.domain.NOTIFY_SOUNDS
import com.islamnotify.events.domain.EventFlags
import com.islamnotify.events.domain.EventsPreferenceKeys
import com.islamnotify.prayer_times.domain.model.PrayerConfig

@Composable
fun NavigationRoot() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Main, // Use the object/class
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(400)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(400)
            )
        },
        // Backward navigation (Back button)
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(400)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(400)
            )
        }
    ) {
        composable<Screen.Main> {
            val viewModel: MainViewModel = hiltViewModel()
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        } else {
                            true
                        }
                        val batteryGranted = (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
                            .isIgnoringBatteryOptimizations(context.packageName)
                        val locationGranted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        viewModel.refreshPermissionState(notificationGranted, batteryGranted, locationGranted)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            // Collect your state here, NOT in the root
            val uiState = viewModel.uiState.collectAsState()
            val countDown = viewModel.countDown.collectAsState()
            val nextPrayer = viewModel.nextPrayer.collectAsState()
            val date = viewModel.hijriDate.collectAsState()
            val isRefreshing = viewModel.isRefreshing.collectAsState()


            MainScreenContent(
                viewModel = viewModel,
                uiState = uiState.value,
                countDown = countDown.value,
                nextPrayer = nextPrayer.value,
                date = date.value,
                isRefreshing = isRefreshing.value,
                onRefresh = { viewModel.refreshData() },
                onSettingsClick = {
                    navController.navigate(Screen.Settings)
                }
            )
        }

        composable<Screen.Settings> {
            val viewModel: SettingsViewModel = hiltViewModel()

            MasterSettingsScreen(
                settingsUiState = viewModel.uiState.collectAsState(),
                onEvent = { event ->
                    when (event) {
                        is SettingsEvent.OnBackClick -> {
                            navController.popBackStack()
                        }

                        is SettingsEvent.OnToggleAutoCalculation -> {
                            viewModel.onToggleAutoCalculation(event.enabled)
                        }

                        is SettingsEvent.OnClickCalculationMethod -> {
                            navController.navigate(CalculationMethodDialog)
                        }

                        is SettingsEvent.OnClickPrayerTimeOffsets -> {
                            navController.navigate(PrayerTimesOffset)
                        }

                        is SettingsEvent.OnClickHijriAdjustment -> {
                            navController.navigate(HijriOffsetDialog)
                        }

                        is SettingsEvent.OnToggleAzanSounds -> {
                            viewModel.onToggleAzanSound(event.enabled)
                        }

                        is SettingsEvent.OnToggleIqamaSounds -> {
                            viewModel.onToggleIqamaSound(event.enabled)
                        }

                        is SettingsEvent.OnClickAzanSoundPicker -> {
                            navController.navigate(AzanSoundPickerDialog)
                        }

                        is SettingsEvent.OnClickIqamaSoundPicker -> {
                            navController.navigate(IqamaSoundPickerDialog)
                        }

                        is SettingsEvent.OnClickNotifySoundPicker -> {
                            navController.navigate(NotifySoundPickerDialog)
                        }

                        is SettingsEvent.OnToggleEventNotifications -> {
                            viewModel.onToggleEventsNotification(event.enabled)
                        }

                        is SettingsEvent.OnClickManageEvents -> {
                            navController.navigate(EventsDialog)
                        }

                        is SettingsEvent.OnClickLanguage -> {
                            navController.navigate(LanguageDialog)

                        }

                        is SettingsEvent.OnClickTheme -> {
                            navController.navigate(ThemeDialog)
                        }

                        is SettingsEvent.OnToggleNotification -> {
                            viewModel.onToggleNotification(event.enabled)
                        }

                        is SettingsEvent.OnClickManageNotifications -> {
                            navController.navigate(NotificationPrayerAdjustmentsDialog)
                        }

                        is SettingsEvent.OnMarkNotificationPermRequested -> {
                            viewModel.markNotificationPermRequested()
                        }
                    }
                }
            )
        }


        // Calculation Method Dialog
        dialog<CalculationMethodDialog> {
            val viewModel: SettingsViewModel =
                hiltViewModel(navController.getBackStackEntry(Screen.Settings))
            val state = viewModel.uiState.collectAsState()
            CalculationMethodSelectionDialog(
                initialMethod = state.value.currentManualMethod,
                onDismiss = { navController.popBackStack() },
                onConfirm = { method ->
                    viewModel.onManualCalculationMethodChanged(method)
                    navController.popBackStack()
                }
            )
        }


        composable<PrayerTimesOffset> {
            val viewModel: PrayerOffsetViewModel = hiltViewModel()
            val config = viewModel.config.collectAsState()
            PrayerTimesOffsetScreen(
                config = config.value,
                onOffsetChanged = { transform -> viewModel.onOffsetChanged(transform) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Hijri Date Offset Dialog
        dialog<HijriOffsetDialog> {
            val viewModel: SettingsViewModel =
                hiltViewModel(navController.getBackStackEntry(Screen.Settings))
            val state = viewModel.uiState.collectAsState()
            SingleSelectDialog(
                title = stringResource(R.string.settings_hijri_date_offset_title),
                items = listOf(-2, -1, 0, 1, 2),
                selectedItem = state.value.currentHijriOffset,
                itemLabel = { offset -> if (offset > 0) "+$offset" else "$offset" },
                onDismiss = { navController.popBackStack() },
                onConfirm = { offset ->
                    viewModel.onHijriOffsetChanged(offset)
                    navController.popBackStack()
                }
            )
        }


        // Notification Prayer Dialog
        dialog<NotificationPrayerAdjustmentsDialog> {
            val viewModel: SettingsViewModel =
                hiltViewModel(navController.getBackStackEntry(Screen.Settings))
            val state = viewModel.uiState.collectAsState()
            val context = LocalContext.current
            val allItems = PrayerVisibilityType.entries
            MultiSelectDialog(
                title = stringResource(R.string.settings_notification_prayer_adjustments_title),
                items = allItems,
                initialSelectedItems = allItems.filter { it.isEnabled(state.value) }.toSet(),
                itemLabel = { context.getString(it.labelRes) },
                onDismiss = { navController.popBackStack() },
                onConfirm = { selectedItems ->
                    viewModel.onPrayerVisibilityFlagsChanged { config ->
                        config.copy(
                            showNextSunrise = PrayerVisibilityType.SUNRISE in selectedItems,
                            showNextDuha = PrayerVisibilityType.DUHA in selectedItems,
                            showNextIqama = PrayerVisibilityType.IQAMA in selectedItems,
                            showNextMidnight = PrayerVisibilityType.MIDNIGHT in selectedItems,
                            showNextLastThird = PrayerVisibilityType.LAST_THIRD in selectedItems
                        )
                    }
                    navController.popBackStack()
                }
            )
        }


// Azan Sound Picker
        dialog<AzanSoundPickerDialog> {
            val viewModel: SettingsViewModel =
                hiltViewModel(navController.getBackStackEntry(Screen.Settings))
            val state = viewModel.uiState.collectAsState()
            val context = LocalContext.current
            SoundPickerDialog(
                title = context.getLocalizedContext().getString(R.string.settings_azan_sound_title),
                items = AZAN_SOUNDS,
                selectedRawResId = state.value.azanSoundResId,
                onDismiss = { navController.popBackStack() },
                onConfirm = { rawResId ->
                    viewModel.onSelectAzanSound(rawResId)
                    navController.popBackStack()
                }
            )
        }

        // Iqama Sound Picker
        dialog<IqamaSoundPickerDialog> {
            val viewModel: SettingsViewModel =
                hiltViewModel(navController.getBackStackEntry(Screen.Settings))
            val state = viewModel.uiState.collectAsState()
            val context = LocalContext.current
            SoundPickerDialog(
                title = context.getLocalizedContext().getString(R.string.settings_iqama_sound_title),
                items = IQAMA_SOUNDS,
                selectedRawResId = state.value.iqamaSoundResId,
                onDismiss = { navController.popBackStack() },
                onConfirm = { rawResId ->
                    viewModel.onSelectIqamaSound(rawResId)
                    navController.popBackStack()
                }
            )
        }

        // Notify Sound Picker
        dialog<NotifySoundPickerDialog> {
            val viewModel: SettingsViewModel =
                hiltViewModel(navController.getBackStackEntry(Screen.Settings))
            val state = viewModel.uiState.collectAsState()
            val context = LocalContext.current
            SoundPickerDialog(
                title = context.getLocalizedContext().getString(R.string.settings_notify_sound_title),
                items = NOTIFY_SOUNDS,
                selectedRawResId = state.value.notifySoundResId,
                onDismiss = { navController.popBackStack() },
                onConfirm = { rawResId ->
                    viewModel.onSelectNotifySound(rawResId)
                    navController.popBackStack()
                }
            )
        }


        // Events Dialog
        dialog<EventsDialog> {
            val viewModel: SettingsViewModel =
                hiltViewModel(navController.getBackStackEntry(Screen.Settings))
            val state = viewModel.uiState.collectAsState()
            val context = LocalContext.current
            val allItems = EventType.entries
            MultiSelectDialog(
                title = stringResource(R.string.settings_adjust_events_title),
                items = allItems,
                initialSelectedItems = allItems.filter { it.getter(state.value.eventFlags) }.toSet(),
                itemLabel = { context.getString(it.labelRes) },
                onDismiss = { navController.popBackStack() },
                onConfirm = { selectedItems ->
                    val newFlags = state.value.eventFlags.copy(
                        mondayFasting = EventType.MONDAY_FASTING in selectedItems,
                        thursdayFasting = EventType.THURSDAY_FASTING in selectedItems,
                        whiteDaysFasting = EventType.WHITE_DAYS_FASTING in selectedItems,
                        arafaFasting = EventType.ARFA_FASTING in selectedItems,
                        tasuaFasting = EventType.TASUA_FASTING in selectedItems,
                        ashoraFasting = EventType.ASHORA_FASTING in selectedItems,
                        shawwalFasting = EventType.SHAWWAL_FASTING in selectedItems,
                        ramadanEvent = EventType.RAMADAN in selectedItems,
                        ramdanLast10DaysEvent = EventType.RAMADAN_LAST_10_DAYS in selectedItems,
                        dhuAlHijjahFirst10DaysEvent = EventType.DHU_AL_HIJJAH_FIRST_10_DAYS in selectedItems,
                        fridayEvent = EventType.FRIDAY in selectedItems,
                        eidAlFitrEvent = EventType.EID_AL_FITR in selectedItems,
                        eidAlAdhaEvent = EventType.EID_AL_ADHA in selectedItems,
                        muharramEvent = EventType.MUHARRAM in selectedItems,
                        rajabEvent = EventType.RAJAB in selectedItems,
                        dhuAlQidaEvent = EventType.DHU_AL_QIDA in selectedItems,
                        dhuAlHijjahEvent = EventType.DHU_AL_HIJJAH in selectedItems
                    )
                    viewModel.onEventsSelectionChanged(newFlags)
                    navController.popBackStack()
                }
            )
        }

        //Language Dialog
        dialog<LanguageDialog> {
            val viewModel: SettingsViewModel =
                hiltViewModel(navController.getBackStackEntry(Screen.Settings))
            val state = viewModel.uiState.collectAsState()
            val context = LocalContext.current
            SingleSelectDialog(
                title = stringResource(R.string.settings_language_title),
                items = LanguageOption.entries,
                selectedItem = state.value.currentLanguageOption,
                itemLabel = { option -> option.toDisplayString(context) },
                onDismiss = { navController.popBackStack() },
                onConfirm = { option ->
                    viewModel.onLanguageChanged(option)
                    navController.popBackStack()
                }
            )
        }


        // theme dialog
        dialog<ThemeDialog> {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Screen.Settings)
            }
            val viewModel: SettingsViewModel = hiltViewModel(parentEntry)
            val state = viewModel.preferencesConfig.collectAsState()
            ThemeSelectionDialog(
                initialTheme = state.value?.theme
                    ?: AppThemeTypes.GREEN_LIGHT, // Assuming this exists in your UI state
                onDismiss = { navController.popBackStack() },
                onConfirm = { selectedTheme ->
                    viewModel.onThemeChanged(selectedTheme)
                    navController.popBackStack()

                })
        }




    }
}


@Keep
@Serializable
data object CalculationMethodDialog

@Keep
@Serializable
data object PrayerTimesOffset

@Keep
@Serializable
data object HijriOffsetDialog

@Keep
@Serializable
data object AzanSoundPickerDialog // Sound with button

@Keep
@Serializable
data object IqamaSoundPickerDialog // Sound with button

@Keep
@Serializable
data object NotifySoundPickerDialog // Sound with button

@Keep
@Serializable
data object NotificationPrayerAdjustmentsDialog // Checkbox

@Keep
@Serializable
data object EventsDialog // Checkbox

@Keep
@Serializable
data object ThemeDialog

@Keep
@Serializable
data object LanguageDialog

private fun LanguageOption.toDisplayString(context: Context): String {
    return when (this) {
        LanguageOption.ENGLISH -> context.getString(R.string.settings_language_en)
        LanguageOption.ARABIC -> context.getString(R.string.settings_language_ar)
        LanguageOption.AUTO -> context.getString(R.string.settings_language_system_default)
    }
}

private enum class PrayerVisibilityType(val labelRes: Int) {
    SUNRISE(R.string.sunrise_name),
    DUHA(R.string.duha_name),
    IQAMA(R.string.iqama_name),
    MIDNIGHT(R.string.midnight_name),
    LAST_THIRD(R.string.last_third_name),
}

private fun PrayerVisibilityType.isEnabled(state: SettingsUiState) = when (this) {
    PrayerVisibilityType.SUNRISE -> state.showNextSunrise
    PrayerVisibilityType.DUHA -> state.showNextDuha
    PrayerVisibilityType.IQAMA -> state.showNextIqama
    PrayerVisibilityType.MIDNIGHT -> state.showNextMidnight
    PrayerVisibilityType.LAST_THIRD -> state.showNextLastThird
}

private enum class EventType(val labelRes: Int, val getter: (EventFlags) -> Boolean) {
    MONDAY_FASTING(R.string.monday_fasting_title, { it.mondayFasting }),
    THURSDAY_FASTING(R.string.thursday_fasting_title, { it.thursdayFasting }),
    WHITE_DAYS_FASTING(R.string.white_days_fasting_title, { it.whiteDaysFasting }),
    ARFA_FASTING(R.string.arfa_fasting_title, { it.arafaFasting }),
    TASUA_FASTING(R.string.tasua_fasting_title, { it.tasuaFasting }),
    ASHORA_FASTING(R.string.ashora_fasting_title, { it.ashoraFasting }),
    SHAWWAL_FASTING(R.string.shawwal_fasting_title, { it.shawwalFasting }),
    RAMADAN(R.string.ramadan_event_title, { it.ramadanEvent }),
    RAMADAN_LAST_10_DAYS(R.string.ramadan_last_10_days_event_title, { it.ramdanLast10DaysEvent }),
    DHU_AL_HIJJAH_FIRST_10_DAYS(R.string.dhu_al_hijjah_first_10_days_event_title, { it.dhuAlHijjahFirst10DaysEvent }),
    FRIDAY(R.string.friday_event_title, { it.fridayEvent }),
    EID_AL_FITR(R.string.eid_al_fitr_event_title, { it.eidAlFitrEvent }),
    EID_AL_ADHA(R.string.eid_al_adha_event_title, { it.eidAlAdhaEvent }),
    MUHARRAM(R.string.muharram_event_title, { it.muharramEvent }),
    RAJAB(R.string.rajab_event_title, { it.rajabEvent }),
    DHU_AL_QIDA(R.string.dhu_al_qadah_event_title, { it.dhuAlQidaEvent }),
    DHU_AL_HIJJAH(R.string.dhu_al_hijjah_event_title, { it.dhuAlHijjahEvent }),
}