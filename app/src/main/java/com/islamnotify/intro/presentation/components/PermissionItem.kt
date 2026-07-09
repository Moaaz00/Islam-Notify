package com.islamnotify.intro.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PermissionItem(
    @DrawableRes iconRes: Int,
    title: String,
    subtitle: String,
    tagText: String,
    isRequired: Boolean,
    isGranted: Boolean,
    isPermanentlyDeclined: Boolean,
    grantLabel: String,
    openSettingsLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    minTrailingWidth: Dp = 0.dp,
    onTrailingMeasured: (Int) -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (tagText.isNotEmpty()) {
                        Spacer(Modifier.width(if (isRequired) 4.dp else 8.dp))
                        Text(
                            text = tagText,
                            style = if (isRequired) {
                                MaterialTheme.typography.titleLarge
                            } else {
                                MaterialTheme.typography.labelSmall
                            },
                            fontWeight = if (isRequired) FontWeight.Bold else FontWeight.Medium,
                            color = if (isRequired) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                Spacer(Modifier.size(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(8.dp))

            TrailingControl(
                isGranted = isGranted,
                isPermanentlyDeclined = isPermanentlyDeclined,
                grantLabel = grantLabel,
                openSettingsLabel = openSettingsLabel,
                minWidth = minTrailingWidth,
                onAction = onAction,
                onMeasured = onTrailingMeasured
            )
        }
    }
}

@Composable
private fun TrailingControl(
    isGranted: Boolean,
    isPermanentlyDeclined: Boolean,
    grantLabel: String,
    openSettingsLabel: String,
    minWidth: Dp,
    onAction: () -> Unit,
    onMeasured: (Int) -> Unit
) {
    val minWidthModifier = if (minWidth > 0.dp) Modifier.widthIn(min = minWidth) else Modifier

    when {
        isGranted -> {
            // Fixed check circle — not part of the button-width measurement
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        isPermanentlyDeclined -> {
            OutlinedButton(
                onClick = onAction,
                modifier = minWidthModifier.onGloballyPositioned { onMeasured(it.size.width) },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = openSettingsLabel, style = MaterialTheme.typography.labelMedium)
            }
        }

        else -> {
            Button(
                onClick = onAction,
                modifier = minWidthModifier.onGloballyPositioned { onMeasured(it.size.width) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(text = grantLabel, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
