package com.islamnotify.alarms.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.islamnotify.alarms.AlarmRelations
import com.islamnotify.alarms.AlarmStatus
import com.islamnotify.alarms.PrayerAlarm
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import java.time.DayOfWeek

@Composable
fun AlarmCard(
    alarm: PrayerAlarm,
    onCardClick: (Int) -> Unit,
    onToggleStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onCardClick(alarm.id) }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1.0f)
            ) {
                Text(
                    text = alarm.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                val formattedPrayer = alarm.prayer.name.lowercase().replaceFirstChar { it.uppercase() }
                val formattedRelation = alarm.relation.name.lowercase()

                Text(
                    text = "$formattedPrayer ($formattedRelation)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Repeat: " + alarm.daysOfWeek.joinToString { it.name.take(3) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = alarm.status == AlarmStatus.ENABLED,
                onCheckedChange = { onToggleStatus() }
            )
        }
    }
}

@Composable
fun AlarmAppNavigation(
    modifier: Modifier = Modifier,
    viewModel: AlarmsViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val alarms by viewModel.alarmsState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "alarms_list",
        modifier = modifier
    ) {
        composable("alarms_list") {
            AlarmsScreen(
                alarms = alarms,
                onNavigateToEdit = { id ->
                    navController.navigate("alarm_edit/$id")
                },
                onToggleStatus = { alarm -> // Resolved: Clean single-argument lambda
                    viewModel.toggleAlarmStatus(alarm)
                }
            )
        }

        composable(
            route = "alarm_edit/{alarmId}",
            arguments = listOf(
                navArgument("alarmId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getInt("alarmId") ?: -1
            AlarmEditScreen(
                alarmId = alarmId,
                onSaveSuccess = { navController.popBackStack() },
                onDeleteSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    alarmId: Int,
    onSaveSuccess: () -> Unit,
    onDeleteSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: AlarmsViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(alarmId) {
        viewModel.loadAlarm(alarmId)
    }

    val alarm = viewModel.currentEditingAlarm
    val scrollState = rememberScrollState()

    var prayerDropdownExpanded by remember { mutableStateOf(false) }
    var relationDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (alarmId == -1) "New Alarm" else "Edit Alarm") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Cancel")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (alarm != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Display Name
                OutlinedTextField(
                    value = alarm.displayName,
                    onValueChange = { viewModel.updateEditingAlarmName(it) },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. Prayer Selector
                ExposedDropdownMenuBox(
                    expanded = prayerDropdownExpanded,
                    onExpandedChange = { prayerDropdownExpanded = !prayerDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = alarm.prayer.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Prayer") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = prayerDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = prayerDropdownExpanded,
                        onDismissRequest = { prayerDropdownExpanded = false }
                    ) {
                        PrayerTypes.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    viewModel.updateEditingAlarmPrayer(type)
                                    prayerDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // 3. Relation Selector
                ExposedDropdownMenuBox(
                    expanded = relationDropdownExpanded,
                    onExpandedChange = { relationDropdownExpanded = !relationDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = alarm.relation.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Relation") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = relationDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = relationDropdownExpanded,
                        onDismissRequest = { relationDropdownExpanded = false }
                    ) {
                        AlarmRelations.entries.forEach { relation ->
                            DropdownMenuItem(
                                text = { Text(relation.name) },
                                onClick = {
                                    viewModel.updateEditingAlarmRelation(relation)
                                    relationDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // 4. Offset Minutes Input
                OutlinedTextField(
                    value = alarm.offsetMinutes.toString(),
                    onValueChange = { value ->
                        viewModel.updateEditingAlarmOffset(value.toIntOrNull() ?: 0)
                    },
                    label = { Text("Offset Minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. Repeat Days of Week Selector
                Text(
                    text = "Repeat Days",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DayOfWeek.entries.forEach { day ->
                        val isSelected = alarm.daysOfWeek.contains(day)
                        OutlinedIconToggleButton(
                            checked = isSelected,
                            onCheckedChange = { viewModel.toggleEditingAlarmDay(day) },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Text(
                                text = day.name.take(2),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // 6. Ringtone URI Input
                OutlinedTextField(
                    value = alarm.ringtoneUri ?: "",
                    onValueChange = { value ->
                        viewModel.updateEditingAlarmRingtone(value.ifBlank { null })
                    },
                    label = { Text("Ringtone URI (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 7. Snooze Duration Minutes Input
                OutlinedTextField(
                    value = alarm.snoozeDurationMinutes.toString(),
                    onValueChange = { value ->
                        viewModel.updateEditingAlarmSnoozeDuration(value.toIntOrNull() ?: 0)
                    },
                    label = { Text("Snooze Duration (Minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // 8. Max Snooze Allowed Input
                OutlinedTextField(
                    value = alarm.maxSnoozeAllowed.toString(),
                    onValueChange = { value ->
                        viewModel.updateEditingAlarmMaxSnooze(value.toIntOrNull() ?: 0)
                    },
                    label = { Text("Maximum Snoozes Allowed") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // 9. Auto Snoozing Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enable Auto Snoozing",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = alarm.isAutoSnoozingEnabled,
                        onCheckedChange = { viewModel.updateEditingAlarmAutoSnooze(it) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                Button(
                    onClick = {
                        viewModel.saveAlarm()
                        onSaveSuccess()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = alarm.displayName.isNotBlank()
                ) {
                    Text("Save")
                }

                // Delete Button
                if (alarmId != -1) {
                    Button(
                        onClick = {
                            viewModel.deleteAlarm()
                            onDeleteSuccess()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete Alarm")
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun AlarmsScreen(
    onNavigateToEdit: (Int) -> Unit,
    alarms: List<PrayerAlarm>,
    onToggleStatus: (PrayerAlarm) -> Unit, // Resolved: Unified to a single-argument callback
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEdit(-1) }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Alarm"
                )
            }
        }
    ) { paddingValues ->
        AlarmList(
            alarms = alarms,
            onAlarmClick = onNavigateToEdit,
            onToggleStatus = onToggleStatus, // Passes callback directly down
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
fun AlarmList(
    alarms: List<PrayerAlarm>,
    onAlarmClick: (Int) -> Unit,
    onToggleStatus: (PrayerAlarm) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp)
    ) {
        items(
            items = alarms,
            key = { it.id }
        ) { alarm ->
            AlarmCard(
                alarm = alarm,
                onCardClick = onAlarmClick,
                onToggleStatus = { onToggleStatus(alarm) }
            )
        }
    }
}