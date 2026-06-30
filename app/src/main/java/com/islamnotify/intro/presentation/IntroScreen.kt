package com.islamnotify.intro.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamnotify.R
import com.islamnotify.intro.presentation.components.BatteryGuideSheet
import com.islamnotify.main.domain.PermissionDialogs
import kotlinx.coroutines.launch

private const val PAGE_WELCOME = 0
private const val PAGE_FEATURE = 1
private const val PAGE_PERMISSIONS = 2
private const val PAGE_READY = 3
private const val PAGE_COUNT = 4

@Composable
fun IntroScreen(
    uiState: IntroUiState,
    showNotificationRow: Boolean,
    isPermanentlyDeclined: (PermissionDialogs) -> Boolean,
    onPageChange: (Int) -> Unit,
    onGrantLocation: () -> Unit,
    onGrantNotification: () -> Unit,
    onGrantBattery: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onDismissBatterySheet: () -> Unit,
    onShowSkipWarning: () -> Unit,
    onDismissSkipWarning: () -> Unit,
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { onPageChange(it) }
    }

    val currentPage = pagerState.currentPage
    val isLastPage = currentPage == PAGE_READY

    fun goToPage(page: Int) {
        scope.launch { pagerState.animateScrollToPage(page) }
    }

    fun onNextClicked() {
        when (currentPage) {
            PAGE_PERMISSIONS -> {
                val optionalDone =
                    (!showNotificationRow || uiState.notificationGranted) && uiState.batteryGranted
                if (optionalDone) goToPage(PAGE_PERMISSIONS + 1) else onShowSkipWarning()
            }

            PAGE_READY -> onFinish()
            else -> goToPage(currentPage + 1)
        }
    }

    // Location is mandatory: lock the Next button on the permissions page until granted.
    val nextEnabled = currentPage != PAGE_PERMISSIONS || uiState.locationGranted

    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            // Pager area: cream background with wave overlays at the bottom.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = false,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        PAGE_WELCOME -> InfoSlide(
                            painter = rememberAppIconPainter(),
                            title = stringResource(R.string.intro_welcome_title),
                            body = stringResource(R.string.intro_welcome_body)
                        )

                        PAGE_FEATURE -> InfoSlide(
                            painter = painterResource(R.drawable.resized_figma_expanded_notification),
                            title = stringResource(R.string.intro_feature_title),
                            body = stringResource(R.string.intro_feature_body),
                            imageHeight = 220.dp
                        )

                        PAGE_PERMISSIONS -> PermissionsSlide(
                            uiState = uiState,
                            showNotificationRow = showNotificationRow,
                            isPermanentlyDeclined = isPermanentlyDeclined,
                            onGrantLocation = onGrantLocation,
                            onGrantNotification = onGrantNotification,
                            onGrantBattery = onGrantBattery
                        )

                        PAGE_READY -> InfoSlide(
                            painter = rememberAppIconPainter(),
                            title = stringResource(R.string.intro_ready_title),
                            body = stringResource(R.string.intro_ready_body)
                        )
                    }
                }

                // Subtle semi-transparent ripple behind the main wave.
                Image(
                    painter = painterResource(R.drawable.ic_wave_background),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                )

                // Primary-colored wave — creates the wavy transition into the green bottom bar.
                Image(
                    painter = painterResource(R.drawable.ic_wave_foreground),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    colorFilter = ColorFilter.tint(primaryColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                )
            }

            // Green bottom section: page indicators + full-width Next / Finish button.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(primaryColor)
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .padding(vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PageIndicator(pageCount = PAGE_COUNT, currentPage = currentPage)

                Spacer(Modifier.height(30.dp))

                Button(
                    onClick = { onNextClicked() },
                    enabled = nextEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = primaryColor,
                        disabledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                        disabledContentColor = primaryColor.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = if (isLastPage) {
                            stringResource(R.string.intro_finish)
                        } else {
                            stringResource(R.string.intro_next)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (uiState.showSkipWarning) {
        SkipWarningDialog(
            onConfirm = {
                onDismissSkipWarning()
                goToPage(PAGE_PERMISSIONS + 1)
            },
            onDismiss = onDismissSkipWarning
        )
    }

    if (uiState.showBatterySheet) {
        BatteryGuideSheet(
            onOpenSettings = onOpenBatterySettings,
            onDismiss = onDismissBatterySheet
        )
    }
}

@Composable
private fun SkipWarningDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.intro_skip_warning_title)) },
        text = { Text(text = stringResource(R.string.intro_skip_warning_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.intro_skip_confirm),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.intro_skip_cancel),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
