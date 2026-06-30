package com.islamnotify.intro.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamnotify.R

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
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(
                text = stringResource(permissionTextProvider.getTitleRes(isPermanentlyDeclined)),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 26.sp
            )
        },
        text = {
            Text(
                text = stringResource(permissionTextProvider.getDescriptionRes(isPermanentlyDeclined)),
                fontSize = 14.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isPermanentlyDeclined) onGoToAppSettingsClick() else onOkClick()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isPermanentlyDeclined) {
                        stringResource(R.string.dialog_perm_grant)
                    } else {
                        stringResource(R.string.dialog_perm_ok)
                    },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = stringResource(R.string.dialog_perm_cancel),
                    fontSize = 14.sp
                )
            }
        },
        modifier = modifier
    )
}

interface PermissionTextProvider {
    @StringRes fun getTitleRes(isPermanentlyDeclined: Boolean): Int
    @StringRes fun getDescriptionRes(isPermanentlyDeclined: Boolean): Int
}

class NotificationPermissionTextProvider : PermissionTextProvider {
    override fun getTitleRes(isPermanentlyDeclined: Boolean) =
        R.string.dialog_perm_notification_title

    override fun getDescriptionRes(isPermanentlyDeclined: Boolean) =
        if (isPermanentlyDeclined) R.string.dialog_perm_notification_permanent
        else R.string.dialog_perm_notification_rationale
}

class LocationPermissionTextProvider : PermissionTextProvider {
    override fun getTitleRes(isPermanentlyDeclined: Boolean) =
        R.string.dialog_perm_location_title

    override fun getDescriptionRes(isPermanentlyDeclined: Boolean) =
        if (isPermanentlyDeclined) R.string.dialog_perm_location_permanent
        else R.string.dialog_perm_location_rationale
}

class BatteryPermissionTextProvider : PermissionTextProvider {
    override fun getTitleRes(isPermanentlyDeclined: Boolean) =
        R.string.dialog_perm_battery_title

    override fun getDescriptionRes(isPermanentlyDeclined: Boolean) =
        if (isPermanentlyDeclined) R.string.dialog_perm_battery_permanent
        else R.string.dialog_perm_battery_rationale
}
