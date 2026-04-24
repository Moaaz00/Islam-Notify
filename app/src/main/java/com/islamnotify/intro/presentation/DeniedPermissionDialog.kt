package com.islamnotify.intro.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamnotify.main.domain.PermissionDialogs

@Composable
fun DeniedPermissionDialog(
    permissionTextProvider: PermissionTextProvider,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGoToAppSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider()
                Text(
                    text = if (isPermanentlyDeclined) {
                        "Grant Permission"
                    } else {
                        "OK"
                    },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isPermanentlyDeclined) {
                                onGoToAppSettingsClick()
                            } else {
                                onOkClick()
                            }
                        }

                )
            }
        },
        title = {
            Text(text = permissionTextProvider.getTitle(isPermanentlyDeclined))
        },
        text = {
            Text(
                text = permissionTextProvider.getDescription(isPermanentlyDeclined)
            )
        },
        modifier = modifier
    )
}

interface PermissionTextProvider {
    fun getTitle(isPermanentlyDeclined: Boolean): String
    fun getDescription(isPermanentlyDeclined: Boolean): String
}

class NotificationPermissionTextProvider(): PermissionTextProvider{
    override fun getTitle(isPermanentlyDeclined: Boolean): String {
        return "Notification Permission Required"
    }

    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        if (isPermanentlyDeclined){
            return "Notification Permission is needed for features like always on notification and the events features. Do you want to grant it?"
        }

        return "Notification Permission is needed for features like always on notification and the events features. You can go to the app settings and grant it there."
    }
}

class LocationPermissionTextProvider: PermissionTextProvider{
    override fun getTitle(isPermanentlyDeclined: Boolean): String {
        return "Location Permission Required"
    }

    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        if (isPermanentlyDeclined){
            return "Location Permission is required to get the prayer times of the current location. grant it"
        }

        return "Location Permission is required to get the prayer times of the current location. You can go to the app settings and grant it there."
    }
}

class BatteryPermissionTextProvider(): PermissionTextProvider{
    override fun getTitle(isPermanentlyDeclined: Boolean): String {
        return "Battery Permission Required"
    }

    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        if (isPermanentlyDeclined){
            return "Battery Permission is required for background features. grant it?"
        }

        return "Battery Permission is required for background features. You can go to the app settings and grant it there."
    }
}