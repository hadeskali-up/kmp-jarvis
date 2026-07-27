package com.jarvis.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.app.core.UiState
import com.jarvis.app.core.fold
import com.jarvis.app.models.CryptoPositionsResponse
import com.jarvis.app.models.DeepSeekData
import com.jarvis.app.models.TradeHistoryResponse
import com.jarvis.app.navigation.Screen
import com.jarvis.app.services.CryptoService
import com.jarvis.app.services.DashboardService
import com.jarvis.app.services.TradeHistoryService
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    var creditState by remember { mutableStateOf<UiState<DeepSeekData>>(UiState.Loading) }
    var cryptoState by remember { mutableStateOf<UiState<CryptoPositionsResponse>>(UiState.Loading) }
    var tradeHistoryState by remember { mutableStateOf<UiState<TradeHistoryResponse>>(UiState.Loading) }
    val service: DashboardService = koinInject()
    val cryptoService: CryptoService = koinInject()
    val tradeHistoryService: TradeHistoryService = koinInject()

    LaunchedEffect(Unit) {
        service.fetchSnapshot().fold(
            onSuccess = {
                creditState = it.deepseek?.let { ds -> UiState.Success(ds) } ?: UiState.Error("No data")
            },
            onFailure = {
                creditState = UiState.Error(it.message ?: "Failed")
            }
        )
    }

    // Auto-refresh crypto positions every 30s
    LaunchedEffect(Unit) {
        while (true) {
            cryptoService.fetchPositions().fold(
                onSuccess = { cryptoState = UiState.Success(it) },
                onFailure = { cryptoState = UiState.Error(it.message ?: "Failed") }
            )
            delay(30_000)
        }
    }

    // Auto-refresh trade history every 30s
    LaunchedEffect(Unit) {
        while (true) {
            tradeHistoryService.fetchTradeHistory(50).fold(
                onSuccess = { tradeHistoryState = UiState.Success(it) },
                onFailure = { tradeHistoryState = UiState.Error(it.message ?: "Failed") }
            )
            delay(30_000)
        }
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
            // ── Daily PnL Banner (top) ──
            DailyPnlBanner(tradeHistoryState) {
                onNavigate(Screen.Dashboard)
            }

            Spacer(Modifier.height(16.dp))

            // ── Crypto PnL Banner ──
            CryptoPnlBanner(cryptoState) {
                onNavigate(Screen.Crypto)
            }

            Spacer(Modifier.height(16.dp))

            // ── DeepSeek Credit Banner ──
            DeepSeekBanner(creditState)

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

// ── Crypto PnL Banner ──
@Composable
private fun CryptoPnlBanner(state: UiState<CryptoPositionsResponse>, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CurrencyBitcoin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Crypto PnL",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))

            state.fold(
                onLoading = {
                    Text(
                        "Loading...",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                },
                onSuccess = { data ->
                    val totalPnl = data.positions.sumOf { it.pnl_usd }
                    val totalValue = data.positions.sumOf { it.market_value }
                    val winners = data.positions.count { it.isProfitable }
                    val losers = data.positions.count { !it.isProfitable }
                    val pnlColor = if (totalPnl >= 0) Color(0xFF4CAF50) else Color(0xFFEF5350)
                    val pnlSign = if (totalPnl >= 0) "+" else ""

                    Text(
                        "$pnlSign$${"%,.2f".format(totalPnl)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$${"%,.2f".format(totalValue)} total  •  ${data.count} open  •  $winners W / $losers L",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                },
                onError = { msg ->
                    Text(
                        "—",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f),
                        textAlign = TextAlign.Center
                    )
                }
            )
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

// ── Daily PnL Banner ──
@Composable
private fun DailyPnlBanner(state: UiState<TradeHistoryResponse>, onClick: () -> Unit) {
    Card(
        onClick = onClick,
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
                Icons.Default.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Daily PnL",
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
                    // Filter trades for today (handle both DD/MM/YY and ISO date formats)
                    val todayDdMmYy = todayDateString()
                    val todayIso = todayIsoDateString()
                    val todayTrades = data.trades.filter {
                        it.formattedDate == todayDdMmYy || it.formattedDate == todayIso
                    }
                    val dailyPnl = todayTrades.sumOf { it.pnl_usd }
                    val pnlColor = if (dailyPnl >= 0) Color(0xFF4CAF50) else Color(0xFFEF5350)
                    val pnlSign = if (dailyPnl >= 0) "+" else ""
                    val cryptoCount = todayTrades.count { it.isCrypto }
                    val forexCount = todayTrades.count { it.isForex }

                    Text(
                        "$pnlSign$${"%,.2f".format(dailyPnl)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${todayTrades.size} trades today  •  $cryptoCount crypto / $forexCount forex",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                },
                onError = { msg ->
                    Text(
                        "N/A",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.3f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        msg.take(40),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            )
        }
    }
}

// ── Today's date in DD/MM/YY format (matches IG forex date format) ──
private fun todayDateString(): String {
    val now = kotlinx.datetime.Clock.System.now()
    val instant = now
    val dateTime = instant.toString().substringBefore("T")
    val parts = dateTime.split("-")
    return "${parts[2]}/${parts[1]}/${parts[0].takeLast(2)}"
}

// ── Today's date in ISO YYYY-MM-DD format (matches Alpaca crypto date format) ──
private fun todayIsoDateString(): String {
    val now = kotlinx.datetime.Clock.System.now()
    return now.toString().substringBefore("T")
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
