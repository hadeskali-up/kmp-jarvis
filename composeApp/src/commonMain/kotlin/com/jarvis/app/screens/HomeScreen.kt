package com.jarvis.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.app.core.UiState
import com.jarvis.app.core.fold
import com.jarvis.app.models.DeepSeekData
import com.jarvis.app.models.RootsysUsageData
import com.jarvis.app.navigation.Screen
import com.jarvis.app.services.DashboardService
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    var creditState by remember { mutableStateOf<UiState<DeepSeekData>>(UiState.Loading) }
    var rootsysState by remember { mutableStateOf<UiState<RootsysUsageData>>(UiState.Loading) }
    val service: DashboardService = koinInject()

    LaunchedEffect(Unit) {
        service.fetchSnapshot().fold(
            onSuccess = {
                creditState = it.deepseek?.let { ds -> UiState.Success(ds) } ?: UiState.Error("No data")
                rootsysState = it.rootsys?.let { rs -> UiState.Success(rs) } ?: UiState.Error("No data")
            },
            onFailure = {
                creditState = UiState.Error(it.message ?: "Failed")
                rootsysState = UiState.Error(it.message ?: "Failed")
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── DeepSeek Credit Banner (powered by UiState) ──
            DeepSeekBanner(creditState)

            Spacer(Modifier.height(16.dp))

            // ── Rootsys Token Usage Banner ──
            RootsysUsageBanner(rootsysState)

            Spacer(Modifier.height(32.dp))

            // ── Two Main Icons ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
            ) {
                HomeTile(
                    icon = Icons.Default.Dashboard,
                    title = "Dashboard",
                    onClick = { onNavigate(Screen.Dashboard) },
                    modifier = Modifier.weight(1f)
                )
                HomeTile(
                    icon = Icons.Default.Apps,
                    title = "Apps",
                    onClick = { onNavigate(Screen.AppList) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ── DeepSeek Credit Banner (UiState-driven) ──
@Composable
private fun DeepSeekBanner(state: UiState<DeepSeekData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "DeepSeek Credits",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))

            state.fold(
                onLoading = {
                    Text(
                        "Loading...",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                },
                onSuccess = { data ->
                    Text(
                        data.display.ifEmpty { "${data.total_balance} ${data.currency}" },
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.TrendingDown,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                        Text(
                            "Today: \$${data.today_used}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                onError = { msg ->
                    Text(
                        "—",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f),
                        textAlign = TextAlign.Center
                    )
                }
            )
        }
    }
}

// ── Rootsys Token Usage Banner ──
@Composable
private fun RootsysUsageBanner(state: UiState<RootsysUsageData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Bolt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Rootsys Token Usage",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))

            state.fold(
                onLoading = {
                    Text(
                        "Loading...",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                },
                onSuccess = { data ->
                    Text(
                        formatTokens(data.today_total),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "today  •  in ${formatTokens(data.today_input)} / out ${formatTokens(data.today_output)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "All-time: ${formatTokens(data.total_tokens)} across ${data.sessions_total} sessions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                },
                onError = { msg ->
                    Text(
                        "—",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.3f),
                        textAlign = TextAlign.Center
                    )
                }
            )
        }
    }
}

// ── Token formatter (e.g. 1.2M, 345K) ──
private fun formatTokens(n: Long): String = when {
    n >= 1_000_000_000 -> "%.1fB".format(n / 1_000_000_000.0)
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000.0)
    n >= 1_000 -> "%.1fK".format(n / 1_000.0)
    else -> n.toString()
}

// ── Home Tile ──
@Composable
private fun HomeTile(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
