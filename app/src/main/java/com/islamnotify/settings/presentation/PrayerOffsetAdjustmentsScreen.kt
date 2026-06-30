package com.islamnotify.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamnotify.R
import com.islamnotify.prayer_times.domain.model.PrayerConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesOffsetScreen(
    config: PrayerConfig,
    onOffsetChanged: ((PrayerConfig) -> PrayerConfig) -> Unit,
    onBackClick: () -> Unit
) {
    val defaults = PrayerConfig()
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
                        stringResource(R.string.settings_prayer_time_offsets_title),
                        fontFamily = Manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back_arrow),
                            contentDescription = null,
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SettingsSection(title = stringResource(R.string.settings_offset_main_prayers_header)) {
                    AdjustAllRow(
                        onDecrement = {
                            onOffsetChanged { c ->
                                c.copy(
                                    fajrOffset = (c.fajrOffset - 1).coerceIn(-60, 60),
                                    sunriseOffset = (c.sunriseOffset - 1).coerceIn(-60, 60),
                                    zuhrOffset = (c.zuhrOffset - 1).coerceIn(-60, 60),
                                    asrOffset = (c.asrOffset - 1).coerceIn(-60, 60),
                                    sunsetOffset = (c.sunsetOffset - 1).coerceIn(-60, 60),
                                    ishaOffset = (c.ishaOffset - 1).coerceIn(-60, 60)
                                )
                            }
                        },
                        onReset = {
                            onOffsetChanged { c ->
                                c.copy(
                                    fajrOffset = defaults.fajrOffset,
                                    sunriseOffset = defaults.sunriseOffset,
                                    zuhrOffset = defaults.zuhrOffset,
                                    asrOffset = defaults.asrOffset,
                                    sunsetOffset = defaults.sunsetOffset,
                                    ishaOffset = defaults.ishaOffset
                                )
                            }
                        },
                        onIncrement = {
                            onOffsetChanged { c ->
                                c.copy(
                                    fajrOffset = (c.fajrOffset + 1).coerceIn(-60, 60),
                                    sunriseOffset = (c.sunriseOffset + 1).coerceIn(-60, 60),
                                    zuhrOffset = (c.zuhrOffset + 1).coerceIn(-60, 60),
                                    asrOffset = (c.asrOffset + 1).coerceIn(-60, 60),
                                    sunsetOffset = (c.sunsetOffset + 1).coerceIn(-60, 60),
                                    ishaOffset = (c.ishaOffset + 1).coerceIn(-60, 60)
                                )
                            }
                        }
                    )
                    OffsetRow(
                        name = stringResource(R.string.fajr_name),
                        value = config.fajrOffset,
                        onValueChange = { onOffsetChanged { c -> c.copy(fajrOffset = it) } }
                    )
                    OffsetRow(
                        name = stringResource(R.string.sunrise_name),
                        value = config.sunriseOffset,
                        onValueChange = { onOffsetChanged { c -> c.copy(sunriseOffset = it) } }
                    )
                    OffsetRow(
                        name = stringResource(R.string.zuhr_name),
                        value = config.zuhrOffset,
                        onValueChange = { onOffsetChanged { c -> c.copy(zuhrOffset = it) } }
                    )
                    OffsetRow(
                        name = stringResource(R.string.asr_name),
                        value = config.asrOffset,
                        onValueChange = { onOffsetChanged { c -> c.copy(asrOffset = it) } }
                    )
                    OffsetRow(
                        name = stringResource(R.string.sunset_name),
                        value = config.sunsetOffset,
                        onValueChange = { onOffsetChanged { c -> c.copy(sunsetOffset = it) } }
                    )
                    OffsetRow(
                        name = stringResource(R.string.isha_name),
                        value = config.ishaOffset,
                        showDivider = false,
                        onValueChange = { onOffsetChanged { c -> c.copy(ishaOffset = it) } }
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_offset_iqama_header)) {
                    AdjustAllRow(
                        onDecrement = {
                            onOffsetChanged { c ->
                                c.copy(
                                    iqamaFajrOffset = (c.iqamaFajrOffset - 1).coerceIn(-60, 60),
                                    iqamaZuhrOffset = (c.iqamaZuhrOffset - 1).coerceIn(-60, 60),
                                    iqamaAsrOffset = (c.iqamaAsrOffset - 1).coerceIn(-60, 60),
                                    iqamaSunsetOffset = (c.iqamaSunsetOffset - 1).coerceIn(-60, 60),
                                    iqamaIshaOffset = (c.iqamaIshaOffset - 1).coerceIn(-60, 60)
                                )
                            }
                        },
                        onReset = {
                            onOffsetChanged { c ->
                                c.copy(
                                    iqamaFajrOffset = defaults.iqamaFajrOffset,
                                    iqamaZuhrOffset = defaults.iqamaZuhrOffset,
                                    iqamaAsrOffset = defaults.iqamaAsrOffset,
                                    iqamaSunsetOffset = defaults.iqamaSunsetOffset,
                                    iqamaIshaOffset = defaults.iqamaIshaOffset
                                )
                            }
                        },
                        onIncrement = {
                            onOffsetChanged { c ->
                                c.copy(
                                    iqamaFajrOffset = (c.iqamaFajrOffset + 1).coerceIn(-60, 60),
                                    iqamaZuhrOffset = (c.iqamaZuhrOffset + 1).coerceIn(-60, 60),
                                    iqamaAsrOffset = (c.iqamaAsrOffset + 1).coerceIn(-60, 60),
                                    iqamaSunsetOffset = (c.iqamaSunsetOffset + 1).coerceIn(-60, 60),
                                    iqamaIshaOffset = (c.iqamaIshaOffset + 1).coerceIn(-60, 60)
                                )
                            }
                        }
                    )
                    OffsetRow(
                        name = stringResource(R.string.iqama_fajr_name),
                        value = config.iqamaFajrOffset,
                        onValueChange = { onOffsetChanged { c -> c.copy(iqamaFajrOffset = it) } }
                    )
                    OffsetRow(
                        name = stringResource(R.string.iqama_zuhr_name),
                        value = config.iqamaZuhrOffset,
                        onValueChange = { onOffsetChanged { c -> c.copy(iqamaZuhrOffset = it) } }
                    )
                    OffsetRow(
                        name = stringResource(R.string.iqama_asr_name),
                        value = config.iqamaAsrOffset,
                        onValueChange = { onOffsetChanged { c -> c.copy(iqamaAsrOffset = it) } }
                    )
                    OffsetRow(
                        name = stringResource(R.string.iqama_sunset_name),
                        value = config.iqamaSunsetOffset,
                        onValueChange = { onOffsetChanged { c -> c.copy(iqamaSunsetOffset = it) } }
                    )
                    OffsetRow(
                        name = stringResource(R.string.iqama_isha_name),
                        value = config.iqamaIshaOffset,
                        showDivider = false,
                        onValueChange = { onOffsetChanged { c -> c.copy(iqamaIshaOffset = it) } }
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_offset_additional_header)) {
                    AdjustAllRow(
                        onDecrement = {
                            onOffsetChanged { c ->
                                c.copy(
                                    duhaSunriseOffset = (c.duhaSunriseOffset - 1).coerceIn(-60, 60),
                                    midnightOffset = (c.midnightOffset - 1).coerceIn(-60, 60),
                                    lastThirdOffset = (c.lastThirdOffset - 1).coerceIn(-60, 60)
                                )
                            }
                        },
                        onReset = {
                            onOffsetChanged { c ->
                                c.copy(
                                    duhaSunriseOffset = defaults.duhaSunriseOffset,
                                    midnightOffset = defaults.midnightOffset,
                                    lastThirdOffset = defaults.lastThirdOffset
                                )
                            }
                        },
                        onIncrement = {
                            onOffsetChanged { c ->
                                c.copy(
                                    duhaSunriseOffset = (c.duhaSunriseOffset + 1).coerceIn(-60, 60),
                                    midnightOffset = (c.midnightOffset + 1).coerceIn(-60, 60),
                                    lastThirdOffset = (c.lastThirdOffset + 1).coerceIn(-60, 60)
                                )
                            }
                        }
                    )
                    OffsetRow(
                        name = stringResource(R.string.duha_name),
                        value = config.duhaSunriseOffset,
                        onValueChange = { onOffsetChanged { c -> c.copy(duhaSunriseOffset = it) } }
                    )
                    OffsetRow(
                        name = stringResource(R.string.midnight_name),
                        value = config.midnightOffset,
                        onValueChange = { onOffsetChanged { c -> c.copy(midnightOffset = it) } }
                    )
                    OffsetRow(
                        name = stringResource(R.string.last_third_name),
                        value = config.lastThirdOffset,
                        showDivider = false,
                        onValueChange = { onOffsetChanged { c -> c.copy(lastThirdOffset = it) } }
                    )
                }
            }

            item { Spacer(Modifier.navigationBarsPadding()) }
        }
    }
}

