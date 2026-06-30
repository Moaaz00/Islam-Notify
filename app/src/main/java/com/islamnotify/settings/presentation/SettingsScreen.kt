package com.islamnotify.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.islamnotify.R
import com.islamnotify.common.AppUtils.getLocalizedContext
import com.islamnotify.intro.presentation.DeniedPermissionDialog
import com.islamnotify.intro.presentation.NotificationPermissionTextProvider

// 1. TYPOGRAPHY SYSTEM
// Centralizing fonts ensures consistency across the whole app
val Manrope = FontFamily(Font(R.font.readex))


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterSettingsScreen(
    settingsUiState: State<SettingsUiState>,
    onEvent: (SettingsEvent) -> Unit
) {
    val context = LocalContext.current

    var showNotificationPermDialog by remember { mutableStateOf(false) }
    var isNotificationPermPermanentlyDeclined by remember { mutableStateOf(false) }

    val notificationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        onEvent(SettingsEvent.OnMarkNotificationPermRequested)
        if (granted) onEvent(SettingsEvent.OnToggleNotification(true))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ) {
                val granted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted && settingsUiState.value.isNotificationEnabled) {
                    onEvent(SettingsEvent.OnToggleNotification(false))
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (showNotificationPermDialog) {
        DeniedPermissionDialog(
            permissionTextProvider = NotificationPermissionTextProvider(),
            isPermanentlyDeclined = isNotificationPermPermanentlyDeclined,
            onDismiss = { showNotificationPermDialog = false },
            onOkClick = {
                showNotificationPermDialog = false
                onEvent(SettingsEvent.OnMarkNotificationPermRequested)
                notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
            onGoToAppSettingsClick = {
                showNotificationPermDialog = false
                context.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                )
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = {
                    Text(
                        stringResource(R.string.settings_screen_title),
                        fontFamily = Manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        lineHeight = 24.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SettingsEvent.OnBackClick) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back_arrow),
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp) // Standard gap between sections
        ) {
            item {
                SettingsSection(title = stringResource(R.string.settings_calculation_header_title)) {
                    SettingsToggleItem(
                        title = stringResource(R.string.settings_auto_calculation_title),
                        subtitle = if (settingsUiState.value.autoCalcMethod != null) settingsUiState.value.autoCalcMethod
                        else stringResource(R.string.settings_auto_calculation_subtitle),
                        fadeWhenDisabled = true,
                        isChecked = settingsUiState.value.isAutoCalcEnabled,
                        onCheckedChange = { onEvent(SettingsEvent.OnToggleAutoCalculation(it)) }
                    )
                    SettingsNavigationItem(
                        title = stringResource(R.string.settings_manual_calculation_title),
                        subtitle = settingsUiState.value.calculationMethod,
                        showDivider = false,
                        clickable = !settingsUiState.value.isAutoCalcEnabled,
                        onClick = { onEvent(SettingsEvent.OnClickCalculationMethod) }
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_time_adjustments_header_title)) {
                    SettingsNavigationItem(
                        title = stringResource(R.string.settings_prayer_time_offsets_title),
                        subtitle = stringResource(R.string.settings_prayer_time_offsets_subtitle),
                        onClick = { onEvent(SettingsEvent.OnClickPrayerTimeOffsets) }
                    )
                    SettingsNavigationItem(
                        title = stringResource(R.string.settings_hijri_date_offset_title),
                        subtitle = settingsUiState.value.hijriDateAdjustment,
                        showDivider = false,
                        onClick = { onEvent(SettingsEvent.OnClickHijriAdjustment) }
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_sounds_header_title)) {
                    SettingsToggleItem(
                        title = stringResource(R.string.settings_enable_azan_sound_title),
                        subtitle = stringResource(R.string.settings_enable_azan_sound_subtitle),
                        isChecked = settingsUiState.value.isAzanSoundEnabled,
                        onCheckedChange = { onEvent(SettingsEvent.OnToggleAzanSounds(it)) }
                    )
                    SettingsToggleItem(
                        title = stringResource(R.string.settings_enable_iqama_sound_title),
                        subtitle = stringResource(R.string.settings_enable_iqama_sound_subtitle),
                        isChecked = settingsUiState.value.isIqamaSoundEnabled,
                        onCheckedChange = { onEvent(SettingsEvent.OnToggleIqamaSounds(it)) }
                    )
                    SettingsNavigationItem(
                        title = stringResource(R.string.settings_azan_sound_title),
                        subtitle = settingsUiState.value.azanSoundName.ifEmpty { stringResource(R.string.settings_azan_sound_subtitle) },
                        onClick = { onEvent(SettingsEvent.OnClickAzanSoundPicker) })
                    SettingsNavigationItem(
                        title = stringResource(R.string.settings_iqama_sound_title),
                        subtitle = settingsUiState.value.iqamaSoundName.ifEmpty { stringResource(R.string.settings_iqama_sound_subtitle) },
                        onClick = { onEvent(SettingsEvent.OnClickIqamaSoundPicker) })
                    SettingsNavigationItem(
                        title = stringResource(R.string.settings_notify_sound_title),
                        subtitle = settingsUiState.value.notifySoundName.ifEmpty { stringResource(R.string.settings_notify_sound_subtitle) },
                        onClick = { onEvent(SettingsEvent.OnClickNotifySoundPicker) },
                        showDivider = false
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_notification_header_title)) {
                    SettingsToggleItem(
                        title = stringResource(R.string.settings_enable_notification_title),
                        subtitle = stringResource(R.string.settings_enable_notification_subtitle),
                        isChecked = settingsUiState.value.isNotificationEnabled,
                        onCheckedChange = { enabled ->
                            if (!enabled) {
                                onEvent(SettingsEvent.OnToggleNotification(false))
                            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                onEvent(SettingsEvent.OnToggleNotification(true))
                            } else {
                                val granted = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    onEvent(SettingsEvent.OnToggleNotification(true))
                                } else {
                                    val showRationale = (context as? android.app.Activity)
                                        ?.shouldShowRequestPermissionRationale(
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) ?: false
                                    when {
                                        showRationale -> {
                                            isNotificationPermPermanentlyDeclined = false
                                            showNotificationPermDialog = true
                                        }
                                        settingsUiState.value.hasRequestedNotificationPerm -> {
                                            isNotificationPermPermanentlyDeclined = true
                                            showNotificationPermDialog = true
                                        }
                                        else -> {
                                            isNotificationPermPermanentlyDeclined = false
                                            showNotificationPermDialog = true
                                        }
                                    }
                                }
                            }
                        }
                    )

                    SettingsNavigationItem(
                        title = stringResource(R.string.settings_notification_prayer_adjustments_title),
                        subtitle = stringResource(R.string.settings_notification_prayer_adjustments_subtitle),
                        showDivider = false,
                        onClick = { onEvent(SettingsEvent.OnClickManageNotifications) }
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_events_header_title)) {
                    SettingsToggleItem(
                        title = stringResource(R.string.settings_enable_events_title),
                        subtitle = stringResource(R.string.settings_enable_events_subtitle),
                        isChecked = settingsUiState.value.isEventsEnabled,
                        onCheckedChange = { onEvent(SettingsEvent.OnToggleEventNotifications(it)) }
                    )
                    SettingsNavigationItem(
                        title = stringResource(R.string.settings_adjust_events_title),
                        subtitle = stringResource(R.string.settings_adjust_events_subtitle),
                        showDivider = false,
                        clickable = settingsUiState.value.isEventsEnabled,
                        onClick = { onEvent(SettingsEvent.OnClickManageEvents) }
                    )
                }

            }

            item {
                SettingsSection(title = stringResource(R.string.settings_preferences_header_title)) {
                    SettingsNavigationItem(
                        title = stringResource(R.string.settings_language_title),
                        subtitle = settingsUiState.value.language
                    ) { onEvent(SettingsEvent.OnClickLanguage) }
                    SettingsNavigationItem(
                        title = stringResource(R.string.settings_theme_title),
                        subtitle = settingsUiState.value.theme,
                        showDivider = false
                    ) { onEvent(SettingsEvent.OnClickTheme) }
                }
            }
        }
    }
}

