package com.islamnotify.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
    fun TestScreenContent(
        goToSettings: () -> Unit
    ){
        Column(modifier = Modifier.fillMaxSize().background(Color.Yellow)) {
            Button(onClick = { goToSettings() }) {
                Text(text = "Go to settings")
            }
        }
    }
