package com.islamnotify.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamnotify.common.PrayerTimesOffset

object PrayerOffsetAdjustmentsScreen {
    @Composable
    fun PrayerTimesOffsetScreen(){
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Text(text = "Prayer times Offsets", modifier = Modifier.padding(32.dp), fontSize = 32.sp)
        }
    }
}