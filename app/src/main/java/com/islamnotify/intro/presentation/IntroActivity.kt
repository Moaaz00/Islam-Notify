package com.islamnotify.intro.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.islamnotify.main.domain.PermissionDialogs
import com.islamnotify.main.presentation.MainActivity
import com.islamnotify.ui.theme.IslamNotifyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroActivity : ComponentActivity() {

    private val viewModel: IntroViewModel by viewModels()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onPermissionResult(PermissionDialogs.LOCATION, granted)
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(PermissionDialogs.NOTIFICATION, granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialTheme = viewModel.initialTheme()

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            IslamNotifyTheme(themeType = initialTheme) {
                IntroScreen(
                    uiState = uiState,
                    showNotificationRow = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
                    isPermanentlyDeclined = ::isPermanentlyDeclined,
                    onPageChange = viewModel::setPage,
                    onGrantLocation = ::requestLocation,
                    onGrantNotification = ::requestNotification,
                    onGrantBattery = viewModel::showBatterySheet,
                    onOpenBatterySettings = {
                        viewModel.dismissBatterySheet()
                        openAppDetailsSettings()
                    },
                    onDismissBatterySheet = viewModel::dismissBatterySheet,
                    onShowSkipWarning = viewModel::showSkipWarning,
                    onDismissSkipWarning = viewModel::dismissSkipWarning,
                    onFinish = { viewModel.completeIntro(::goToMainActivity) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check live permission state, e.g. after the user returns from a settings screen.
        viewModel.refreshPermissionStates(
            location = hasLocationPermission(),
            notification = hasNotificationPermission(),
            battery = isIgnoringBatteryOptimizations()
        )
    }

    // --- Permission requests ---

    private fun requestLocation() {
        if (isPermanentlyDeclined(PermissionDialogs.LOCATION)) {
            openAppDetailsSettings()
            return
        }
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
        viewModel.markRequested(PermissionDialogs.LOCATION)
    }

    private fun requestNotification() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        if (isPermanentlyDeclined(PermissionDialogs.NOTIFICATION)) {
            openNotificationSettings()
            return
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        viewModel.markRequested(PermissionDialogs.NOTIFICATION)
    }

    /**
     * A permission is "permanently declined" when it has been requested before but the system
     * no longer offers a rationale dialog (the user chose "Don't ask again", or denied twice on
     * Android 11+). In that state the only path left is the app settings screen.
     */
    private fun isPermanentlyDeclined(permission: PermissionDialogs): Boolean {
        return when (permission) {
            PermissionDialogs.LOCATION -> {
                viewModel.uiState.value.locationRequested &&
                        !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            PermissionDialogs.NOTIFICATION -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
                viewModel.uiState.value.notificationRequested &&
                        !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
            }

            // Battery uses the settings screen directly, never the runtime-permission flow.
            PermissionDialogs.BATTERY -> false
        }
    }

    // --- Permission state checks ---

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun hasNotificationPermission(): Boolean {
        // Notifications are always allowed by default below Android 13.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    // --- Navigation to system screens ---

    private fun openAppDetailsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        startActivity(intent)
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
