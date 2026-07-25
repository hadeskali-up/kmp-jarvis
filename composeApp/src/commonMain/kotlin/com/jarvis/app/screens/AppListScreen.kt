package com.jarvis.app.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jarvis.app.navigation.Screen

private data class AppItem(
    val title: String,
    val icon: ImageVector,
    val screen: Screen,
    val description: String
)

private val apps = listOf(
    AppItem("Chat", Icons.Default.Chat, Screen.Chat, "AI chat assistant"),
    AppItem("Expenses", Icons.Default.AccountBalanceWallet, Screen.ExpenseCapture, "Track and categorize spending"),
    AppItem("Flashcards", Icons.Default.School, Screen.Flashcard, "Study with spaced repetition"),
    AppItem("Timer", Icons.Default.Timer, Screen.Timer, "Pomodoro & focus timer"),
    AppItem("Daily Commits", Icons.Default.DateRange, Screen.DailyCommits, "GitHub commit tracker"),
    AppItem("Notes", Icons.Default.Note, Screen.Notes, "Quick notes & memos"),
    AppItem("Habits", Icons.Default.Favorite, Screen.Habit, "Habit tracker"),
    AppItem("Weather", Icons.Default.Cloud, Screen.Weather, "Current conditions"),
    AppItem("Todo", Icons.Default.List, Screen.Todo, "Task manager"),
    AppItem("Calculator", Icons.Default.Calculate, Screen.Calculator, "Basic calculator"),
    AppItem("Converter", Icons.Default.SwapHoriz, Screen.Converter, "Unit converter"),
    AppItem("Palette", Icons.Default.Palette, Screen.Palette, "Color palette tool"),
    AppItem("Crypto", Icons.Default.CurrencyBitcoin, Screen.Crypto, "Live crypto positions & PnL"),
    AppItem("Quote", Icons.Default.FormatQuote, Screen.Quote, "Daily inspiration")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(onBack: () -> Unit, onNavigate: (Screen) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apps", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(apps) { app ->
                AppRow(app = app, onClick = { onNavigate(app.screen) })
            }
        }
    }
}

@Composable
private fun AppRow(app: AppItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                app.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(app.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(
                    app.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
