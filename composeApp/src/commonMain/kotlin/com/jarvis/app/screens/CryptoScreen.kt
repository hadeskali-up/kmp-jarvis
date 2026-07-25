package com.jarvis.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.app.core.UiState
import com.jarvis.app.core.fold
import com.jarvis.app.models.CryptoPosition
import com.jarvis.app.models.CryptoPositionsResponse
import com.jarvis.app.services.CryptoService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoScreen(onBack: () -> Unit) {
    val service: CryptoService = koinInject()
    var state by remember { mutableStateOf<UiState<CryptoPositionsResponse>>(UiState.Loading) }
    var autoRefresh by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun loadData() {
        state = UiState.Loading
        scope.launch {
            service.fetchPositions().fold(
                onSuccess = { state = UiState.Success(it) },
                onFailure = { state = UiState.Error(it.message ?: "Failed to fetch positions") }
            )
        }
    }

    LaunchedEffect(Unit) {
        while (autoRefresh) {
            service.fetchPositions().fold(
                onSuccess = { state = UiState.Success(it) },
                onFailure = { state = UiState.Error(it.message ?: "Failed") }
            )
            delay(30_000) // refresh every 30s
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crypto Positions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { loadData() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                    IconButton(onClick = { autoRefresh = !autoRefresh }) {
                        Icon(
                            if (autoRefresh) Icons.Default.Sync else Icons.Default.SyncDisabled,
                            contentDescription = "Auto-refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        state.fold(
            onLoading = {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))
                        Text("Loading positions...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            onSuccess = { data ->
                CryptoContent(
                    data = data,
                    autoRefresh = autoRefresh,
                    modifier = Modifier.padding(padding)
                )
            },
            onError = { msg ->
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Connection Error", fontWeight = FontWeight.Bold)
                        Text(
                            msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { loadData() }) { Text("Retry") }
                    }
                }
            }
        )
    }
}

@Composable
private fun CryptoContent(
    data: CryptoPositionsResponse,
    autoRefresh: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // ── Summary header ──
        val totalPnl = data.positions.sumOf { it.pnl_usd }
        val totalValue = data.positions.sumOf { it.market_value }
        val winners = data.positions.count { it.isProfitable }
        val losers = data.positions.count { !it.isProfitable }

        SummaryBanner(
            totalValue = totalValue,
            totalPnl = totalPnl,
            openCount = data.count,
            winners = winners,
            losers = losers,
            autoRefresh = autoRefresh
        )

        // ── Position cards ──
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(data.positions) { pos ->
                PositionCard(pos)
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "TP = +20%  |  SL = -10%  |  Auto-monitor active",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SummaryBanner(
    totalValue: Double,
    totalPnl: Double,
    openCount: Int,
    winners: Int,
    losers: Int,
    autoRefresh: Boolean
) {
    val pnlColor = if (totalPnl >= 0) Color(0xFF4CAF50) else Color(0xFFEF5350)
    val pnlSign = if (totalPnl >= 0) "+" else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Total Portfolio Value",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "$${"%,.2f".format(totalValue)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip("PnL", "$pnlSign$${"%,.2f".format(totalPnl)}", pnlColor)
                StatChip("Open", "$openCount", MaterialTheme.colorScheme.onPrimaryContainer)
                StatChip("W/L", "$winners/$losers", MaterialTheme.colorScheme.onPrimaryContainer)
            }
            if (autoRefresh) {
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    )
                    Text(
                        "Auto-refresh 30s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun PositionCard(pos: CryptoPosition) {
    val pnlColor = if (pos.isProfitable) Color(0xFF4CAF50) else Color(0xFFEF5350)
    val pnlSign = if (pos.pnl_usd >= 0) "+" else ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Symbol + PnL ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CurrencyBitcoin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        pos.symbol,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$pnlSign$${"%,.2f".format(pos.pnl_usd)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor
                    )
                    Text(
                        "$pnlSign${"%,.2f".format(pos.pnl_pct)}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = pnlColor
                    )
                }
            }

            // ── Entry vs Current ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PriceCell("Entry", "$${"%,.4f".format(pos.entry)}", Modifier.weight(1f))
                PriceCell("Current", "$${"%,.4f".format(pos.current)}", Modifier.weight(1f))
                PriceCell("Qty", "${"%,.4f".format(pos.qty)}", Modifier.weight(1f))
            }

            // ── TP / SL levels ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TpSlCell(
                    label = "TP +20%",
                    value = "$${"%,.4f".format(pos.tp)}",
                    progress = pos.tp_progress,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                TpSlCell(
                    label = "SL -10%",
                    value = "$${"%,.4f".format(pos.sl)}",
                    progress = pos.sl_progress,
                    color = Color(0xFFEF5350),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PriceCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TpSlCell(
    label: String,
    value: String,
    progress: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${"%.1f".format(progress)}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        // Progress bar
        LinearProgressIndicator(
            progress = { (progress / 100).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
        )
    }
}
