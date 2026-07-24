package com.jarvis.app.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CommitHeatmap(
    data: List<Int> = emptyList(),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Commit Heatmap", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            // 7x7 grid for heatmap
            for (row in 0 until 7) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0 until 7) {
                        val intensity = if (row * 7 + col < data.size)
                            data[row * 7 + col].coerceIn(0, 4) / 4f
                        else 0f
                        Surface(
                            modifier = Modifier.size(12.dp),
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.2f + intensity * 0.6f
                            )
                        ) {}
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
