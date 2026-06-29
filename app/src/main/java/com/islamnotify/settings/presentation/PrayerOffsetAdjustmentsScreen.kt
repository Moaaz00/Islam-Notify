package com.islamnotify.settings.presentation

import androidx.compose.foundation.layout.Arrangement
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
                IconButton(
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

                IconButton(
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
