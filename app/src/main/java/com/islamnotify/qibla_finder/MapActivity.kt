package com.islamnotify.qibla_finder

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.islamnotify.ui.theme.IslamNotifyTheme

class MapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IslamNotifyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    mainScreen(context = this, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun mainScreen(context: Context, modifier: Modifier){
    Column(modifier = modifier) {
        GoogleMapsCanvas(context,  Modifier.weight(2f))
    }
}
