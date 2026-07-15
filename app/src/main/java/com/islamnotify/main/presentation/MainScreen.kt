package com.islamnotify.main.presentation

import android.content.Context
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamnotify.R
import com.islamnotify.prayer_times.domain.model.NextPrayerData
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import com.islamnotify.sounds.domain.SoundStates
import java.text.NumberFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import com.islamnotify.ui.theme.ExtendedTheme
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import com.islamnotify.common.AppUtils.toPrayerDataList
import com.islamnotify.prayer_times.domain.model.CountDownDataModel


val tightTextStyle = TextStyle(
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both
    )
)

val MainFont = FontFamily(
    Font(R.font.readex)
)


@Composable
fun AnimatedTopGradient(modifier: Modifier = Modifier) {
    val primaryColor = Color(0xFF4A6A3F)
    // A slightly lighter olive for the "shimmer" effect
    val lightOlive = Color(0xFF5F8551)
    // A deeper olive for depth
    val darkOlive = Color(0xFF3D5934)

    val infiniteTransition = rememberInfiniteTransition(label = "gradient")

    // This value moves from 0 to 2000 to create a sliding window effect
    val xOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing), // Slow 15s loop
            repeatMode = RepeatMode.Reverse
        ),
        label = "xOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp) // Covers the Header area
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(darkOlive, primaryColor, lightOlive, primaryColor, darkOlive),
                    start = Offset(xOffset - 1000f, 0f),
                    end = Offset(xOffset, 500f)
                )
            )
    )
}

@Composable
fun MainScreenContent(
    viewModel: MainViewModel,
    uiState: LocationPrayerState,
    countDown: CountDownDataModel,
    nextPrayer: NextPrayerData,
    date: String,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
//    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
//    val scope = rememberCoroutineScope()

    val permissionDialogState by viewModel.permissionDialogState.collectAsState()

    val locationName = when (uiState) {
        is LocationPrayerState.Success -> uiState.locationData.locationName
            ?: stringResource(R.string.locating_unknown_string)

        is LocationPrayerState.LocationStale -> uiState.locationData.locationName ?: stringResource(
            R.string.locating_unknown_string
        )

        is LocationPrayerState.Initial -> uiState.locationData?.locationName ?: String()
        else -> stringResource(R.string.locating_string)
    }

    //    val lastFetched = when (uiState) {
    //        is LocationPrayerState.Success -> formatRelativeTime(uiState.locationData.timestamp)
    //        is LocationPrayerState.LocationStale -> formatRelativeTime(uiState.locationData.timestamp)
    //        is LocationPrayerState.Initial -> formatRelativeTime(uiState.locationData?.timestamp ?: 0L)
    //        else -> ""
    //    }

    val prayerEntities = when (uiState) {
        is LocationPrayerState.Success -> uiState.prayerData
        is LocationPrayerState.LocationStale -> uiState.prayerData
        is LocationPrayerState.Initial -> uiState.prayerData
        else -> null
    }

    when (uiState) {
        is LocationPrayerState.Loading -> LoadingDialog()
        is LocationPrayerState.PrayerError -> PrayerErrorDialog(onRetry = onRefresh)
        is LocationPrayerState.LocationError -> LocationErrorDialog(
            cause = uiState.failureCause,
            onRetry = onRefresh
        )
        else -> permissionDialogState?.let { state ->
            PermissionsReminderDialog(
                state = state,
                onDismiss = { viewModel.dismissPermissionsDialog(dontAskAgain = false) },
                onDontAskAgain = { viewModel.dismissPermissionsDialog(dontAskAgain = true) }
            )
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()

//    ModalNavigationDrawer(
//        drawerState = drawerState,
//        drawerContent = {
//            NavDrawerContent(
//                viewModel = viewModel,
//                onCloseDrawer = { scope.launch { drawerState.close() } })
//        },
//        gesturesEnabled = drawerState.isOpen
//
//    ) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = ExtendedTheme.colors.backgroundGradient
                )
            )
            .statusBarsPadding()
    ) {
        // Background Image
//            Image(
//                painter = painterResource(id = R.drawable.bg_mosque_extended),
//                contentDescription = null,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.TopCenter)
//                    .graphicsLayer(alpha = 0.075f),
//                contentScale = ContentScale.FillWidth
//            )


        Column(modifier = Modifier.fillMaxSize()) {
            HeaderSection(
                locationName = locationName,
                nextPrayerName = formatPrayerType(context, nextPrayer.type),
                nextPrayerTime = nextPrayer.time,
                countDown = countDown,
                onMenuClick = {
                    onSettingsClick()
                    /*scope.launch { drawerState.open() }*/
                },
                modifier = Modifier.wrapContentHeight()
            )

            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (-1).dp),
                    color = Color.White.copy(alpha = ExtendedTheme.colors.bg_alpha)
                ) { }

                PrayerList(
                    viewModel = viewModel,
                    prayers = prayerEntities,
                    nextPrayerType = nextPrayer.type,
                    modifier = Modifier.fillMaxSize(),
                    date = date,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh
                )
            }

