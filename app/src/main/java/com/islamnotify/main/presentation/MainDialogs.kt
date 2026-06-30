package com.islamnotify.main.presentation

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamnotify.R
import com.islamnotify.location.domain.model.LocationFailureCause

@Composable
fun LoadingDialog() {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = stringResource(R.string.dialog_loading_title),
                fontFamily = MainFont,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.dialog_loading_message),
                    fontFamily = MainFont,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun PrayerErrorDialog(onRetry: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = stringResource(R.string.dialog_prayer_error_title),
                fontFamily = MainFont,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.dialog_prayer_error_message),
                fontFamily = MainFont,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text(
                    text = stringResource(R.string.dialog_retry),
                    fontFamily = MainFont,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun LocationErrorDialog(
    cause: LocationFailureCause,
    onRetry: () -> Unit
) {
    val context = LocalContext.current

    val message = when (cause) {
        LocationFailureCause.PERMISSION_DENIED -> stringResource(R.string.dialog_location_permission_denied_message)
        LocationFailureCause.GPS_DISABLED -> stringResource(R.string.dialog_location_gps_disabled_message)
        LocationFailureCause.GENERIC_ERROR -> stringResource(R.string.dialog_location_generic_error_message)
    }

    val confirmLabel = when (cause) {
        LocationFailureCause.PERMISSION_DENIED -> stringResource(R.string.dialog_open_settings)
        LocationFailureCause.GPS_DISABLED -> stringResource(R.string.dialog_enable_gps)
        LocationFailureCause.GENERIC_ERROR -> stringResource(R.string.dialog_retry)
    }

    val onConfirm: () -> Unit = when (cause) {
        LocationFailureCause.PERMISSION_DENIED -> ({
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
            )
        })
        LocationFailureCause.GPS_DISABLED -> ({
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        })
        LocationFailureCause.GENERIC_ERROR -> onRetry
    }

    val tryAgainLabel = stringResource(R.string.dialog_try_again)

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = stringResource(R.string.dialog_location_error_title),
                fontFamily = MainFont,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = message,
                fontFamily = MainFont,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmLabel,
                    fontFamily = MainFont,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = when (cause) {
            LocationFailureCause.PERMISSION_DENIED,
            LocationFailureCause.GPS_DISABLED -> {
                {
                    TextButton(onClick = onRetry) {
                        Text(
                            text = tryAgainLabel,
                            fontFamily = MainFont,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            LocationFailureCause.GENERIC_ERROR -> null
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun PermissionsReminderDialog(
    state: PermissionDialogState,
    onDismiss: () -> Unit,
    onDontAskAgain: () -> Unit
) {
    val context = LocalContext.current

    val openNotificationSettings: () -> Unit = {
        context.startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        )
    }

    val openBatterySettings: () -> Unit = {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.dialog_permissions_title),
                fontFamily = MainFont,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
//                Text(
//                    text = "",
//                    //text = stringResource(R.string.dialog_permissions_message),
//                    fontFamily = MainFont,
//                    style = MaterialTheme.typography.bodyMedium
//                )
                if (state.notificationMissing) {
                    PermissionRow(
                        title = stringResource(R.string.dialog_permissions_notification),
                        description = stringResource(R.string.dialog_permissions_notification_desc),
                        onOpenSettings = openNotificationSettings
                    )
                }
                if (state.batteryMissing) {
                    PermissionRow(
                        title = stringResource(R.string.dialog_permissions_battery),
                        description = stringResource(R.string.dialog_permissions_battery_desc),
                        onOpenSettings = openBatterySettings
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDontAskAgain) {
                Text(
                    text = stringResource(R.string.dialog_permissions_dont_ask),
                    fontFamily = MainFont,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.dialog_permissions_cancel),
                    fontFamily = MainFont,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun PermissionRow(
    title: String,
    description: String,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = MainFont,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontFamily = MainFont,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedButton(
            onClick = onOpenSettings,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = stringResource(R.string.dialog_open_settings),
                fontFamily = MainFont,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
