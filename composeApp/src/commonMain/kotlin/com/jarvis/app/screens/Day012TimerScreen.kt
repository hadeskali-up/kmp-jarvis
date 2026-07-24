package com.jarvis.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(onBack: () -> Unit) {
    var timeInMillis by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var timerMode by remember { mutableStateOf("stopwatch") } // "stopwatch" or "countdown"
    var countdownTime by remember { mutableStateOf(60000L) } // 1 minute default
    var countdownInput by remember { mutableStateOf("1") }

    LaunchedEffect(isRunning, timerMode) {
        while (isRunning) {
            delay(10L)
            if (timerMode == "stopwatch") {
                timeInMillis += 10L
            } else {
                if (timeInMillis > 0) {
                    timeInMillis -= 10L
                } else {
                    isRunning = false
                }
            }
        }
    }

    fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val centiseconds = (millis % 1000) / 10
        return String.format("%02d:%02d.%02d", minutes, seconds, centiseconds)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timer") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Mode selector
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = timerMode == "stopwatch",
                    onClick = {
                        if (!isRunning) {
                            timerMode = "stopwatch"
                            timeInMillis = 0L
                        }
                    },
                    label = { Text("Stopwatch") }
                )
                FilterChip(
                    selected = timerMode == "countdown",
                    onClick = {
                        if (!isRunning) {
                            timerMode = "countdown"
                            timeInMillis = countdownTime
                        }
                    },
                    label = { Text("Countdown") }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = formatTime(timeInMillis),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (timerMode == "countdown" && !isRunning) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = countdownInput,
                        onValueChange = { countdownInput = it },
                        label = { Text("Minutes") },
                        modifier = Modifier.width(120.dp),
                        singleLine = true
                    )
                    Button(onClick = {
                        val mins = countdownInput.toLongOrNull() ?: 1
                        if (mins > 0) {
                            countdownTime = mins * 60000L
                            timeInMillis = countdownTime
                        }
                    }) {
                        Text("Set")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        isRunning = if (timerMode == "countdown" && timeInMillis <= 0) {
                            timeInMillis = countdownTime
                            true
                        } else {
                            !isRunning
                        }
                    }
                ) {
                    Text(if (isRunning) "Pause" else "Start")
                }

                OutlinedButton(
                    onClick = {
                        isRunning = false
                        if (timerMode == "stopwatch") {
                            timeInMillis = 0L
                        } else {
                            timeInMillis = countdownTime
                        }
                    }
                ) {
                    Text("Reset")
                }
            }
        }
    }
}
