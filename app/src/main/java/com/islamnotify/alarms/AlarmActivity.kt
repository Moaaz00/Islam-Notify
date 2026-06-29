package com.islamnotify.alarms

import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class AlarmActivity : ComponentActivity() {

    private var alarmId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        showOverLockscreen()
        super.onCreate(savedInstanceState)

        alarmId = intent.getIntExtra(Constants.SERVICE_KEY_ALARM_ID, -1)

        setContent {
            MaterialTheme {
                AlarmScreen(
                    alarmId = alarmId,
                    onSnoozeClick = {
                        sendActionToService(AlarmService.ACTION_SNOOZE)
                        finish()
                    },
                    onDismissClick = {
                        sendActionToService(AlarmService.ACTION_DISMISS)
                        finish()
                    }
                )
            }
        }
    }

    private fun sendActionToService(action: String) {
        val intent = Intent(action).apply {
            setPackage(packageName)
            putExtra(Constants.SERVICE_KEY_ALARM_ID, alarmId)
        }
        this.sendBroadcast(intent)
    }

    private fun showOverLockscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
    }
}


@Composable
fun AlarmScreen(
    alarmId: Int,
    onSnoozeClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9)), // Soft, pleasant light green
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            // Alarm Text Title
            Text(
                text = "Alarm is Ringing",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20) // Deep Islamic green
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle indicating which alarm is active
            Text(
                text = "Active Alarm ID: $alarmId",
                fontSize = 16.sp,
                color = Color(0xFF4E342E) // Soft brown
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Snooze Button
            Button(
                onClick = onSnoozeClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50), // Standard Green
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Snooze",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dismiss Button
            Button(
                onClick = onDismissClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC62828), // Gentle Red for cancellation
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Dismiss",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}