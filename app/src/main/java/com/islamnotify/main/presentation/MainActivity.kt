package com.islamnotify.main.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.islamnotify.location.domain.model.LocationData
import com.islamnotify.notification.domain.NotificationFailureCauses
import com.islamnotify.prayer_times.domain.model.PrayerData
import com.islamnotify.prayer_times.domain.model.PrayerEntities
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import com.islamnotify.ui.theme.IslamNotifyTheme
import dagger.hilt.android.AndroidEntryPoint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.islamnotify.R
import com.islamnotify.common.AppUtils
import com.islamnotify.prayer_times.domain.model.NextPrayerData


import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import com.islamnotify.common.NavigationRoot
import com.islamnotify.intro.presentation.IntroActivity
import com.islamnotify.main.domain.MainPreferencesConfig
import com.islamnotify.main.domain.MainPreferencesRepository
import com.islamnotify.settings.presentation.SettingsViewModel
import com.islamnotify.sounds.domain.SoundStates
import com.islamnotify.ui.theme.AppThemeTypes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var mainPreferencesRepository: MainPreferencesRepository

    // Location permission launcher — notification/battery are handled via the in-app dialog
    private val locationPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseLocationGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineLocationGranted || coarseLocationGranted) {
                Log.d("LocationClient", "Permission Granted via Launcher")
                viewModel.setToLoading()
                viewModel.fetchPrayerDataAsync()
            } else {
                Log.e("LocationClient", "Permission Denied")
                com.islamnotify.common.domain.CrashReporterProvider
                    .instance?.log("MainActivity: location permission denied via launcher")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // On first launch, hand off to the onboarding flow before doing any other setup.
        val showIntro = runBlocking { mainPreferencesRepository.getConfig().first().showIntro }
        if (showIntro) {
            startActivity(Intent(this, IntroActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
//        viewModel.startThemeState()
//        runBlocking {
//            viewModel.loadInitialData()
//        }

        //checkPermissionsAndFetch()
// OBSERVE notification work state
//        lifecycleScope.launchWhenStarted {
//            viewModel.notificationState.collect { state ->
//                Log.d("NotifState", "State = $state")
//
//                when (state) {
//                    is NotificationWorkState.Loading -> Log.d("NotifState", "Loading…")
//                    is NotificationWorkState.Success -> Log.d("NotifState", "Notifications OK")
//                    is NotificationWorkState.Error -> Log.e("NotifState", "General error")
//                    is NotificationWorkState.Failed -> Log.e("NotifState", "Notification failed")
//                    is NotificationWorkState.LocationError -> Log.e("NotifState", "Location error")
//                    is NotificationWorkState.PrayerError -> Log.e("NotifState", "Prayer error")
//                }
//            }
//        }

        val initialTheme: MainPreferencesConfig = viewModel.initialTheme()
        setContent {
            val themeConfig = settingsViewModel.preferencesConfig.collectAsState(initialTheme)
            IslamNotifyTheme(themeType = themeConfig.value?.theme?: initialTheme.theme) {

                NavigationRoot()
//                MainScreenContent(
//                    viewModel = viewModel,
//                    uiState = uiState,
//                    countDown = countDown,
//                    nextPrayer = nextPrayer,
//                    date = date,
//                    onRefresh = {viewModel.refreshData()},
//                    onSettingsClick = {},
//                    isRefreshing = viewModel.isRefreshing.collectAsState().value
//                )
//                MainScreenContent(
//                    viewModel = viewModel,
//                    notificationFailureCauses = notificationFailureCauses,
//                    uiState = uiState,
//                    countDown = countDown,
//                    nextPrayer = nextPrayer,
//                    date = date,
//                    onRefresh = {viewModel.refreshData()},
//                    isRefreshing = viewModel.isRefreshing.collectAsState().value,
//                    onMenuClick = {handleMenuClick()},
//                    onToggleSound = {handleSoundsToggle(it)}
//                )


            }
        }
    }



    private fun checkPermissionsAndFetch() {
        val hasFine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
//            viewModel.fetchPrayerDataAsync()
//        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


}


// --- MODERN COLOR PALETTE ---
val DeepEmerald = Color(0xFF1B3022)
val MidEmerald = Color(0xFF2D4B37)
val SoftGold = Color(0xFFD4AF37)
val Champagne = Color(0xFFF7F3E9)
val MutedText = Color(0xFF7A867E)
val ActiveCardGlow = Color(0xFF3E6349)

//@Composable
//fun MainScreenContent2(
//    viewModel: MainViewModel,
//    uiState: LocationPrayerState,
//    countDown: String,
//    nextPrayer: NextPrayerData,
//    date: String,
//    isRefreshing: Boolean,
//    onRefresh: () -> Unit,
//    onToggleSound: (PrayerTypes) -> Unit
//) {
//    val context = LocalContext.current
//
//    val locationName = when (uiState) {
//        is LocationPrayerState.Success -> uiState.locationData.locationName ?: stringResource(R.string.locating_unknown_string)
//        is LocationPrayerState.LocationStale -> uiState.locationData.locationName ?: stringResource(R.string.locating_unknown_string)
//        else -> stringResource(R.string.locating_string)
//    }
//
//    val prayerEntities = when (uiState) {
//        is LocationPrayerState.Success -> uiState.prayerData
//        is LocationPrayerState.LocationStale -> uiState.prayerData
//        is LocationPrayerState.Initial -> uiState.prayerData
//        else -> null
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Brush.verticalGradient(listOf(DeepEmerald, MidEmerald)))
//    ) {
//        // Decorative Mosque Background
//        Image(
//            painter = painterResource(id = R.drawable.bg_mosque_vector),
//            contentDescription = null,
//            modifier = Modifier
//                .fillMaxWidth()
//                .align(Alignment.TopCenter)
//                .graphicsLayer(alpha = 0.12f),
//            contentScale = ContentScale.FillWidth
//        )
//
//        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
//
//            // 1. TOP BAR
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 20.dp, vertical = 16.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Column {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(
//                            painter = painterResource(R.drawable.ic_location_pin),
//                            contentDescription = null,
//                            tint = SoftGold,
//                            modifier = Modifier.size(14.dp)
//                        )
//                        Spacer(Modifier.width(4.dp))
//                        Text(
//                            text = locationName.uppercase(),
//                            color = Color.White.copy(alpha = 0.9f),
//                            letterSpacing = 1.sp,
//                            fontSize = 12.sp,
//                            fontWeight = FontWeight.Bold,
//                            fontFamily = MainFont
//                        )
//                    }
//                    Text(
//                        text = date,
//                        color = Color.White.copy(alpha = 0.6f),
//                        fontSize = 11.sp,
//                        fontFamily = MainFont
//                    )
//                }
//
//                Surface(
//                    color = Color.White.copy(0.1f),
//                    shape = CircleShape,
//                    modifier = Modifier.size(40.dp).clickable { /* Menu */ }
//                ) {
//                    Icon(
//                        painter = painterResource(R.drawable.ic_menu),
//                        contentDescription = null,
//                        tint = Color.White,
//                        modifier = Modifier.padding(10.dp)
//                    )
//                }
//            }
//
//            // 2. HERO SECTION (Countdown)
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1.2f),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                Text(
//                    text = formatPrayerType(context, nextPrayer.type).uppercase(),
//                    color = SoftGold,
//                    letterSpacing = 3.sp,
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Light,
//                    fontFamily = MainFont
//                )
//
//                Text(
//                    text = formatTime(nextPrayer.time, context),
//                    color = Color.White,
//                    fontSize = 64.sp,
//                    fontWeight = FontWeight.Bold,
//                    fontFamily = MainFont
//                )
//
//                Surface(
//                    color = SoftGold.copy(alpha = 0.15f),
//                    shape = RoundedCornerShape(50.dp),
//                    border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.3f))
//                ) {
//                    Text(
//                        text = if (countDown.isNotEmpty()) "- $countDown" else "",
//                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
//                        color = SoftGold,
//                        fontSize = 16.sp,
//                        fontFamily = MainFont,
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//
//            // 3. PRAYER LIST CONTAINER
//            PullToRefreshBox(
//                isRefreshing = isRefreshing,
//                onRefresh = onRefresh,
//                modifier = Modifier.weight(2f)
//            ) {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
//                    color = Champagne,
//                    shadowElevation = 16.dp
//                ) {
//                    val uiPrayerList = remember(prayerEntities, nextPrayer.type) {
//                        if (prayerEntities == null) emptyList() else listOf(
//                            prayerEntities.fajr, prayerEntities.sunrise, prayerEntities.zuhr,
//                            prayerEntities.asr, prayerEntities.sunset, prayerEntities.isha
//                        ).map { domain ->
//                            PrayerUIData(
//                                type = domain.type,
//                                name = formatPrayerType(context, domain.type),
//                                time = domain.time,
//                                icon = getIconForType(domain.type),
//                                isActive = isActive(domain.type, nextPrayer.type)
//                            )
//                        }
//                    }
//
//                    LazyColumn(
//                        modifier = Modifier.fillMaxSize(),
//                        contentPadding = PaddingValues(24.dp),
//                        verticalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        items(uiPrayerList) { prayer ->
//                            EnhancedPrayerCard(viewModel, prayer, onToggleSound)
//                        }
//                        item { Spacer(Modifier.navigationBarsPadding()) }
//                    }
//                }
//            }
//        }
//    }
//}

private fun isActive(cardPrayerType: PrayerTypes, nextPrayerType: PrayerTypes): Boolean {
    return when (cardPrayerType) {
        PrayerTypes.FAJR -> nextPrayerType in listOf(PrayerTypes.FAJR, PrayerTypes.IQAMA_FAJR)
        PrayerTypes.SUNRISE -> nextPrayerType == PrayerTypes.SUNRISE
        PrayerTypes.ZUHR -> nextPrayerType in listOf(
            PrayerTypes.ZUHR,
            PrayerTypes.IQAMA_ZUHR,
            PrayerTypes.DUHA
        )

        PrayerTypes.ASR -> nextPrayerType in listOf(PrayerTypes.ASR, PrayerTypes.IQAMA_ASR)
        PrayerTypes.SUNSET -> nextPrayerType in listOf(PrayerTypes.SUNSET, PrayerTypes.IQAMA_SUNSET)
        PrayerTypes.ISHA -> nextPrayerType in listOf(PrayerTypes.ISHA, PrayerTypes.IQAMA_ISHA)
        PrayerTypes.LAST_THIRD -> nextPrayerType in listOf(
            PrayerTypes.LAST_THIRD,
            PrayerTypes.MIDNIGHT
        )

        else -> false
    }
}

@Composable
fun EnhancedPrayerCard(
    viewModel: MainViewModel,
    prayer: PrayerUIData,
    onToggleSound: (PrayerTypes) -> Unit
) {
    val context = LocalContext.current
    val soundState by viewModel.prayerSoundStates.collectAsState()
    val currentSound = soundState?.get(prayer.type) ?: SoundStates.AZAN

    val containerColor = if (prayer.isActive) ActiveCardGlow else Color.White
    val contentColor = if (prayer.isActive) Color.White else DeepEmerald
    val shadowAlpha = if (prayer.isActive) 0.25f else 0.05f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black, spotColor = Color.Black.copy(shadowAlpha)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (!prayer.isActive) BorderStroke(1.dp, Color.Black.copy(0.05f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sound Toggle Circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (prayer.isActive) Color.White.copy(0.15f) else Champagne)
                    .clickable {
                        onToggleSound(prayer.type)
                        vibrate(context)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(getSoundIcon(currentSound)),
                    contentDescription = null,
                    tint = if (prayer.isActive) Color.White else MidEmerald,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prayer.name,
                    color = contentColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = MainFont
                )
                if (prayer.isActive) {
                    Text(
                        text = "الآن", // Add "Now" string to your strings.xml
                        color = SoftGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MainFont
                    )
                }
            }

            Text(
                text = formatTime(prayer.time, context),
                color = contentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MainFont
            )
        }
    }
}

// --- HELPER FUNCTIONS (KEEP EXISTING LOGIC) ---

private fun getSoundIcon(soundStates: SoundStates?): Int {
    return when (soundStates) {
        SoundStates.AZAN, SoundStates.IQAMA -> R.drawable.ic_sounds_loud
        SoundStates.NOTIFY -> R.drawable.ic_sounds_notify
        SoundStates.MUTE -> R.drawable.ic_sounds_mute
        else -> R.drawable.ic_sounds_loud
    }
}

private fun vibrate(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    if (vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(50)
        }
    }
}

// --- Sub-Composables ---

@Composable
fun LocationDataDisplay(loc: LocationData) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Location Details", fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))

            // FIX 2: Explicitly passing Color.Black
            val contentColor = Color.Black

            KeyValueText("Name", loc.locationName ?: "Unknown", contentColor)
            KeyValueText("Country", loc.countryCode ?: "Unknown", contentColor)
            KeyValueText("Coords", "${loc.latitude}, ${loc.longitude}", contentColor)
            KeyValueText("Fetched", formatRelativeTime(loc.timestamp), contentColor)
        }
    }
}