//            Surface(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(64.dp),
//                color = MaterialTheme.colorScheme.primary,
//                tonalElevation = 8.dp
//            ) {
//                // Currently empty as requested
//                Box(modifier = Modifier.fillMaxSize())
//            }

        }
    }
//    }
}

@Composable
fun HeaderSection(
    locationName: String,
    nextPrayerName: String,
    onMenuClick: () -> Unit,
    nextPrayerTime: String,
    countDown: CountDownDataModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val containerAlpha = 0.75f
    val containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = containerAlpha)

    Box(modifier = modifier) {
//        Image(
//            painter = painterResource(id = R.drawable.bg_mosque_vector),
//            contentDescription = null,
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .graphicsLayer(colorFilter = ColorFilter.tint(Color(0xFF1E293B)))//, colorFilter = ColorFilter.tint(Color(0xFFC5A028)))
//        )
        Image(
            painter = painterResource(id = R.drawable.bg_mosque_vector),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer(alpha = ExtendedTheme.colors.bg_alpha)//, colorFilter = ColorFilter.tint(Color(0xFFC5A028)))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(start = 12.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    painter = painterResource(R.drawable.ic_location_pin_filled),
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.onPrimary,
//                    modifier = Modifier.size(16.dp)
//                )
//                Spacer(modifier = Modifier.width(6.dp))
//                Text(
//                    text = locationName,
//                    color = MaterialTheme.colorScheme.onPrimary,
//                    fontFamily = MainFont,
//                    fontWeight = FontWeight.Normal,
//                    fontSize = 12.sp,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//
//            IconButton(onClick = { onMenuClick() }, modifier = Modifier.size(48.dp)) {
//                Icon(
//                    painter = painterResource(R.drawable.ic_settings_filled),
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.onPrimary,
//                    modifier = Modifier.size(24.dp)
//                )
//            }
//        }
// Inside HeaderSection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. LOCATION SIDE: Wrapped in a subtle "Pill"
                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                    shape = CircleShape
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_location_pin),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = locationName,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontFamily = MainFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 2. BUTTONS SIDE: Grouped together to align at the end of the screen
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp) // Space between the two buttons
                ) {
                    // Settings Button (placed at the absolute end)
                    IconButton(
                        onClick = { onMenuClick() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Box(Modifier.wrapContentHeight()) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 48.dp), // Change 6: Breathing padding
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
//                    Spacer(Modifier.weight(4f))
                    Text(
                        text = nextPrayerName,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontFamily = MainFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        style = tightTextStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        modifier = Modifier.padding(bottom = 0.dp, top = 0.dp),
                        text = formatTime(nextPrayerTime, context),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontFamily = MainFont,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val remainingText = stringResource(R.string.remaining_string)
                    val h = stringResource(R.string.remaining_hour_symbol)
                    val m = stringResource(R.string.remaining_minutes_symbol)
                    val s = stringResource(R.string.remaining_seconds_symbol)

                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "%s %d$h %d$m %d$s",
                            remainingText,
                            countDown.hours,
                            countDown.minutes,
                            countDown.seconds
                        ),
                        //                    text = if (countDown.isNotEmpty()) "$remainingText $nextPrayerName $countDown" else "",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontFamily = MainFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        style = tightTextStyle.copy(
                            fontFeatureSettings = "tnum"
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
//                    Spacer(
//                        Modifier
//                            .weight(6f)
//                            .padding(bottom = 12.dp)
//                    )
                }

//            PullToRefreshBox(
//                isRefreshing = isRefreshing,
//                onRefresh = onRefresh,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                LazyColumn(
//                    Modifier
//                        .fillMaxWidth()
//                        .height(160.dp)
//                ) {
//                    item {
//                    }
//                }
//            }
            }

        }
    }

}


