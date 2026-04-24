package com.islamnotify.intro.presentation

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.islamnotify.main.domain.PermissionDialogs
import com.islamnotify.ui.theme.IslamNotifyTheme

class IntroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IslamNotifyTheme {
                val viewModel: IntroViewModel by viewModels()
                val permissionDialogsQueue = viewModel.visiblePermissionDialogQueue

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        viewModel.onPermissionResult(
                            permission = PermissionDialogs.NOTIFICATION,
                            isGranted = isGranted
                        )
                    }
                )

                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                    onResult = { perms ->
                        perms.forEach { permission ->
                            viewModel.onPermissionResult(
                                permission = when (permission.key) {
                                    Manifest.permission.POST_NOTIFICATIONS -> PermissionDialogs.NOTIFICATION
                                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION -> PermissionDialogs.LOCATION
                                    Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> PermissionDialogs.BATTERY
                                    else -> null
                                },
                                isGranted = permission.value
                            )
                        }
                    }
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {

                    }) {
                        Text("Location")
                    }

                    Button(onClick = {
                        notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }) {
                        Text("Notification")
                    }

                    Button(onClick = {

                    }) {
                        Text("Battery")
                    }
                }

                permissionDialogsQueue.reversed().forEach { permission ->
                    DeniedPermissionDialog(
                        permissionTextProvider = when (permission) {
                            PermissionDialogs.LOCATION -> LocationPermissionTextProvider()
                            PermissionDialogs.BATTERY -> BatteryPermissionTextProvider()
                            PermissionDialogs.NOTIFICATION -> BatteryPermissionTextProvider()
                        },
                        isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                            when (permission) {
                                PermissionDialogs.LOCATION -> Manifest.permission.ACCESS_COARSE_LOCATION
                                PermissionDialogs.BATTERY -> Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                PermissionDialogs.NOTIFICATION -> Manifest.permission.POST_NOTIFICATIONS
                            }
                        ),
                        onDismiss = { viewModel.dismissDialog() },
                        onOkClick = {
                            viewModel.dismissDialog()
                            val permissions = mutableListOf<String>()
                            when(permission){
                                PermissionDialogs.BATTERY -> permissions.add(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                PermissionDialogs.LOCATION -> {
                                    permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
                                    permissions.add (Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                                PermissionDialogs.NOTIFICATION -> permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                            }

                        },
                        onGoToAppSettingsClick = {},
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }


    }
}