@Composable
fun KeyValueText(key: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = "$key: ", fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Text(text = value, color = valueColor)
    }
}


// --- Sub-Composables ---

@Composable
fun PrayerDataDisplay(context: Context, data: PrayerEntities) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray)
            .padding(8.dp)
    ) {
        Text(
            "All Prayer Entities",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        PrayerRow(context, data.fajr)
        PrayerRow(context, data.iqamaFajr)
        PrayerRow(context, data.sunrise)
        PrayerRow(context, data.duha)
        PrayerRow(context, data.zuhr)
        PrayerRow(context, data.iqamaZuhr)
        PrayerRow(context, data.asr)
        PrayerRow(context, data.iqamaAsr)
        PrayerRow(context, data.sunset)
        PrayerRow(context, data.iqamaSunset)
        PrayerRow(context, data.isha)
        PrayerRow(context, data.iqamaIsha)
        PrayerRow(context, data.midnight)
        PrayerRow(context, data.lastThird)
    }
}


@Composable
fun PrayerRow(context: Context, prayer: PrayerData) {
    if (prayer.type != PrayerTypes.EMPTY) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatPrayerType(context, prayer.type), fontWeight = FontWeight.Medium)
            Text(text = prayer.time)
        }
        Divider(color = Color.LightGray, thickness = 0.5.dp)
    }
}

