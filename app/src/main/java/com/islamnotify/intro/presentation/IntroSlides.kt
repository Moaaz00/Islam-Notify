package com.islamnotify.intro.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.Image
import com.islamnotify.R
import com.islamnotify.intro.presentation.components.PermissionItem
import com.islamnotify.main.domain.PermissionDialogs

/**
 * Loads the app launcher icon as a [Painter]. The launcher icon is an adaptive-icon XML, which
 * painterResource() cannot decode, so we rasterize it to a bitmap first.
 */
@Composable
fun rememberAppIconPainter(): Painter {
    val context = LocalContext.current
    val imageBitmap = remember {
        val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher_round)!!
        drawable.toBitmap().asImageBitmap()
    }
    return BitmapPainter(imageBitmap)
}

/**
 * Plain informational slide (welcome / feature / ready).
 *
 * Content is vertically centered in the pager area, shifted up by [WAVE_HEIGHT_DP] so it stays
 * in the cream zone above the wave — matching the Java project's ConstraintLayout+marginBottom approach.
 */
private val WAVE_HEIGHT_DP = 140.dp

@Composable
fun InfoSlide(
    painter: Painter,
    title: String,
    body: String,
    imageHeight: Dp = 130.dp,
    modifier: Modifier = Modifier,
    imageContentScale: ContentScale = ContentScale.Fit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Trailing padding reserves the wave band so centering doesn't push content
                // behind the wave — equivalent to marginBottom="70dp" in the Java layout.
                .padding(bottom = WAVE_HEIGHT_DP)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = imageContentScale,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .padding(horizontal = 40.dp)
            )

            Spacer(Modifier.height(60.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * The interactive permissions slide.
 *
 * All trailing action buttons (Grant / Open Settings) are measured dynamically so they share
 * the same minimum width — no hardcoded dp values.
 */
@Composable
fun PermissionsSlide(
    uiState: IntroUiState,
    showNotificationRow: Boolean,
    isPermanentlyDeclined: (PermissionDialogs) -> Boolean,
    onGrantLocation: () -> Unit,
    onGrantNotification: () -> Unit,
    onGrantBattery: () -> Unit,
    modifier: Modifier = Modifier
) {
    var maxButtonPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val minButtonWidth: Dp = with(density) { maxButtonPx.toDp() }
    val onMeasured: (Int) -> Unit = { w -> if (w > maxButtonPx) maxButtonPx = w }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        Text(
            text = stringResource(R.string.intro_permissions_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.intro_permissions_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(28.dp))

        PermissionItem(
            iconRes = R.drawable.ic_location_pin_filled,
            title = stringResource(R.string.intro_perm_location_title),
            subtitle = stringResource(R.string.intro_perm_location_subtitle),
            tagText = stringResource(R.string.intro_perm_required),
            isRequired = true,
            isGranted = uiState.locationGranted,
            isPermanentlyDeclined = isPermanentlyDeclined(PermissionDialogs.LOCATION),
            grantLabel = stringResource(R.string.intro_perm_grant),
            openSettingsLabel = stringResource(R.string.intro_perm_open_settings),
            minTrailingWidth = minButtonWidth,
            onTrailingMeasured = onMeasured,
            onAction = onGrantLocation
        )

        if (showNotificationRow) {
            Spacer(Modifier.height(14.dp))
            PermissionItem(
                iconRes = R.drawable.ic_notification_filled,
                title = stringResource(R.string.intro_perm_notification_title),
                subtitle = stringResource(R.string.intro_perm_notification_subtitle),
                tagText = stringResource(R.string.intro_perm_optional),
                isRequired = false,
                isGranted = uiState.notificationGranted,
                isPermanentlyDeclined = isPermanentlyDeclined(PermissionDialogs.NOTIFICATION),
                grantLabel = stringResource(R.string.intro_perm_grant),
                openSettingsLabel = stringResource(R.string.intro_perm_open_settings),
                minTrailingWidth = minButtonWidth,
                onTrailingMeasured = onMeasured,
                onAction = onGrantNotification
            )
        }

        Spacer(Modifier.height(14.dp))
        PermissionItem(
            iconRes = R.drawable.ic_intro_battery,
            title = stringResource(R.string.intro_perm_battery_title),
            subtitle = stringResource(R.string.intro_perm_battery_subtitle),
            tagText = stringResource(R.string.intro_perm_optional),
            isRequired = false,
            isGranted = uiState.batteryGranted,
            isPermanentlyDeclined = false,
            grantLabel = stringResource(R.string.intro_perm_grant),
            openSettingsLabel = stringResource(R.string.intro_perm_open_settings),
            minTrailingWidth = minButtonWidth,
            onTrailingMeasured = onMeasured,
            onAction = onGrantBattery
        )

        // Reserve space so content never slides behind the wave band.
        Spacer(Modifier.height(160.dp))
    }
}

/**
 * Row of circular page indicator dots rendered on the green bottom bar.
 * Selected dot is fully opaque (onPrimary), unselected is 30 % — matching the Java project's
 * tab_indicator_selected / tab_indicator_unselected drawables (#FFF8E1 vs #4CFFF8E1).
 */
@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val color = if (index == currentPage) onPrimary else onPrimary.copy(alpha = 0.3f)
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