// 2. REUSABLE SECTION COMPONENT
@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            fontFamily = Manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column(content = content)
        }
    }
}

// 3. TOGGLE ITEM
@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String? = null,
    isChecked: Boolean,
    fadeWhenDisabled: Boolean = false,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean = true
) {
//    var checked by remember { mutableStateOf(initialValue) }
    val contentAlpha = if (fadeWhenDisabled && !isChecked) 0.38f else 1f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth()
                .heightIn(min = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 24.dp)
                    .graphicsLayer(alpha = contentAlpha),
            ) {
                Text(
                    text = title,
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Text(
                        text = it,
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Switch(
                checked = isChecked,
                onCheckedChange = {
                    onCheckedChange(it)
                },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    checkedThumbColor = Color.White,
                    uncheckedThumbColor = Color.White
                )
            )
        }
        if (showDivider) SettingDivider()
    }
}

// 4. NAVIGATION ITEM
@Composable
fun SettingsNavigationItem(
    title: String,
    subtitle: String? = null,
    clickable: Boolean = true,
    showDivider: Boolean = true,
    onClick: () -> Unit = {}
) {
    val contentAlpha = if (clickable) 1f else 0.38f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = clickable) { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxWidth()
                .graphicsLayer(alpha = contentAlpha),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier
                .weight(1f)
                .padding(end = 24.dp)) {
                Text(
                    text = title,
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Text(
                        text = it,
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                painter = painterResource(R.drawable.ic_settings_navigation_arrow),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
        if (showDivider) SettingDivider()
    }
}

@Composable
fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}


