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
import com.jarvis.app.models.MT5Deal
import com.jarvis.app.models.MT5HistoryResponse
import com.jarvis.app.models.MT5Position
import com.jarvis.app.models.MT5PositionsResponse
import com.jarvis.app.models.MT5StatusResponse
import com.jarvis.app.services.ForexService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForexScreen(onBack: () -> Unit) {
    val service: ForexService = koinInject()
    var state by remember { mutableStateOf<UiState<MT5PositionsResponse>>(UiState.Loading) }
    var historyState by remember { mutableStateOf<UiState<MT5HistoryResponse>>(UiState.Loading) }
    var statusState by remember { mutableStateOf<UiState<MT5StatusResponse>>(UiState.Loading) }
    var autoRefresh by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun loadData() {
        state = UiState.Loading
        historyState = UiState.Loading
        statusState = UiState.Loading
        scope.launch {
            service.fetchMT5Positions().fold(
                onSuccess = { state = UiState.Success(it) },
                onFailure = { state = UiState.Error(it.message ?: "No MT5 data yet") }
            )
            service.fetchMT5History(50).fold(
                onSuccess = { historyState = UiState.Success(it) },
                onFailure = { historyState = UiState.Error(it.message ?: "No history yet") }
            )
            service.fetchMT5Status().fold(
                onSuccess = { statusState = UiState.Success(it) },
                onFailure = { statusState = UiState.Error(it.message ?: "Status unavailable") }
            )
        }
    }

    LaunchedEffect(Unit) {
        while (autoRefresh) {
            service.fetchMT5Positions().fold(
                onSuccess = { state = UiState.Success(it) },
                onFailure = { state = UiState.Error(it.message ?: "No MT5 data") }
            )
            service.fetchMT5History(50).fold(
                onSuccess = { historyState = UiState.Success(it) },
                onFailure = { historyState = UiState.Error(it.message ?: "No history") }
            )
            service.fetchMT5Status().fold(
                onSuccess = { statusState = UiState.Success(it) },
                onFailure = { statusState = UiState.Error(it.message ?: "Status error") }
            )
            delay(30_000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MT5 Positions", fontWeight = FontWeight.Bold) },
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
                        Text("Loading MT5 data...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            onSuccess = { data ->
                ForexContent(
                    data = data,
                    historyState = historyState,
                    statusState = statusState,
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
                        Text("No MT5 Data", fontWeight = FontWeight.Bold)
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
    data: MT5PositionsResponse,
    historyState: UiState<MT5HistoryResponse>,
    statusState: UiState<MT5StatusResponse>,
    autoRefresh: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // ── Account banner (TOP — summary first) ──
        MT5AccountBanner(
            account = data.account,
            openCount = data.count,
            totalPnl = data.total_pnl,
            statusState = statusState,
            autoRefresh = autoRefresh
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Open Positions ──
            if (data.positions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CurrencyExchange,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No open positions", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "MT5 demo account is empty",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(data.positions) { pos -> MT5PositionCard(pos) }
            }

            // ─── Deal History Section ───
            item { MT5HistorySection(historyState) }

            item {
                Text(
                    "MT5 Demo  |  FXTM  |  Leverage 1:${data.account.leverage}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ── MT5 Account Banner ──
@Composable
private fun MT5AccountBanner(
    account: com.jarvis.app.models.MT5Account,
    openCount: Int,
    totalPnl: Double,
    statusState: UiState<MT5StatusResponse>,
    autoRefresh: Boolean
) {
    val pnlColor = if (totalPnl >= 0) Color(0xFF4CAF50) else Color(0xFFEF5350)
    val pnlSign = if (totalPnl >= 0) "+" else ""
    val isFresh = statusState is UiState.Success && statusState.data.fresh

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
            // ── Server + Connection status ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    account.server.ifEmpty { "MT5 Demo" },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (isFresh) Color(0xFF4CAF50) else Color(0xFFEF5350))
                    )
                    Text(
                        if (isFresh) "Live" else "Stale",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Account holder name ──
            if (account.name.isNotEmpty()) {
                Text(
                    account.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }

            // ── Balance (BIG) ──
            Text(
                "$${"%,.2f".format(account.balance)}",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                "${account.currency}  •  Login: ${account.login}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(12.dp))

            // ── Equity / Margin / PnL row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MT5StatChip("Equity", "$${"%,.2f".format(account.equity)}", MaterialTheme.colorScheme.onSecondaryContainer)
                MT5StatChip("Margin", "$${"%,.2f".format(account.margin)}", MaterialTheme.colorScheme.onSecondaryContainer)
                MT5StatChip("Open PnL", "$pnlSign${"%,.2f".format(totalPnl)}", pnlColor)
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MT5StatChip("Positions", "$openCount", MaterialTheme.colorScheme.onSecondaryContainer)
                MT5StatChip("Free Margin", "$${"%,.2f".format(account.margin_free)}", MaterialTheme.colorScheme.onSecondaryContainer)
                MT5StatChip("Leverage", "1:${account.leverage}", MaterialTheme.colorScheme.onSecondaryContainer)
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
private fun MT5StatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontSize = 15.sp,
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

// ── MT5 Position Card ──
@Composable
private fun MT5PositionCard(pos: MT5Position) {
    val pnlColor = if (pos.isProfitable) Color(0xFF4CAF50) else Color(0xFFEF5350)
    val pnlSign = if (pos.profit >= 0) "+" else ""
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
            // ── Symbol + Direction + PnL ──
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
                        pos.symbol,
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
                        "$pnlSign$${"%,.2f".format(pos.profit)}",
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
                MT5PriceCell("Entry", "${"%,.5f".format(pos.price_open)}", Modifier.weight(1f))
                MT5PriceCell("Current", "${"%,.5f".format(pos.price_current)}", Modifier.weight(1f))
                MT5PriceCell("Volume", "${"%,.2f".format(pos.volume)}", Modifier.weight(1f))
            }

            // ── SL / TP levels ──
            if (pos.sl > 0 || pos.tp > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (pos.sl > 0) {
                        MT5PriceCell("SL", "${"%,.5f".format(pos.sl)}", Modifier.weight(1f), Color(0xFFEF5350))
                    }
                    if (pos.tp > 0) {
                        MT5PriceCell("TP", "${"%,.5f".format(pos.tp)}", Modifier.weight(1f), Color(0xFF4CAF50))
                    }
                }
            }

            // ── TP/SL Progress bars ──
            if (pos.tp_progress > 0 || pos.sl_progress > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (pos.tp_progress > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "TP",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.width(24.dp)
                            )
                            LinearProgressIndicator(
                                progress = { (pos.tp_progress / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.weight(1f).height(6.dp),
                                color = Color(0xFF4CAF50),
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                            Text(
                                "${"%.0f".format(pos.tp_progress)}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.width(36.dp)
                            )
                        }
                    }
                    if (pos.sl_progress > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "SL",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFEF5350),
                                modifier = Modifier.width(24.dp)
                            )
                            LinearProgressIndicator(
                                progress = { (pos.sl_progress / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.weight(1f).height(6.dp),
                                color = Color(0xFFEF5350),
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                            Text(
                                "${"%.0f".format(pos.sl_progress)}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFEF5350),
                                modifier = Modifier.width(36.dp)
                            )
                        }
                    }
                }
            }

            // ── Comment + Time ──
            if (pos.comment.isNotEmpty() || pos.time.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (pos.comment.isNotEmpty()) {
                        Text(
                            "📝 ${pos.comment}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (pos.time.isNotEmpty()) {
                        Text(
                            pos.time.replace("T", " ").take(19),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ── MT5 History Section ──
@Composable
private fun MT5HistorySection(state: UiState<MT5HistoryResponse>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Deal History (30 days)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        state.fold(
            onLoading = { LinearProgressIndicator(Modifier.fillMaxWidth()) },
            onSuccess = { data ->
                if (data.deals.isEmpty()) {
                    Text(
                        "No closed deals yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    // Summary row
                    val closedDeals = data.deals.filter { !it.isBalance }
                    val winners = closedDeals.count { it.profit > 0 }
                    val losers = closedDeals.count { it.profit < 0 }
                    val totalPnl = data.total_pnl
                    val pnlColor = if (totalPnl >= 0) Color(0xFF4CAF50) else Color(0xFFEF5350)
                    val pnlSign = if (totalPnl >= 0) "+" else ""

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Total Realized PnL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "$pnlSign$${"%,.2f".format(totalPnl)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = pnlColor
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                MT5MiniStat("Deals", "${data.count}")
                                MT5MiniStat("W", "$winners", Color(0xFF4CAF50))
                                MT5MiniStat("L", "$losers", Color(0xFFEF5350))
                            }
                        }
                    }

                    // ── Scrollable deal list (up to 100 deals) ──
                    val displayDeals = data.deals.take(100)
                    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 480.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        androidx.compose.foundation.lazy.LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(displayDeals) { deal -> MT5DealRow(deal) }
                            if (data.deals.size > 100) {
                                item {
                                    Text(
                                        "+${data.deals.size - 100} more deals not shown",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            },
            onError = { message ->
                Text(
                    message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        )
    }
}

@Composable
private fun MT5MiniStat(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MT5DealRow(deal: MT5Deal) {
    // ── Color coding: TP (profit) = green, SL (loss) = red, Balance = gray ──
    val isWin = deal.profit > 0
    val isLoss = deal.profit < 0
    val isNeutral = deal.profit == 0.0 || deal.isBalance

    val pnlColor = when {
        deal.isBalance -> MaterialTheme.colorScheme.onSurfaceVariant
        isWin -> Color(0xFF4CAF50)
        isLoss -> Color(0xFFEF5350)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val pnlSign = if (deal.profit >= 0) "+" else ""

    // ── Status badge: TP / SL / BAL / OUT ──
    val statusLabel = when {
        deal.isBalance -> "BAL"
        isWin -> "TP"
        isLoss -> "SL"
        else -> "OUT"
    }
    val statusColor = when {
        deal.isBalance -> MaterialTheme.colorScheme.onSurfaceVariant
        isWin -> Color(0xFF4CAF50)
        isLoss -> Color(0xFFEF5350)
        else -> MaterialTheme.colorScheme.primary
    }

    val sourceIcon = if (deal.isBalance) Icons.Default.AccountBalanceWallet else Icons.Default.CurrencyExchange

    // ── Left accent bar color ──
    val accentColor = when {
        deal.isBalance -> MaterialTheme.colorScheme.outlineVariant
        isWin -> Color(0xFF4CAF50)
        isLoss -> Color(0xFFEF5350)
        else -> MaterialTheme.colorScheme.outline
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Color accent bar ──
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor)
        )

        // ── Icon ──
        Icon(
            sourceIcon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
        )

        // ── Symbol + time ──
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (deal.isBalance) "Balance Operation" else deal.symbol,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                "${deal.type} • ${"%,.2f".format(deal.volume)} lots • ${deal.formattedTime}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ── Status badge ──
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = statusColor.copy(alpha = 0.15f)
        ) {
            Text(
                statusLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // ── PnL ──
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "$pnlSign$${"%,.2f".format(deal.profit)}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = pnlColor
            )
            if (deal.comment.isNotEmpty() && !deal.isBalance) {
                Text(
                    deal.comment.take(15),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MT5PriceCell(
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
