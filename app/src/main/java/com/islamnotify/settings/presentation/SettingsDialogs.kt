package com.islamnotify.settings.presentation

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamnotify.R
import com.islamnotify.common.AppUtils.getLocalizedContext
import com.islamnotify.ui.theme.AppThemeTypes

object SettingsDialogs {

    @Composable
    fun <T> SingleSelectDialog(
        title: String,
        items: List<T>,
        selectedItem: T,
        itemLabel: (T) -> String,
        onDismiss: () -> Unit,
        onConfirm: (T) -> Unit,
        topContent: @Composable (() -> Unit)? = null,
        leadingContent: @Composable ((T) -> Unit)? = null // Added to support icons or color previews
    ) {
        var currentSelection by remember(selectedItem) { mutableStateOf(selectedItem) }

        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = title,
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(modifier = Modifier.heightIn(max = 450.dp)) {
                    topContent?.invoke()
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(items) { item ->
                            val isSelected = (item == currentSelection)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { currentSelection = item }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                // Inject leading content (like the color circle) if provided
                                if (leadingContent != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    leadingContent(item)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = itemLabel(item),
                                    fontFamily = Manrope,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirm(currentSelection) }) {
                    Text(
                        stringResource(R.string.settings_dialog_apply),
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        stringResource(R.string.settings_dialog_cancel),
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }

    @Composable
    fun <T> MultiSelectDialog(
        title: String,
        items: List<T>,
        initialSelectedItems: Set<T>,
        itemLabel: (T) -> String,
        onDismiss: () -> Unit,
        onConfirm: (Set<T>) -> Unit
    ) {
        val selectedItems = remember { mutableStateListOf<T>().apply { addAll(initialSelectedItems) } }

        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = title,
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Box(modifier = Modifier.heightIn(max = 450.dp)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(items) { item ->
                            val isChecked = selectedItems.contains(item)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (isChecked) selectedItems.remove(item) else selectedItems.add(item)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = itemLabel(item),
                                    fontFamily = Manrope,
                                    fontSize = 15.sp,
                                    fontWeight = if (isChecked) FontWeight.Medium else FontWeight.Normal,
                                    color = if (isChecked) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirm(selectedItems.toSet()) }) {
                    Text(
                        stringResource(R.string.settings_dialog_apply),
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        stringResource(R.string.settings_dialog_cancel),
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }



    @Composable
    fun ThemeSelectionDialog(
        initialTheme: AppThemeTypes,
        onDismiss: () -> Unit,
        onConfirm: (AppThemeTypes) -> Unit
    ) {
        val context = LocalContext.current

        SingleSelectDialog(
            title = stringResource(R.string.settings_choose_theme),
            items = AppThemeTypes.entries.toList(),
            selectedItem = initialTheme,
            // Map the enum to your localized string
            itemLabel = { theme -> theme.mapToString(context.getLocalizedContext()) },
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            // Here is how we include the Color Preview Circle!
            leadingContent = { theme ->
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    color = getPreviewColorForTheme(theme),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f))
                ) {}
            }
        )
    }


//    /**THEME DIALOG*/
//    @Composable
//    fun ThemeSelectionDialog(
//        initialTheme: AppThemeTypes,
//        onDismiss: () -> Unit,
//        onConfirm: (AppThemeTypes) -> Unit
//    ) {
//        var selectedTheme by remember(initialTheme) { mutableStateOf(initialTheme) }
//
//        AlertDialog(
//            onDismissRequest = onDismiss,
//            shape = RoundedCornerShape(28.dp),
//            containerColor = MaterialTheme.colorScheme.surface,
//            title = {
//                Text(
//                    text = stringResource(R.string.settings_choose_theme),
//                    fontFamily = Manrope,
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 20.sp,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//            },
//            text = {
//                // Set a max height so the dialog doesn't take the full screen height
//                Box(modifier = Modifier.heightIn(max = 450.dp)) {
//                    LazyColumn(
//                        modifier = Modifier.fillMaxWidth(),
//                        verticalArrangement = Arrangement.spacedBy(4.dp),
//                        contentPadding = PaddingValues(vertical = 8.dp)
//                    ) {
//                        items(AppThemeTypes.entries.toTypedArray()) { theme ->
//                            ThemeRadioButtonItem(
//                                themeType = theme,
//                                isSelected = (theme == selectedTheme),
//                                onClick = { selectedTheme = theme }
//                            )
//                        }
//                    }
//                }
//            },
//            confirmButton = {
//                TextButton(onClick = { onConfirm(selectedTheme) }) {
//                    Text(
//                        stringResource(R.string.settings_dialog_apply),
//                        fontFamily = Manrope,
//                        fontWeight = FontWeight.Bold,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = onDismiss) {
//                    Text(
//                        stringResource(R.string.settings_dialog_cancel),
//                        fontFamily = Manrope,
//                        fontWeight = FontWeight.Medium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//        )
//    }
//
//
//    @Composable
//    fun ThemeRadioButtonItem(
//        themeType: AppThemeTypes,
//        isSelected: Boolean,
//        onClick: () -> Unit
//    ) {
//        // Helper to format enum name (e.g., "BROWN1" -> "Brown 1")
//        val displayName = themeType.mapToString(LocalContext.current.getLocalizedContext())
//
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .clip(RoundedCornerShape(12.dp))
//                .clickable { onClick() }
//                .padding(vertical = 8.dp, horizontal = 4.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            RadioButton(
//                selected = isSelected,
//                onClick = null,
//                colors = RadioButtonDefaults.colors(
//                    selectedColor = MaterialTheme.colorScheme.primary,
//                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            )
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            // Visual Color Preview Circle
//            Surface(
//                modifier = Modifier.size(24.dp),
//                shape = CircleShape,
//                color = getPreviewColorForTheme(themeType),
//                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f))
//            ) {}
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            Text(
//                text = displayName,
//                fontFamily = Manrope,
//                fontSize = 14.sp,
//                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
//                color = if (isSelected) MaterialTheme.colorScheme.onSurface
//                else MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//    }


    fun AppThemeTypes.mapToString(context: Context): String{
        return when (this) {
            AppThemeTypes.GREEN_DARK -> context.getString(R.string.settings_theme_green_dark)
            AppThemeTypes.GREEN_LIGHT -> context.getString(R.string.settings_theme_green_light)
            AppThemeTypes.PINK_DARK -> context.getString(R.string.settings_theme_pink_dark)
            AppThemeTypes.PINK_LIGHT -> context.getString(R.string.settings_theme_pink_light)
            AppThemeTypes.BLUE_DARK -> context.getString(R.string.settings_theme_blue_dark)
            AppThemeTypes.BLUE_LIGHT -> context.getString(R.string.settings_theme_blue_light)
            AppThemeTypes.YELLOW_DARK -> context.getString(R.string.settings_theme_yellow_dark)
            AppThemeTypes.YELLOW_LIGHT -> context.getString(R.string.settings_theme_yellow_light)
            AppThemeTypes.BROWN_DARK -> context.getString(R.string.settings_theme_brown_dark)
            AppThemeTypes.BROWN_LIGHT -> context.getString(R.string.settings_theme_brown_light)
            AppThemeTypes.RED_DARK -> context.getString(R.string.settings_theme_red_dark)
            AppThemeTypes.RED_LIGHT -> context.getString(R.string.settings_theme_red_light)
            else -> context.getString(R.string.settings_unknown_place_holder)
        }
    }

    @Composable
    fun getPreviewColorForTheme(theme: AppThemeTypes): Color {
        return when (theme) {
            AppThemeTypes.GREEN_LIGHT -> Color(0xFF85A05F)
            AppThemeTypes.GREEN_DARK -> Color(0xFF406238)

            AppThemeTypes.RED_DARK -> Color(0xFF790D2F)
            AppThemeTypes.RED_LIGHT -> Color(0xFF9D153E)

            AppThemeTypes.PINK_DARK -> Color(0xFFB94455)
            AppThemeTypes.PINK_LIGHT -> Color(0xFFFDA4AF)

            AppThemeTypes.BROWN_DARK -> Color(0xFF694D34)
            AppThemeTypes.BROWN_LIGHT -> Color(0xFFCBA584)

            AppThemeTypes.YELLOW_DARK -> Color(0xFFAF8C3F)
            AppThemeTypes.YELLOW_LIGHT -> Color(0xFFD5BA7E)

            AppThemeTypes.BLUE_DARK -> Color(0xFF476375)
            AppThemeTypes.BLUE_LIGHT -> Color(0xFF809EB0)
            else -> Color(0x00000000)
        }
    }

}