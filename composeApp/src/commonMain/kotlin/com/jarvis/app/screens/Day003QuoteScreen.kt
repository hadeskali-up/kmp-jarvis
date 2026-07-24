package com.jarvis.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jarvis.app.models.Quote
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteScreen(onBack: () -> Unit) {
    val quotes = remember {
        listOf(
            Quote("1", "The best way to predict the future is to invent it.", "Alan Kay"),
            Quote("2", "Code is like humor. When you have to explain it, it's bad.", "Cory House"),
            Quote("3", "First, solve the problem. Then, write the code.", "John Johnson"),
            Quote("4", "Make it work, make it right, make it fast.", "Kent Beck"),
            Quote("5", "Talk is cheap. Show me the code.", "Linus Torvalds"),
            Quote("6", "Any fool can write code that a computer can understand. Good programmers write code that humans can understand.", "Martin Fowler"),
            Quote("7", "The only way to learn a new programming language is by writing programs in it.", "Dennis Ritchie"),
            Quote("8", "Simplicity is the soul of efficiency.", "Austin Freeman")
        )
    }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(500)
            currentIndex = (currentIndex + 1) % quotes.size
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quote of the Day") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "\u201C",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = quotes[currentIndex].text,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "- ${quotes[currentIndex].author}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            FilledTonalButton(
                onClick = { isRefreshing = true },
                enabled = !isRefreshing
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isRefreshing) "Loading..." else "New Quote")
            }
        }
    }
}