//package com.islamnotify.settings.presentation
//
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.Font
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.unit.dp
//import com.islamnotify.R
//
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//
//val Manrope = FontFamily(
//    Font(R.font.readex)
//)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MasterSettingsScreen() {
//    Scaffold(
//        containerColor = MaterialTheme.colorScheme.background,
//        topBar = {
//            TopAppBar(
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
//                    titleContentColor = MaterialTheme.colorScheme.onBackground
//                ),
//                title = {
//                    Text(
//                        "Settings",
//                        fontFamily = Manrope,
//                        fontWeight = FontWeight.SemiBold,
//                        fontSize = 18.sp
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = { /* Handle back */ }) {
//                        Icon(
//                            painter = painterResource(R.drawable.ic_back_arrow),
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.onBackground,
//                            modifier = Modifier.size(24.dp)
//                        )
//                    }
//                }
//            )
//        }
//    ) { innerPadding ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .padding(horizontal = 16.dp),
//            verticalArrangement = Arrangement.spacedBy(24.dp),
//            contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
//        ) {
//            item {
//                SettingsSection(title = "Calculation Settings") {
//                    SettingsToggleItem(title = "Auto Calculation Method","method chosen based on location", initialValue = true)
//                    SettingsNavigationItem(
//                        title = "Calculation Method",
//                        subtitle = "Egypt Government Method"
//                    )
//                }
//            }
//
//            item {
//                SettingsSection(title = "Time Adjustments") {
//                    SettingsNavigationItem(
//                        title = "Prayer Time Offsets",
//                        subtitle = "Adjust Prayer Times"
//                    )
//                    SettingsNavigationItem(title = "Hijri Date Adjustment", subtitle = "+1 Day")
//                }
//            }
//
//            item {
//                SettingsSection(title = "Sounds") {
//                    SettingsToggleItem(title = "Enable Azan Sounds","enable/disable all azan sounds", initialValue = true)
//                    SettingsToggleItem(title = "Enable Iqama Sounds","enable/disable all iqama sounds", initialValue = true)
//                    SettingsNavigationItem(title = "Azan Sound", subtitle = "Al-Minshawy")
//                    SettingsNavigationItem(title = "Iqama Sound", subtitle = "Al-Husary")
//                    SettingsNavigationItem(title = "Notify Sound", subtitle = "Sound-01")
//                }
//            }
//
//            item {
//                SettingsSection(title = "Notification Settings") {
//                    SettingsToggleItem(title = "Always-On Notification","Show a prayer times notification", initialValue = true)
//                    SettingsNavigationItem(
//                        title = "Next Prayer Adjustments",
//                        "choose which prayers to show"
//                    )
//                }
//            }
//
//            item {
//                SettingsSection(title = "Events Settings") {
//                    SettingsToggleItem(title = "Enable Events Notifications","notify the user for events", initialValue = true)
//                    SettingsNavigationItem(
//                        title = "Events Adjustments ",
//                        "choose which event to set"
//                    )
//                }
//            }
//
//            item {
//                SettingsSection(title = "Preferences") {
//                    SettingsNavigationItem(title = "Language", subtitle = "English (Auto)")
//                    SettingsNavigationItem(title = "Theme", subtitle = "Olive")
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
//    Column {
//        Text(
//            text = title,
//            fontFamily = Manrope,
//            fontWeight = FontWeight.Normal,
//            fontSize = 12.sp,
//            color = MaterialTheme.colorScheme.primary,
//            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
//        )
//        Card(
//            shape = RoundedCornerShape(15.dp),
//            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
//        ) {
//            Column(content = content)
//        }
//    }
//}
//
//@Composable
//fun SettingsToggleItem(title: String, subtitle: String = String(), initialValue: Boolean) {
//    var checked by remember { mutableStateOf(initialValue) }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Column {
//            Text(
//                text = title,
//                fontFamily = Manrope,
//                fontSize = 15.sp,
//                fontWeight = FontWeight.Normal,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//            if (subtitle.isNotEmpty()) {
//                Text(
//                    text = subtitle,
//                    fontFamily = Manrope,
//                    fontWeight = FontWeight.Normal,
//                    fontSize = 12.sp,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//                )
//            }
//        }
//
//        Switch(
//            checked = checked,
//            onCheckedChange = { checked = it },
//            colors = SwitchDefaults.colors(
//                checkedThumbColor = Color.White,
//                checkedTrackColor = MaterialTheme.colorScheme.primary,
//                uncheckedTrackColor = Color(0xFFBDBDBD),
//                uncheckedThumbColor = Color.White,
//                uncheckedBorderColor = Color(0xFFBDBDBD)
//            )
//        )
//    }
//    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.background)
//}
//
//@Composable
//fun SettingsNavigationItem(title: String, subtitle: String? = null) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { }
//            .padding(16.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Column {
//            Text(
//                text = title,
//                fontFamily = Manrope,
//                fontSize = 15.sp,
//                fontWeight = FontWeight.Normal,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//            if (subtitle != null) {
//                Text(
//                    text = subtitle,
//                    fontFamily = Manrope,
//                    fontWeight = FontWeight.Normal,
//                    fontSize = 12.sp,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//                )
//            }
//        }
//        Icon(
//            painter = painterResource(R.drawable.ic_settings_navigation_arrow),
//            contentDescription = null,
//            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
//            modifier = Modifier.size(24.dp)
//        )
//    }
//    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.background)
//}