@Composable
fun PrayerList(
    viewModel: MainViewModel,
    prayers: com.islamnotify.prayer_times.domain.model.PrayerEntities?,
    nextPrayerType: PrayerTypes,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    date: String
) {
    val context = LocalContext.current
    val soundConfig by viewModel.soundsConfigState.collectAsState()

    val uiPrayerList = if (prayers == null) emptyList() else listOf(
        prayers.fajr,
        prayers.sunrise,
        prayers.zuhr,
        prayers.asr,
        prayers.sunset,
        prayers.isha,
        prayers.lastThird
    ).filter { it.type != PrayerTypes.EMPTY }.map { domain ->
        val isAzanType = domain.type !in listOf(PrayerTypes.SUNRISE, PrayerTypes.LAST_THIRD)

        PrayerUIData(
            type = domain.type,
            name = formatPrayerType(context, domain.type),
            time = domain.time,
            icon = getIconForType(domain.type),
            isActive = isActive(cardPrayerType = domain.type, nextPrayerType = nextPrayerType),
            isSoundActionEnabled = if (isAzanType) {
                soundConfig?.isAzanEnabled ?: true
            } else {
                true
            }
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 24.dp, start = 24.dp, end = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        text = date,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 13.sp,
                        style = tightTextStyle,
                        textAlign = TextAlign.Center,
                        fontFamily = MainFont,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                items(
                    items = uiPrayerList,
                    key = { it.type }
                ) { prayer ->
                    PrayerCard(viewModel = viewModel, prayer = prayer)
                }

                item { Spacer(Modifier.navigationBarsPadding()) }
            }
        }
    }
}