@Composable
private fun RepeatingIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var didRepeat by remember { mutableStateOf(false) }
    val currentOnClick by rememberUpdatedState(onClick)

    LaunchedEffect(isPressed) {
        if (isPressed) {
            didRepeat = false
            delay(400L)
            didRepeat = true
            while (true) {
                currentOnClick()
                delay(80L)
            }
        } else {
            // Reset after onClick fires (onClick fires before this coroutine runs)
            // so the next single click is not suppressed
            didRepeat = false
        }
    }

    IconButton(
        onClick = { if (!didRepeat) currentOnClick() },
        modifier = modifier,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
private fun AdjustAllRow(
    onDecrement: () -> Unit,
    onReset: () -> Unit,
    onIncrement: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_offset_adjust_all),
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RepeatingIconButton(
                    onClick = onDecrement,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_remove),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box(
                    modifier = Modifier.widthIn(min = 56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                RepeatingIconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        SettingDivider()
    }
}

@Composable
private fun OffsetRow(
    name: String,
    value: Int,
    showDivider: Boolean = true,
    onValueChange: (Int) -> Unit
) {
    val minuteSymbol = stringResource(R.string.remaining_minutes_symbol)
    val valueLabel = "${if (value > 0) "+" else ""}$value $minuteSymbol"

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                fontFamily = Manrope,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RepeatingIconButton(
                    onClick = { onValueChange((value - 1).coerceIn(-60, 60)) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_remove),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = valueLabel,
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 56.dp)
                )

                RepeatingIconButton(
                    onClick = { onValueChange((value + 1).coerceIn(-60, 60)) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        if (showDivider) SettingDivider()
    }
}
