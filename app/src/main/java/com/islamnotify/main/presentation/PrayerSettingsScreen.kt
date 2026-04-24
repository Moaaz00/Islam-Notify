package com.islamnotify.main.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Color Palette from your Tailwind Config ---
val AppPrimary = Color(0xFF335229)
val AppBackground = Color(0xFFFCF9F2)
val AppSurfaceVariant = Color(0xFFE5E2DB)
val AppOnSurface = Color(0xFF1C1C18)
val AppOnSurfaceVariant = Color(0xFF43483F)
val AppOutlineVariant = Color(0xFFC3C8BC)
val AppSurfaceContainerLow = Color(0xFFF6F3EC)
val AppSecondaryContainer = Color(0xFFD5E4CB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerSettingsScreen() {
    // State for toggles
    var notificationsEnabled by remember { mutableStateOf(true) }
    var silentModeEnabled by remember { mutableStateOf(false) }

    // State for prayer offsets
    var fajrOffset by remember { mutableStateOf(-5) }
    var dhuhrOffset by remember { mutableStateOf(0) }
    var asrOffset by remember { mutableStateOf(2) }
    var maghribOffset by remember { mutableStateOf(0) }
    var ishaOffset by remember { mutableStateOf(5) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppOnSurface
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { /* Back */ }) {
//                            Icon(Icons.Default.ArrowBack, contentVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = AppPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
                )
                HorizontalDivider(color = AppOutlineVariant.copy(alpha = 0.3f))
            }
        },
        bottomBar = {
//            BottomNavigationBar()
        },
        containerColor = AppBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
        ) {
            // --- Calculation Method Section ---
            item {
                MySectionHeader("GENERAL")
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                AppOutlineVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                            .background(AppSurfaceContainerLow, RoundedCornerShape(12.dp))
                            .clickable { /* Select Method */ }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Muslim World League", color = AppOnSurface, fontWeight = FontWeight.Medium)
//                            Icon(Icons.Default.UnfoldMore, contentDescription = null, tint = AppOnSurfaceVariant)
                        }
                    }
                    // Floating Label
                    Text(
                        "Calculation Method",
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .offset(y = (-10).dp)
                            .background(AppBackground)
                            .padding(horizontal = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AppPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // --- Manual Adjustments Section ---
            item {
                MySectionHeader("MANUAL ADJUSTMENTS")
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppSurfaceContainerLow),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, AppOutlineVariant.copy(alpha = 0.2f))
                ) {
                    Column {
                        AdjustmentRow("Fajr", fajrOffset) { fajrOffset += it }
                        Divider()
                        AdjustmentRow("Dhuhr", dhuhrOffset) { dhuhrOffset += it }
                        Divider()
                        AdjustmentRow("Asr", asrOffset) { asrOffset += it }
                        Divider()
                        AdjustmentRow("Maghrib", maghribOffset) { maghribOffset += it }
                        Divider()
                        AdjustmentRow("Isha", ishaOffset) { ishaOffset += it }
                    }
                }
            }

            // --- Preferences Section ---
            item {
                MySectionHeader("PREFERENCES")
                ToggleRow(
                    title = "Enable Notifications",
                    subtitle = "Get alerts for upcoming prayers",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                HorizontalDivider(color = AppOutlineVariant.copy(alpha = 0.2f))
                ToggleRow(
                    title = "Silent Mode during Prayer",
                    subtitle = "Auto-mute device during prayer times",
                    checked = silentModeEnabled,
                    onCheckedChange = { silentModeEnabled = it }
                )
            }
        }
    }
}

@Composable
fun MySectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = AppPrimary
        )
    )
}

@Composable
fun AdjustmentRow(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//            Icon(icon, contentDescription = null, tint = AppOnSurfaceVariant, modifier = Modifier.size(20.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppOnSurface)
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//            StepperButton(Icons.Default.Remove) { onValueChange(-1) }
            Text(
                text = if (value > 0) "+$value" else "$value",
                modifier = Modifier.width(32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = AppPrimary,
                fontSize = 14.sp
            )
//            StepperButton(Icons.Default.Add) { onValueChange(1) }
        }
    }
}

@Composable
fun StepperButton(icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(32.dp)
            .clickable { onClick() },
        shape = CircleShape,
        border = BorderStroke(1.dp, AppOutlineVariant.copy(alpha = 0.4f)),
        color = Color.Transparent
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.padding(6.dp),
            tint = AppOnSurfaceVariant
        )
    }
}

@Composable
fun ToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppOnSurface)
            Text(subtitle, fontSize = 12.sp, color = AppOnSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppPrimary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = AppSurfaceVariant
            )
        )
    }
}

@Composable
fun Divider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = AppOutlineVariant.copy(alpha = 0.1f))
}

//@Composable
//fun BottomNavigationBar() {
//    NavigationBar(
//        containerColor = Color.White.copy(alpha = 0.8f),
//        tonalElevation = 8.dp,
//        modifier = Modifier.graphicsLayer {
//            clip = true
//            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
//        }
//    ) {
//        val items = listOf("Home", "Prayers", "Settings", "Profile")
//        val icons = listOf(Icons.Default.Home, Icons.Default.Schedule, Icons.Default.Settings, Icons.Default.Person)
//
//        items.forEachIndexed { index, item ->
//            val isSelected = item == "Settings"
//            NavigationBarItem(
//                selected = isSelected,
//                onClick = { },
//                icon = { Icon(icons[index], contentDescription = item) },
//                label = { Text(item, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = AppOnSurface,
//                    selectedTextColor = AppOnSurface,
//                    indicatorColor = AppSecondaryContainer,
//                    unselectedIconColor = AppOnSurfaceVariant,
//                    unselectedTextColor = AppOnSurfaceVariant
//                )
//            )
//        }
//    }
//}