@Composable
fun PrayerCard(
    viewModel: MainViewModel,
    prayer: PrayerUIData
) {
    val animatedElevation = animateDpAsState(
        targetValue = if (prayer.isActive) 6.dp else 0.dp,
        label = "elevation_anim"
    )

    val contentAlpha = if (prayer.isSoundActionEnabled) 1f else 0.38f
    val backgroundColor =
        if (prayer.isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
    val contentColor =
        if (prayer.isActive) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
    val borderStroke =
        if (prayer.isActive) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)

    val context = LocalContext.current
    val soundButtonStates = viewModel.prayerSoundStates.collectAsState()
    val zIndex = if (prayer.isActive) 1f else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .zIndex(zIndex)
            .graphicsLayer {
                shadowElevation = animatedElevation.value.toPx()
                shape = RoundedCornerShape(16.dp)
                clip = true
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. PRAYER ICON
            Icon(
                painter = painterResource(id = prayer.icon),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )

            // Gap between Icon and Name
            Spacer(modifier = Modifier.width(16.dp))

            // 2. PRAYER NAME
            Text(
                text = prayer.name,
                color = contentColor,
                fontSize = 16.sp,
                fontFamily = MainFont,
                fontWeight = FontWeight.Medium, // Increased weight for better readability
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 3. PRAYER TIME
            Text(
                text = formatTime(prayer.time, context),
                color = contentColor,
                fontSize = 16.sp, // Slightly smaller than name for hierarchy
                fontFamily = MainFont,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 4. SOUND ACTION ICON
            // We use a custom box or small IconButton to ensure the icon is 16dp from the edge
            IconButton(
                onClick = {
                    viewModel.togglePrayerSoundState(prayer.type)
                    vibrate(context)
                },
                enabled = prayer.isSoundActionEnabled,
                modifier = Modifier.size(48.dp) // Standard touch target
            ) {
                Icon(
                    painter = painterResource(getSoundIcon(soundButtonStates.value?.get(prayer.type))),
                    contentDescription = "Toggle Sound",
                    tint = contentColor.copy(alpha = contentAlpha),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

//@Composable
//fun PrayerCard(
//    viewModel: MainViewModel,
//    prayer: PrayerUIData
//) {
//    val animatedElevation = animateDpAsState(
//        targetValue = if (prayer.isActive) 6.dp else 0.dp,
//        label = "elevation_anim"
//    )
//
//    val contentAlpha = if (prayer.isSoundActionEnabled) 1f else 0.38f
//    val backgroundColor = if (prayer.isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
//    val contentColor = if (prayer.isActive) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
//    val borderStroke = if (prayer.isActive) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
//
//    val context = LocalContext.current
//    val soundButtonStates = viewModel.prayerSoundStates.collectAsState()
//    val zIndex = if (prayer.isActive) 1f else 0f
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(56.dp)
//            .zIndex(zIndex)
//            .graphicsLayer {
//                shadowElevation = animatedElevation.value.toPx()
//                shape = RoundedCornerShape(15.dp)
//                clip = true
//            },
//        shape = RoundedCornerShape(15.dp),
//        colors = CardDefaults.cardColors(containerColor = backgroundColor),
//        border = borderStroke,
//        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 16.dp), // Balanced horizontal padding
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // 1. PRAYER ICON (Sun/Moon)
//            Spacer(Modifier.padding(start = 8.dp))
//            Icon(
//                painter = painterResource(id = prayer.icon),
//                contentDescription = null,
//                tint = contentColor,
//                modifier = Modifier.size(24.dp)
//            )
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            // 2. PRAYER NAME
//            Text(
//                text = prayer.name,
//                color = contentColor,
//                fontSize = 16.sp,
//                fontFamily = MainFont,
//                fontWeight = FontWeight.Medium,
//                modifier = Modifier.weight(1f), // Takes available space
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//
//            // 3. PRAYER TIME
//            Text(
//                text = formatTime(prayer.time, context),
//                color = contentColor,
//                fontSize = 16.sp,
//                fontFamily = MainFont,
//                fontWeight = FontWeight.Medium,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//
//            Spacer(modifier = Modifier.width(4.dp))
//
//            // 4. SOUND ACTION ICON
//            IconButton(
//                onClick = {
//                    viewModel.togglePrayerSoundState(prayer.type)
//                    vibrate(context)
//                },
//                enabled = prayer.isSoundActionEnabled,
//                modifier = Modifier.size(44.dp),
//            ) {
//                Icon(
//                    painter = painterResource(getSoundIcon(soundButtonStates.value?.get(prayer.type))),
//                    contentDescription = "Toggle Sound",
//                    tint = contentColor,
//                    modifier = Modifier
//                        .size(20.dp) // Slightly smaller for professional look
//                        .graphicsLayer(alpha = contentAlpha)
//                )
//            }
//        }
//    }
//}

//@Composable
//fun PrayerCard(
//    viewModel: MainViewModel,
//    prayer: PrayerUIData
//) {
//    // 1. Animate the elevation to ensure a smooth transition
//    val animatedElevation = animateDpAsState(
//        targetValue = if (prayer.isActive) 6.dp else 0.dp,
//        label = "elevation_anim"
//    )
//
//    val contentAlpha = if (prayer.isSoundActionEnabled) 1f else 0.38f
//
//    val backgroundColor =
//        if (prayer.isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
//    val contentColor =
//        if (prayer.isActive) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
//    val borderStroke =
//        if (prayer.isActive) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
//    val context = LocalContext.current
//    val soundButtonStates = viewModel.prayerSoundStates.collectAsState()
//
//    // 2. We use the 'zIndex' to make sure the active card's shadow
//    // is always drawn on top of the cards around it.
//    val zIndex = if (prayer.isActive) 1f else 0f
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(56.dp)
//            .zIndex(zIndex) // Forces the active card to the front
//            .graphicsLayer {
//                // 3. THIS IS THE FIX:
//                // We manually set the shadow elevation on the hardware layer.
//                // This forces the RenderNode to update and clears "stuck" shadows.
//                shadowElevation = animatedElevation.value.toPx()
//                shape = RoundedCornerShape(15.dp)
//                clip = true
//            },
//        shape = RoundedCornerShape(15.dp),
//        colors = CardDefaults.cardColors(containerColor = backgroundColor),
//        border = borderStroke,
//        // Set internal elevation to 0 so it doesn't conflict with our manual graphicsLayer shadow
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(end = 24.dp, start = 12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            IconButton(
//                onClick = {
//                    viewModel.togglePrayerSoundState(prayer.type)
//                    vibrate(context)
//                },
//                enabled = prayer.isSoundActionEnabled,
//
//                modifier = Modifier.size(48.dp),
//            ) {
//                Icon(
//                    painter = painterResource(getSoundIcon(soundButtonStates.value?.get(prayer.type))),
//                    contentDescription = "Toggle Sound",
//                    tint = contentColor,
//                    modifier = Modifier.size(24.dp)
//                                .graphicsLayer(alpha = contentAlpha)
//
//                )
//            }
//            Spacer(modifier = Modifier.width(4.dp))
//            Text(
//                prayer.name,
//                color = contentColor,
//                fontSize = 16.sp,
//                fontFamily = MainFont,
//                fontWeight = FontWeight.Normal,
//                modifier = Modifier.weight(1f),
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//            Text(
//                formatTime(prayer.time, context),
//                color = contentColor,
//                fontSize = 16.sp,
//                fontFamily = MainFont,
//                fontWeight = FontWeight.Normal,
//            )
//        }
//    }
//}


private fun getSoundIcon(soundStates: SoundStates?): Int {
    return when (soundStates) {
        SoundStates.AZAN, SoundStates.IQAMA -> R.drawable.ic_sounds_loud
//        SoundStates.AZAN, SoundStates.IQAMA -> R.drawable.ic_volume_loud_filled
        SoundStates.NOTIFY -> R.drawable.ic_sounds_notify
        SoundStates.MUTE -> R.drawable.ic_sounds_mute
        null -> R.drawable.ic_sounds_loud
    }
}

data class PrayerUIData(
    val type: PrayerTypes,
    val name: String,
    val time: String,
    val icon: Int,
    val isActive: Boolean,
    val isSoundActionEnabled: Boolean
)

fun getIconForType(type: PrayerTypes): Int {
    return when (type) {
        PrayerTypes.FAJR -> R.drawable.ic_fajr2
        PrayerTypes.SUNRISE -> R.drawable.ic_sunrise2
        PrayerTypes.ZUHR -> R.drawable.ic_zuhr2
        PrayerTypes.ASR -> R.drawable.ic_asr2
        PrayerTypes.SUNSET -> R.drawable.ic_sunset2
        PrayerTypes.ISHA -> R.drawable.ic_isha2
        PrayerTypes.LAST_THIRD -> R.drawable.ic_last_third2
        else -> R.drawable.ic_zuhr2
    }
}


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

fun formatTime(time24h: String, localizedContext: Context): String {
    if (time24h.isBlank()) return ""
    try {
        val inputFormatter = DateTimeFormatter.ofPattern("H:mm")
        val time = LocalTime.parse(time24h, inputFormatter)
        val locale: Locale = localizedContext.resources.configuration.locales[0]

        val formattedResult = time.format(DateTimeFormatter.ofPattern("h:mm", locale))

        val parts = formattedResult.split(":")
        val hourLong = parts[0].toLong()
        val minuteLong = parts[1].toLong()

        val numberFormatter = NumberFormat.getInstance(locale)
        numberFormatter.minimumIntegerDigits = 2

        val localizedHour = numberFormatter.format(hourLong)
        val localizedMinute = numberFormatter.format(minuteLong)

        return "$localizedHour:$localizedMinute"
    } catch (e: Exception) {
        Log.e("MainActivity", "Error parsing time: $time24h", e)
        com.islamnotify.common.domain.CrashReporterProvider.instance?.recordNonFatal(e)
        return time24h
    }
}

private fun vibrate(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    if (vibrator.hasVibrator()) {
        val pattern = longArrayOf(0, 50)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
}