@Composable
fun ErrorCard(msg: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = msg, color = Color.Red, modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Blue,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun KeyValueText(key: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = "$key: ", fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Text(text = value)
    }
}

// --- Formatting Helpers ---

fun formatPrayerType(context: Context, type: PrayerTypes): String {

    return when (type) {
        PrayerTypes.FAJR -> {
            context.getString(R.string.fajr_name)
        }

        PrayerTypes.IQAMA_FAJR -> {
            context.getString(R.string.iqama_fajr_name)
        }

        PrayerTypes.SUNRISE -> {
            context.getString(R.string.sunrise_name)
        }

        PrayerTypes.DUHA -> {
            context.getString(R.string.duha_name)
        }

        PrayerTypes.ZUHR -> {
            if (AppUtils.isTodayFriday()) {
                context.getString(R.string.jummah_name)
            } else {
                context.getString(R.string.zuhr_name)
            }
        }

        PrayerTypes.IQAMA_ZUHR -> {
            context.getString(R.string.iqama_zuhr_name)
        }

        PrayerTypes.ASR -> {
            context.getString(R.string.asr_name)
        }

        PrayerTypes.IQAMA_ASR -> {
            context.getString(R.string.iqama_asr_name)
        }

        PrayerTypes.SUNSET -> {
            context.getString(R.string.sunset_name)
        }

        PrayerTypes.IQAMA_SUNSET -> {
            context.getString(R.string.iqama_sunset_name)
        }

        PrayerTypes.ISHA -> {
            context.getString(R.string.isha_name)
        }

        PrayerTypes.IQAMA_ISHA -> {
            context.getString(R.string.iqama_isha_name)
        }

        PrayerTypes.LAST_THIRD -> {
            context.getString(R.string.last_third_name)
        }

        PrayerTypes.MIDNIGHT -> {
            context.getString(R.string.midnight_name)
        }

        PrayerTypes.EMPTY -> {
            ""
        }
    }

}

