package com.jarvis.app.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ActivityChart(
    data: List<Float> = emptyList(),
    modifier: Modifier = Modifier
) {
    // Simplified activity chart placeholder
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.height(100.dp).padding(8.dp)) {
            // Chart rendering would go here
        }
    }
}
