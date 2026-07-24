package com.jarvis.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

data class FeatureTile(
    val title: String,
    val icon: ImageVector,
    val screen: Screen,
    val color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
)

private val features = listOf(
    FeatureTile("Chat", Icons.Default.Chat, Screen.Chat),
    FeatureTile("Expenses", Icons.Default.AccountBalanceWallet, Screen.ExpenseCapture),
    FeatureTile("Flashcards", Icons.Default.School, Screen.Flashcard),
    FeatureTile("Timer", Icons.Default.Timer, Screen.Timer),
    FeatureTile("Dashboard", Icons.Default.Dashboard, Screen.Dashboard),
    FeatureTile("Daily", Icons.Default.DateRange, Screen.DailyCommits),
    FeatureTile("Notes", Icons.Default.Note, Screen.Notes),
    FeatureTile("Habits", Icons.Default.Favorite, Screen.Habit),
    FeatureTile("Weather", Icons.Default.Cloud, Screen.Weather),
    FeatureTile("Todo", Icons.Default.Checklist, Screen.Todo),
    FeatureTile("Calculator", Icons.Default.Calculate, Screen.Calculator),
    FeatureTile("Converter", Icons.Default.SwapHoriz, Screen.Converter),
    FeatureTile("Palette", Icons.Default.Palette, Screen.Palette),
    FeatureTile("Quote", Icons.Default.FormatQuote, Screen.Quote),
    FeatureTile("Welcome", Icons.Default.WavingHand, Screen.Welcome)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("KMP Jarvis", fontWeight = FontWeight.Bold)
                        Text(
                            "Your AI Companion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(features) { feature ->
                FeatureCard(
                    title = feature.title,
                    icon = feature.icon,
                    onClick = { onNavigate(feature.screen) }
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