fun formatFailureCause(cause: NotificationFailureCauses): String {
    return cause.name
        .lowercase()
        .replace("_", " ")
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}

fun formatRelativeTime(timestamp: Long): String {
    if (timestamp == 0L) return "Never"
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        else -> "Last Fetched: ${
            DateUtils.getRelativeTimeSpanString(
                timestamp,
                now,
                DateUtils.MINUTE_IN_MILLIS
            )
        }"
    }
}
//package com.islamnotify.display_prayers.presentation
//
//import android.Manifest
//import android.content.Context
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.text.format.DateUtils
//import android.util.Log
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.activity.viewModels
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.lifecycleScope
//import com.islamnotify.location.domain.model.LocationData
//import com.islamnotify.notification.domain.NotificationWorkState
//import com.islamnotify.notification.domain.NotificationFailureCauses
//import com.islamnotify.prayer_data.domain.model.PrayerData
//import com.islamnotify.prayer_data.domain.model.PrayerEntities
//import com.islamnotify.prayer_data.domain.model.PrayerTypes
//import com.islamnotify.ui.theme.IslamNotifyTheme
//import dagger.hilt.android.AndroidEntryPoint
//import android.content.Intent
//import android.net.Uri
//import android.os.Build
//import android.os.PowerManager
//import android.provider.Settings
//import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.app.AppCompatDelegate
//import androidx.compose.ui.res.stringResource
//import androidx.core.os.LocaleListCompat
//import kotlinx.coroutines.flow.StateFlow
//import com.islamnotify.R
//
//@AndroidEntryPoint
//class MainActivity : AppCompatActivity() {
//
//    private val viewModel: MainViewModel by viewModels()
//
//    // 1. Define the permission launcher
//    private val locationPermissionLauncher: ActivityResultLauncher<Array<String>> =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
//            val coarseLocationGranted =
//                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
//
//            if (fineLocationGranted || coarseLocationGranted) {
//                // Permission granted, fetch data
//                Log.d("LocationClient", "Permission Granted via Launcher")
//                viewModel.setToLoading()
//                viewModel.fetchPrayerDataAsync()
//                requestNotificationPermissionIfNeeded()
//            } else {
//                Log.e("LocationClient", "Permission Denied")
//                // Optional: Show a Snackbar or UI message here telling user why you need it
//            }
//        }
//
//    // === Notification Permission Launcher ===
//    private val notificationPermissionLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
//            if (!granted) {
//                Log.e("NotificationPerm", "User denied POST_NOTIFICATIONS")
//            } else {
//                Log.d("NotificationPerm", "Notification permission granted")
//                viewModel.startNotificationWork()
//                requestBatteryWhitelistingIfNeeded()
//            }
//        }
//
//    // === Battery Optimization Launcher ===
//    private val batteryOptLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            Log.d("BatteryOpt", "Returned from Battery Optimizations screen")
//            viewModel.startNotificationWork()
//        }
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//
//        checkPermissionsAndFetch()
//// OBSERVE notification work state
//        lifecycleScope.launchWhenStarted {
//            viewModel.notificationState.collect { state ->
//                Log.d("NotifState", "State = $state")
//
//                when (state) {
//                    is NotificationWorkState.Loading -> Log.d("NotifState", "Loading…")
//                    is NotificationWorkState.Success -> Log.d("NotifState", "Notifications OK")
//                    is NotificationWorkState.Error -> Log.e("NotifState", "General error")
//                    is NotificationWorkState.Failed -> Log.e("NotifState", "Notification failed")
//                    is NotificationWorkState.LocationError -> Log.e("NotifState", "Location error")
//                    is NotificationWorkState.PrayerError -> Log.e("NotifState", "Prayer error")
//                }
//            }
//        }
//
//// OBSERVE failure causes → automatically request permissions
//        lifecycleScope.launchWhenStarted {
//            viewModel.notificationFailures.collect { failures ->
//                if (failures.isNullOrEmpty()) return@collect
//
//                Log.e("NotifFailures", "Failures = $failures")
//
//                if (failures.contains(NotificationFailureCauses.NOTIFICATION_PERMISSION_DENIED)) {
//                    requestNotificationPermissionIfNeeded()
//                }
//
//                if (failures.contains(NotificationFailureCauses.BATTERY_PERMISSION_DENIED)) {
//                    requestBatteryWhitelistingIfNeeded()
//                }
//            }
//        }
//
//        val notificationStateFlow = viewModel.notificationState
//        val notificationFailuresFlow = viewModel.notificationFailures
//
//        setContent {
//            IslamNotifyTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    DebugPrayerScreen(
//                        context = this,
//                        modifier = Modifier.padding(innerPadding),
//                        viewModel = viewModel,
//                        // 2. Pass the action to the composable
//                        onRefreshClick = {
//                            checkPermissionsAndFetch()
//                        },
//                        notificationStateFlow = notificationStateFlow,
//                        notificationFailuresFlow = notificationFailuresFlow
//                    )
//                }
//            }
//        }
//    }
//
//    private fun checkPermissionsAndFetch() {
//        val hasFine = ContextCompat.checkSelfPermission(
//            this,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED
//        val hasCoarse = ContextCompat.checkSelfPermission(
//            this,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED
//
//        if (!hasFine && !hasCoarse) {
////            viewModel.fetchPrayerDataAsync()
////        } else {
//            locationPermissionLauncher.launch(
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                )
//            )
//        }
//    }
//
//    private fun requestNotificationPermissionIfNeeded() {
//        // Android 13 (API 33) and above requires runtime permission
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            val permissionStatus = ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.POST_NOTIFICATIONS
//            )
//
//            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
//                // Launch the permission requester defined at the top of your class
//                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//            }
//        }
//    }
//
//    private fun requestBatteryWhitelistingIfNeeded() {
//        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
//        val packageName = packageName
//
//        // Check if the app is already on the whitelist
//        val isIgnoringOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName)
//
//        if (!isIgnoringOptimizations) {
//            // Create an intent to request the user to whitelist the app
//            val intent = Intent().apply {
//                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
//                data = Uri.parse("package:$packageName")
//            }
//            // Launch the activity result launcher defined at the top of your class
//            batteryOptLauncher.launch(intent)
//        } else {
//            Log.d("BatteryOpt", "App is already whitelisted.")
//            viewModel.startNotificationWork()
//        }
//    }
//
//}
//
//
//
//@Composable
//fun DebugPrayerScreen(
//    context: Context,
//    modifier: Modifier = Modifier,
//    viewModel: MainViewModel,
//    onRefreshClick: () -> Unit,
//    notificationStateFlow: StateFlow<NotificationWorkState>,
//    notificationFailuresFlow: StateFlow<List<NotificationFailureCauses>?>
//
//) {
//    val uiState by viewModel.uiState.collectAsState()
//    val countDown by viewModel.countDown.collectAsState()
//    val nextPrayer by viewModel.nextPrayer.collectAsState()
//    val notifState by notificationStateFlow.collectAsState()
//    val notifFailures by notificationFailuresFlow.collectAsState()
//
//
//    Scaffold(
//        modifier = modifier,
//        topBar = {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(Color.DarkGray)
//                    .padding(16.dp)
//            ) {
//                Text(
//                    text = "Debug: Prayer Data",
//                    color = Color.White,
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 20.sp
//                )
//            }
//        },
//        bottomBar = {
//            Button(
//                onClick = { onRefreshClick() },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//            ) {
//                Text("ACTION: Check Permissions & Refresh")
//            }
//        }
//    ) { paddingValues ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//
//            // 1. Dynamic Data (Countdown & Next Prayer)
//            item {
//                SectionHeader("Dynamic State")
//                Card(modifier = Modifier.fillMaxWidth()) {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        KeyValueText("Countdown", countDown.ifEmpty { "Waiting..." })
//                        Divider(modifier = Modifier.padding(vertical = 8.dp))
//
//                        Text(text = "Next Prayer Object:", fontWeight = FontWeight.Bold)
//
//                        // FIX 1: If name is empty, fall back to the formatted Type
//                        val displayName = nextPrayer.name.ifEmpty {
//                            formatPrayerType(context, nextPrayer.type)
//                        }
//
//                        KeyValueText("Name", displayName)
//                        KeyValueText("Time", nextPrayer.time)
//                        KeyValueText("Type", nextPrayer.type.name)
//                    }
//                }
//            }
//
//            item {
//                SectionHeader("Notification System")
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)) // Light Orange background
//                ) {
//                    Column(modifier = Modifier.padding(16.dp)) {
//
//                        // --- Part A: The Current Work State ---
//                        Text(text = "Current State:", fontWeight = FontWeight.Bold)
//
//                        val (statusText, statusColor) = when (notifState) {
//                            is NotificationWorkState.Success -> "Active & Scheduled" to Color(0xFF2E7D32) // Green
//                            is NotificationWorkState.Loading -> "Processing..." to Color.DarkGray
//                            is NotificationWorkState.Failed -> "Worker Failed" to Color.Red
//                            is NotificationWorkState.Error -> "General Error" to Color.Red
//                            is NotificationWorkState.LocationError -> "Location Error" to Color.Red
//                            is NotificationWorkState.PrayerError -> "Prayer Data Error" to Color.Red
//                        }
//
//                        Text(
//                            text = statusText,
//                            color = statusColor,
//                            fontWeight = FontWeight.ExtraBold,
//                            fontSize = 18.sp,
//                            modifier = Modifier.padding(vertical = 4.dp)
//                        )
//
////                        // If there is specific error text in the state, show it
////                        if (notifState is NotificationWorkState.LocationError) {
////                            Text("Reason: LocationError", fontSize = 12.sp, color = Color.Red)
////                        }
////                        if (notifState is NotificationWorkState.PrayerError) {
////                            Text("Reason: PrayerError", fontSize = 12.sp, color = Color.Red)
////                        }
//
//                        // --- Part B: Failure Causes List ---
//                        if (!notifFailures.isNullOrEmpty()) {
//                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)
//                            Text(text = "Detected Issues:", fontWeight = FontWeight.Bold, color = Color.Red)
//
//                            notifFailures!!.forEach { cause ->
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(vertical = 2.dp)
//                                ) {
//                                    // Small bullet point
//                                    Text(text = "• ", fontWeight = FontWeight.Bold, color = Color.Red)
//                                    Text(
//                                        text = formatFailureCause(cause),
//                                        color = Color(0xFFC62828), // Dark Red
//                                        fontSize = 14.sp
//                                    )
//                                }
//                            }
//                        } else if (notifState is NotificationWorkState.Success) {
//                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)
//                            Text("No issues detected.", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 12.sp)
//                        }
//                    }
//                }
//            }
//
//
//            // 2. UI State Logic
//            item {
//                SectionHeader("Current UI State: ${uiState::class.simpleName}")
//            }
//
//            when (val state = uiState) {
//                is LocationPrayerState.Loading -> {
//                    item { Text("Status: Loading Data...") }
//                }
//
//                is LocationPrayerState.PrayerError -> {
//                    item { ErrorCard("Critical Error: Unable to fetch prayer times.") }
//                }
//
//                is LocationPrayerState.LocationError -> {
//                    item { ErrorCard("Location Error: ${state.failureCause}") }
//                }
//
//                is LocationPrayerState.LocationStale -> {
//                    item {
//                        ErrorCard("Warning: Location is Stale.\nCause: ${state.failureCause}")
//                    }
//                    item { PrayerDataDisplay(context, state.prayerData) }
//                    item { LocationDataDisplay(state.locationData) }
//                }
//
//                is LocationPrayerState.Success -> {
//                    item { Text("Status: Data Valid", color = Color(0xFF006400)) }
//                    item { PrayerDataDisplay(context, state.prayerData) }
//                    item { LocationDataDisplay(state.locationData) }
//                }
//
//                is LocationPrayerState.Initial -> {
//                    item { Text("Status: Initial Cached Data") }
//                    item { PrayerDataDisplay(context, state.prayerData) }
//                    if (state.locationData != null) {
//                        item { LocationDataDisplay(state.locationData) }
//                    } else {
//                        item { Text("No Location Data Available") }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// --- Sub-Composables ---
//
//@Composable
//fun LocationDataDisplay(loc: LocationData) {
//    Card(
//        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text("Location Details", fontWeight = FontWeight.Bold, color = Color.Black)
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // FIX 2: Explicitly passing Color.Black
//            val contentColor = Color.Black
//
//            KeyValueText("Name", loc.locationName ?: "Unknown", contentColor)
//            KeyValueText("Country", loc.countryCode ?: "Unknown", contentColor)
//            KeyValueText("Coords", "${loc.latitude}, ${loc.longitude}", contentColor)
//            KeyValueText("Fetched", formatRelativeTime(loc.timestamp), contentColor)
//        }
//    }
//}
//
//@Composable
//fun KeyValueText(key: String, value: String, valueColor: Color = Color.Unspecified) {
//    Row(modifier = Modifier.fillMaxWidth()) {
//        Text(text = "$key: ", fontWeight = FontWeight.SemiBold, color = Color.Gray)
//        Text(text = value, color = valueColor)
//    }
//}
//
//
//// --- Sub-Composables ---
//
//@Composable
//fun PrayerDataDisplay(context: Context, data: PrayerEntities) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .border(1.dp, Color.Gray)
//            .padding(8.dp)
//    ) {
//        Text(
//            "All Prayer Entities",
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//        PrayerRow(context, data.fajr)
//        PrayerRow(context, data.iqamaFajr)
//        PrayerRow(context, data.sunrise)
//        PrayerRow(context, data.duha)
//        PrayerRow(context, data.zuhr)
//        PrayerRow(context, data.iqamaZuhr)
//        PrayerRow(context, data.asr)
//        PrayerRow(context, data.iqamaAsr)
//        PrayerRow(context, data.sunset)
//        PrayerRow(context, data.iqamaSunset)
//        PrayerRow(context, data.isha)
//        PrayerRow(context, data.iqamaIsha)
//        PrayerRow(context, data.midnight)
//        PrayerRow(context, data.lastThird)
//    }
//}
//
//
//@Composable
//fun PrayerRow(context: Context, prayer: PrayerData) {
//    if (prayer.type != PrayerTypes.EMPTY) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 4.dp),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text(text = formatPrayerType(context, prayer.type), fontWeight = FontWeight.Medium)
//            Text(text = prayer.time)
//        }
//        Divider(color = Color.LightGray, thickness = 0.5.dp)
//    }
//}
//
//@Composable
//fun ErrorCard(msg: String) {
//    Card(
//        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Text(text = msg, color = Color.Red, modifier = Modifier.padding(16.dp))
//    }
//}
//
//@Composable
//fun SectionHeader(title: String) {
//    Text(
//        text = title,
//        fontSize = 18.sp,
//        fontWeight = FontWeight.Bold,
//        color = Color.Blue,
//        modifier = Modifier.padding(vertical = 4.dp)
//    )
//}
//
//@Composable
//fun KeyValueText(key: String, value: String) {
//    Row(modifier = Modifier.fillMaxWidth()) {
//        Text(text = "$key: ", fontWeight = FontWeight.SemiBold, color = Color.Gray)
//        Text(text = value)
//    }
//}
//
//// --- Formatting Helpers ---
//
//fun formatPrayerType(context: Context, type: PrayerTypes): String {
//
//    return when(type){
//        PrayerTypes.FAJR -> {context.getString(R.string.fajr_name)}
//        PrayerTypes.IQAMA_FAJR -> {context.getString(R.string.iqama_fajr_name)}
//        PrayerTypes.SUNRISE -> {context.getString(R.string.sunrise_name)}
//        PrayerTypes.DUHA -> {context.getString(R.string.duha_name)}
//        PrayerTypes.ZUHR -> {context.getString(R.string.zuhr_name)}
//        PrayerTypes.IQAMA_ZUHR -> {context.getString(R.string.iqama_zuhr_name)}
//        PrayerTypes.ASR -> {context.getString(R.string.asr_name)}
//        PrayerTypes.IQAMA_ASR -> {context.getString(R.string.iqama_asr_name)}
//        PrayerTypes.SUNSET -> {context.getString(R.string.sunset_name)}
//        PrayerTypes.IQAMA_SUNSET -> {context.getString(R.string.iqama_sunset_name)}
//        PrayerTypes.ISHA -> {context.getString(R.string.isha_name)}
//        PrayerTypes.IQAMA_ISHA -> {context.getString(R.string.iqama_isha_name)}
//        PrayerTypes.LAST_THIRD -> {context.getString(R.string.last_third_name)}
//        PrayerTypes.MIDNIGHT -> {context.getString(R.string.midnight_name)}
//        PrayerTypes.EMPTY -> { "" }
//    }
//
//}
//
//fun formatFailureCause(cause: NotificationFailureCauses): String {
//    return cause.name
//        .lowercase()
//        .replace("_", " ")
//        .split(" ")
//        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
//}
//
//fun formatRelativeTime(timestamp: Long): String {
//    if (timestamp == 0L) return "Never"
//    val now = System.currentTimeMillis()
//    val diff = now - timestamp
//
//    return when {
//        diff < 60_000 -> "Just now"
//        else -> "Last Fetched: ${
//            DateUtils.getRelativeTimeSpanString(
//                timestamp,
//                now,
//                DateUtils.MINUTE_IN_MILLIS
//            )
//        }"
//    }
//}