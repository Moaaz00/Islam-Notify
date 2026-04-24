package com.islamnotify.common

import android.content.Context
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.islamnotify.main.presentation.MainScreenContent
import com.islamnotify.main.presentation.MainViewModel
import com.islamnotify.settings.TestScreenContent
import com.islamnotify.settings.presentation.Manrope
import com.islamnotify.settings.presentation.MasterSettingsScreen
import com.islamnotify.settings.presentation.SettingsEvent
import com.islamnotify.settings.presentation.SettingsViewModel
import com.islamnotify.ui.theme.AppThemeTypes
import kotlinx.serialization.Serializable
import com.islamnotify.R
import com.islamnotify.common.AppUtils.getLocalizedContext
import com.islamnotify.settings.presentation.PrayerOffsetAdjustmentsScreen.PrayerTimesOffsetScreen
import com.islamnotify.settings.presentation.SettingsDialogs.MultiSelectDialog
import com.islamnotify.settings.presentation.SettingsDialogs.SingleSelectDialog
import com.islamnotify.settings.presentation.SettingsDialogs.ThemeSelectionDialog

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
                    }
                }
            )
        }


        //Calculation Dialog
        dialog<CalculationMethodDialog> {
            val viewModel: SettingsViewModel =
                hiltViewModel(navController.getBackStackEntry(Screen.Settings))
            SingleSelectDialog(
                title = "Calculation Methods",
                items = listOf("Egyptian", "umm al qura", "other"),
                selectedItem = viewModel.uiState.collectAsState().value.language,
                itemLabel = { it },
                onDismiss = { navController.popBackStack() },
                onConfirm = { lang ->
                    /* viewModel.onLanguageChanged(lang) */
                    navController.popBackStack()
                }
            )
        }


        composable<PrayerTimesOffset> {
            PrayerTimesOffsetScreen()
        }

        //Hijri offsets Dialog
        dialog<HijriOffsetDialog> {
            val viewModel: SettingsViewModel =
                hiltViewModel(navController.getBackStackEntry(Screen.Settings))
            SingleSelectDialog(
                title = "Hijri Date Offsets",
                items = listOf("-2", "-1", "0","1","2"),
                selectedItem = viewModel.uiState.collectAsState().value.language,
                itemLabel = { it },
                onDismiss = { navController.popBackStack() },
                onConfirm = { lang ->
                    /* viewModel.onLanguageChanged(lang) */
                    navController.popBackStack()
                }
            )
        }


 // Notification Prayer Dialog
        dialog<NotificationPrayerAdjustmentsDialog> {
            val viewModel: SettingsViewModel =
                hiltViewModel(navController.getBackStackEntry(Screen.Settings))
            MultiSelectDialog(
                title = "Notify for Prayers",
                items = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"),
                initialSelectedItems = setOf("Fajr", "Maghrib"), // Get from VM
                itemLabel = { it },
                onDismiss = { navController.popBackStack() },
                onConfirm = { selectedSet ->
                    // viewModel.onUpdateNotificationPrayers(selectedSet)
                    navController.popBackStack()
                }
            )
        }


// Azan Sound Picker
        dialog<AzanSoundPickerDialog> {
            val viewModel: SettingsViewModel =
                hiltViewModel(navController.getBackStackEntry(Screen.Settings))
            SingleSelectDialog(
                title = "Choose Sound azan",
                items = listOf("Default", "Al-Minshawy", "Al-Husary", "Adhan Mecca"),
                selectedItem = "Default",
                itemLabel = { it },
                onDismiss = { navController.popBackStack() },
                onConfirm = { sound -> navController.popBackStack() },
                topContent = {
                    Button(
                        onClick = { /* Handle file picker */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(painterResource(R.drawable.ic_add), contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Custom Sound", fontFamily = Manrope)
                    }
                }
            )
        }


        // Iqama Sound Picker
        dialog<IqamaSoundPickerDialog> {
            val viewModel: SettingsViewModel =
                hiltViewModel(navController.getBackStackEntry(Screen.Settings))
            SingleSelectDialog(
                title = "Choose Sound iqama",
                items = listOf("Default", "Al-Minshawy", "Al-Husary", "Adhan Mecca"),
                selectedItem = "Default",
                itemLabel = { it },
                onDismiss = { navController.popBackStack() },
                onConfirm = { sound -> navController.popBackStack() },
                topContent = {
                    Button(
                        onClick = { /* Handle file picker */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(painterResource(R.drawable.ic_add), contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Custom Sound", fontFamily = Manrope)
                    }
                }
            )
        }


        // Notify Sound Picker
        dialog<NotifySoundPickerDialog> {
            val viewModel: SettingsViewModel =
                hiltViewModel(navController.getBackStackEntry(Screen.Settings))
            SingleSelectDialog(
                title = "Choose Sound notify",
                items = listOf("Default", "Al-Minshawy", "Al-Husary", "Adhan Mecca"),
                selectedItem = "Default",
                itemLabel = { it },
                onDismiss = { navController.popBackStack() },
                onConfirm = { sound -> navController.popBackStack() },
                topContent = {
                    Button(
                        onClick = { /* Handle file picker */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(painterResource(R.drawable.ic_add), contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Custom Sound", fontFamily = Manrope)
                    }
                }
            )
        }


        // Events Dialog
        dialog<EventsDialog> {
            val viewModel: SettingsViewModel =
                hiltViewModel(navController.getBackStackEntry(Screen.Settings))
            MultiSelectDialog(
                title = "Events Adjustments",
                items = listOf("Monday", "thursday", "White Days", "Ramadan", "Eid"),
                initialSelectedItems = setOf("White Days", "Monday"), // Get from VM
                itemLabel = { it },
                onDismiss = { navController.popBackStack() },
                onConfirm = { selectedSet ->
                    // viewModel.onUpdateNotificationPrayers(selectedSet)
                    navController.popBackStack()
                }
            )
        }

        //Language Dialog
        dialog<LanguageDialog> {
            val viewModel: SettingsViewModel =
                hiltViewModel(navController.getBackStackEntry(Screen.Settings))
            SingleSelectDialog(
                title = "Select Language",
                items = listOf("English", "Arabic", "French"),
                selectedItem = viewModel.uiState.collectAsState().value.language,
                itemLabel = { it },
                onDismiss = { navController.popBackStack() },
                onConfirm = { lang ->
                    /* viewModel.onLanguageChanged(lang) */
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
