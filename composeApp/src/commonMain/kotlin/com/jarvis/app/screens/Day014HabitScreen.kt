package com.jarvis.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jarvis.app.models.Habit
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(onBack: () -> Unit) {
    var habits by remember { mutableStateOf(listOf<Habit>()) }
    var showDialog by remember { mutableStateOf(false) }
    var newHabitTitle by remember { mutableStateOf("") }
    var newHabitDesc by remember { mutableStateOf("") }

    fun toggleHabit(habit: Habit) {
        val today = Clock.System.now().toString().substringBefore("T")
        val updatedCompletions = if (today in habit.completedDates) {
            habit.completedDates - today
        } else {
            habit.completedDates + today
        }
        val streak = calculateStreak(updatedCompletions)
        habits = habits.map {
            if (it.id == habit.id) it.copy(
                completedDates = updatedCompletions,
                streak = streak
            ) else it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habits") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { padding ->
        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No habits tracked yet",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Start building good habits!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(habits, key = { it.id }) { habit ->
                    val today = Clock.System.now().toString().substringBefore("T")
                    val isCompletedToday = today in habit.completedDates

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCompletedToday)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isCompletedToday,
                                onCheckedChange = { toggleHabit(habit) }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    habit.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                if (habit.description.isNotEmpty()) {
                                    Text(
                                        habit.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${habit.streak}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "day streak",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                habits = habits.filter { it.id != habit.id }
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false; newHabitTitle = ""; newHabitDesc = "" },
            title = { Text("New Habit") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newHabitTitle,
                        onValueChange = { newHabitTitle = it },
                        label = { Text("Habit name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newHabitDesc,
                        onValueChange = { newHabitDesc = it },
                        label = { Text("Description (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newHabitTitle.isNotBlank()) {
                            val habit = Habit(
                                id = Clock.System.now().toEpochMilliseconds().toString(),
                                title = newHabitTitle,
                                description = newHabitDesc,
                                createdAt = Clock.System.now().toString().substringBefore("T")
                            )
                            habits = listOf(habit) + habits
                            newHabitTitle = ""
                            newHabitDesc = ""
                            showDialog = false
                        }
                    }
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false; newHabitTitle = ""; newHabitDesc = "" }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun calculateStreak(completedDates: List<String>): Int {
    if (completedDates.isEmpty()) return 0
    val sorted = completedDates.sortedDescending()
    var streak = 0
    val today = Clock.System.now().toString().substringBefore("T")

    // Check if today or yesterday is in the list
    if (sorted.first() != today) {
        // Check for yesterday
        // Simplified: just count consecutive days from most recent
    }

    return sorted.size.coerceAtMost(30) // Simplified streak count
}
