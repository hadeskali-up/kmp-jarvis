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
import com.jarvis.app.models.ForexPosition
import com.jarvis.app.models.ForexPositionsResponse
import com.jarvis.app.services.ForexService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForexScreen(onBack: () -> Unit) {
    val service: ForexService = koinInject()
    var state by remember { mutableStateOf<UiState<ForexPositionsResponse>>(UiState.Loading) }
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
            delay(30_000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forex Positions", fontWeight = FontWeight.Bold) },
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
                ForexContent(
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
private fun ForexContent(
    data: ForexPositionsResponse,
    autoRefresh: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // ── Summary header ──
        val totalPnl = data.total_pnl
        val winners = data.positions.count { it.isProfitable }
        val losers = data.positions.count { !it.isProfitable }

        ForexSummaryBanner(
            totalPnl = totalPnl,
            openCount = data.count,
            winners = winners,
            losers = losers,
            autoRefresh = autoRefresh
        )

        if (data.positions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CurrencyExchange,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No open positions",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Forex market may be closed (weekend)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(data.positions) { pos ->
                    ForexPositionCard(pos)
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "TP = +30 pips  |  SL = -20 pips  |  IG native SL/TP",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ForexSummaryBanner(
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
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Forex PnL (IG Demo)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "$pnlSign$${"%,.2f".format(totalPnl)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = pnlColor
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ForexStatChip("Open", "$openCount", MaterialTheme.colorScheme.onSecondaryContainer)
                ForexStatChip("Winners", "$winners", Color(0xFF4CAF50))
                ForexStatChip("Losers", "$losers", Color(0xFFEF5350))
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
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                    )
                    Text(
                        "Auto-refresh 30s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ForexStatChip(label: String, value: String, color: Color) {
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
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun ForexPositionCard(pos: ForexPosition) {
    val pnlColor = if (pos.isProfitable) Color(0xFF4CAF50) else Color(0xFFEF5350)
    val pnlSign = if (pos.pnl_usd >= 0) "+" else ""
    val dirColor = if (pos.isLong) Color(0xFF4CAF50) else Color(0xFFEF5350)
    val dirLabel = if (pos.isLong) "LONG" else "SHORT"

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
            // ── Pair + Direction + PnL ──
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
                        Icons.Default.CurrencyExchange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        pos.pair,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = dirColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            dirLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = dirColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
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
                ForexPriceCell("Entry", "${"%,.5f".format(pos.level)}", Modifier.weight(1f))
                ForexPriceCell("Bid", "${"%,.5f".format(pos.bid)}", Modifier.weight(1f))
                ForexPriceCell("Offer", "${"%,.5f".format(pos.offer)}", Modifier.weight(1f))
            }

            // ── Size + Change ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ForexPriceCell("Size", "${"%,.2f".format(pos.size)}", Modifier.weight(1f))
                ForexPriceCell("Change", "${"%,.2f".format(pos.percentage_change)}%", Modifier.weight(1f))
            }

            // ── SL / TP levels ──
            if (pos.stop > 0 || pos.limit > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (pos.stop > 0) {
                        ForexPriceCell("SL", "${"%,.5f".format(pos.stop)}", Modifier.weight(1f), Color(0xFFEF5350))
                    }
                    if (pos.limit > 0) {
                        ForexPriceCell("TP", "${"%,.5f".format(pos.limit)}", Modifier.weight(1f), Color(0xFF4CAF50))
                    }
                }
            }
        }
    }
}

@Composable
private fun ForexPriceCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
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
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
