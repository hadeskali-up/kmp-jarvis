package com.jarvis.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jarvis.app.core.ScreenState
import com.jarvis.app.core.UiState
import com.jarvis.app.core.fold
import com.jarvis.app.models.*
import com.jarvis.app.services.CryptoService
import com.jarvis.app.services.TradeHistoryService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onBack: () -> Unit) {
    val viewModel: DashboardViewModel = koinInject()
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val cryptoService: CryptoService = koinInject()
    var cryptoState by remember { mutableStateOf<UiState<CryptoPositionsResponse>>(UiState.Loading) }
    val tradeHistoryService: TradeHistoryService = koinInject()
    var tradeHistoryState by remember { mutableStateOf<UiState<TradeHistoryResponse>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    // Fetch crypto positions alongside dashboard data
    fun loadCrypto() {
        scope.launch {
            cryptoService.fetchPositions().fold(
                onSuccess = { cryptoState = UiState.Success(it) },
                onFailure = { cryptoState = UiState.Error(it.message ?: "Failed to fetch crypto") }
            )
        }
    }

    fun loadTradeHistory() {
        scope.launch {
            tradeHistoryService.fetchTradeHistory(50).fold(
                onSuccess = { tradeHistoryState = UiState.Success(it) },
                onFailure = { tradeHistoryState = UiState.Error(it.message ?: "Failed to fetch trades") }
            )
        }
    }

    LaunchedEffect(Unit) {
        loadCrypto()
        loadTradeHistory()
    }

    // Auto-refresh trade history every 60s to reflect today's trades
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000)
            loadTradeHistory()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.loadData()
                        loadCrypto()
                        loadTradeHistory()
                    }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        ScreenState(
            state = uiState,
            onRetry = { viewModel.loadData() },
            modifier = Modifier.padding(padding),
            loadingMessage = "Loading dashboard..."
        ) { data: SnapshotResponse ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Infrastructure overview ──
                data.vps?.let { vps ->
                    SectionHeader("VPS Health", Icons.Default.Dns)
                    VpsOverviewCard(vps)
                }

                // ── DeepSeek Credits ──
                data.deepseek?.let { ds ->
                    SectionHeader("DeepSeek Credits", Icons.Default.CurrencyExchange)
                    CreditCard(ds)
                }

                // ── Crypto Holdings ──
                CryptoHoldingsSection(cryptoState)

                // ── Trade History (consolidated crypto + forex) ──
                TradeHistorySection(tradeHistoryState)


                // ── Gateway Status ──
                data.gateway?.let { gw ->
                    SectionHeader("Gateway", Icons.Default.Router)
                    GatewayCard(gw)
                }

                // ── Agent Stats ──
                data.agents?.let { agents ->
                    SectionHeader("Agents", Icons.Default.People)
                    AgentStatsGrid(agents)
                }

                // ── Overall Stats ──
                data.stats?.let { st ->
                    SectionHeader("Task Stats", Icons.Default.Assessment)
                    StatsRow(st)
                }

                // ── Page Hits ──
                data.page_hits?.let { ph ->
                    SectionHeader("Page Hits", Icons.Default.Visibility)
                    PageHitsCard(ph)
                }

                // ── Recent Activity ──
                data.activity?.let { acts ->
                    SectionHeader("Recent Activity", Icons.Default.Timeline)
                    ActivityFeed(acts.take(10))
                }

                // ── Activity by Day ──
                data.activity_by_day?.let { days ->
                    SectionHeader("Activity by Day", Icons.Default.BarChart)
                    DayActivityBars(days)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ── Section Header ──
@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, letterSpacing = 0.1.sp)
    }
}

// ── DeepSeek Credit Card ──
@Composable
private fun CreditCard(ds: DeepSeekData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Balance", ds.display.ifEmpty { "${ds.total_balance} ${ds.currency}" })
            VerticalDivider(Modifier.height(40.dp))
            StatItem("Used Today", "$${ds.today_used}")
        }
    }
}

// ── VPS Health Card ──
@Composable
private fun VpsOverviewCard(vps: VpsData) {
    val overallColor = when {
        vps.cpu_pct > 80 || vps.mem_pct > 80 || vps.disk_pct > 85 -> MaterialTheme.colorScheme.error
        vps.cpu_pct > 60 || vps.mem_pct > 60 || vps.disk_pct > 70 -> MaterialTheme.colorScheme.tertiary
        else -> Color(0xFF218739)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Dns, null, tint = MaterialTheme.colorScheme.primary) }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Production VPS", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Live infrastructure", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(
                    Modifier.clip(RoundedCornerShape(20.dp)).background(overallColor.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(7.dp).clip(RoundedCornerShape(4.dp)).background(overallColor))
                    Spacer(Modifier.width(6.dp))
                    Text("Healthy", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = overallColor)
                }
            }

            ResourceBar("CPU", vps.cpu_pct, 100.0, "Load ${"%.2f".format(vps.cpu_load)}")
            ResourceBar("Memory", vps.mem_pct, 100.0, "${"%.0f".format(vps.mem_used_mb)} / ${"%.0f".format(vps.mem_total_mb)} MB")
            ResourceBar("Storage", vps.disk_pct, 100.0, "${"%.1f".format(vps.disk_used_gb)} / ${"%.1f".format(vps.disk_total_gb)} GB")

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Uptime", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatUptime(vps.uptime_s), fontWeight = FontWeight.SemiBold)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Database", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%.1f".format(vps.db_size_mb)} MB", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ResourceBar(label: String, value: Double, max: Double, detail: String) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Text(
                "${"%.1f".format(value)}%  $detail",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (value / max).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = when {
                value > 80 -> MaterialTheme.colorScheme.error
                value > 60 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

// ── Gateway Card ──
@Composable
private fun GatewayCard(gw: GatewayData) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Circle,
                    null,
                    tint = if (gw.status == "connected") {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(gw.status.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Medium)
            }
            Text(formatUptime(gw.uptime_seconds), style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ── Agent Stats Grid ──
@Composable
private fun AgentStatsGrid(agents: Map<String, AgentStat>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        agents.entries.sortedBy { it.key }.forEach { (name, stat) ->
            AgentRow(name, stat)
        }
    }
}

@Composable
private fun AgentRow(name: String, stat: AgentStat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(name.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (stat.last_seen.isNotEmpty()) {
                    Text(
                        "Last: ${stat.last_seen.take(16).replace("T", " ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AgentStatBadge("${stat.total}", "total", MaterialTheme.colorScheme.outline)
                AgentStatBadge("${stat.completed}", "done", MaterialTheme.colorScheme.primary)
                if (stat.failed > 0) {
                    AgentStatBadge("${stat.failed}", "fail", MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun AgentStatBadge(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Stats Row ──
@Composable
private fun StatsRow(st: StatsData) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MiniStatCard("Total", st.total, Modifier.weight(1f))
        MiniStatCard("Done", st.completed, Modifier.weight(1f))
        MiniStatCard("Failed", st.failed, Modifier.weight(1f))
    }
}

@Composable
private fun MiniStatCard(label: String, value: Int, modifier: Modifier) {
    Card(modifier = modifier) {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$value", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Page Hits Card ──
@Composable
private fun PageHitsCard(ph: PageHitsData) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MiniStatCard("Total", ph.total, Modifier.weight(1f))
        MiniStatCard("Today", ph.today, Modifier.weight(1f))
    }
}

// ── Activity Feed ──
@Composable
private fun ActivityFeed(items: List<ActivityItem>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items.forEach { item ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.FiberManualRecord,
                        null,
                        modifier = Modifier.size(6.dp).padding(top = 6.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            item.task_description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row {
                            Text(
                                item.agent_name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                item.created_at.take(10),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Day Activity Bars ──
@Composable
private fun DayActivityBars(days: List<DayActivity>) {
    val maxT = days.maxOfOrNull { it.t }?.coerceAtLeast(1) ?: 1
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            days.takeLast(14).forEach { day ->
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        day.day.takeLast(5),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(48.dp)
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = (day.t.toFloat() / maxT).coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${day.t}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(24.dp)
                    )
                }
            }
        }
    }
}

// ── Crypto Holdings Section ──
@Composable
private fun CryptoHoldingsSection(state: UiState<CryptoPositionsResponse>) {
    SectionHeader("Crypto Holdings", Icons.Default.CurrencyBitcoin)

    state.fold(
        onLoading = {
            Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        onSuccess = { data ->
            val totalValue = data.positions.sumOf { it.market_value }
            val totalPnl = data.positions.sumOf { it.pnl_usd }
            val totalCost = totalValue - totalPnl
            val pnlPct = if (totalCost > 0) (totalPnl / totalCost) * 100 else 0.0
            val pnlColor = if (totalPnl >= 0) Color(0xFF4CAF50) else Color(0xFFEF5350)
            val pnlSign = if (totalPnl >= 0) "+" else ""
            val winners = data.positions.count { it.isProfitable }
            val losers = data.positions.count { !it.isProfitable }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Summary row
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Total Value",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "$${"%,.2f".format(totalValue)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Total PnL",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "$pnlSign$${"%,.2f".format(totalPnl)} ($pnlSign${"%.1f".format(pnlPct)}%)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = pnlColor
                            )
                        }
                    }

                    // W/L + open count badge
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CryptoMiniStat("Open", "${data.count}", Modifier.weight(1f))
                        CryptoMiniStat("Winners", "$winners", Modifier.weight(1f), Color(0xFF4CAF50))
                        CryptoMiniStat("Losers", "$losers", Modifier.weight(1f), Color(0xFFEF5350))
                    }

                    if (data.positions.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // Position list (max 5)
                        data.positions.take(5).forEach { pos ->
                            CryptoPositionRow(pos)
                        }

                        if (data.positions.size > 5) {
                            Text(
                                "+${data.positions.size - 5} more...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        onError = { msg ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Failed to load", fontWeight = FontWeight.Bold)
                    Text(
                        msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

@Composable
private fun CryptoMiniStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CryptoPositionRow(pos: CryptoPosition) {
    val pnlColor = if (pos.isProfitable) Color(0xFF4CAF50) else Color(0xFFEF5350)
    val pnlSign = if (pos.pnl_usd >= 0) "+" else ""

    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
                modifier = Modifier.size(16.dp)
            )
            Text(
                pos.symbol,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$${"%,.2f".format(pos.market_value)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "$pnlSign$${"%,.2f".format(pos.pnl_usd)}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = pnlColor
            )
            Text(
                "$pnlSign${"%.1f".format(pos.pnl_pct)}%",
                style = MaterialTheme.typography.labelSmall,
                color = pnlColor
            )
        }
    }
}

// ── Trade History Section ──
@Composable
private fun TradeHistorySection(state: UiState<TradeHistoryResponse>) {
    SectionHeader("Trade History", Icons.Default.History)

    state.fold(
        onLoading = {
            Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        onSuccess = { data ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Summary row
                    val winners = data.trades.count { it.isProfitable }
                    val losers = data.trades.count { it.pnl_usd < 0 }
                    val totalPnl = data.total_pnl
                    val pnlColor = if (totalPnl >= 0) Color(0xFF4CAF50) else Color(0xFFEF5350)
                    val pnlSign = if (totalPnl >= 0) "+" else ""

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Total PnL",
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
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CryptoMiniStat("Trades", "${data.count}", Modifier.weight(1f))
                            CryptoMiniStat("W", "$winners", Modifier.weight(1f), Color(0xFF4CAF50))
                            CryptoMiniStat("L", "$losers", Modifier.weight(1f), Color(0xFFEF5350))
                        }
                    }

                    if (data.trades.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // Last 20 trades
                        data.trades.take(20).forEach { trade ->
                            TradeHistoryRow(trade)
                        }

                        if (data.trades.size > 20) {
                            Text(
                                "+${data.trades.size - 20} more...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Text(
                            "No trades yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        onError = { msg ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Failed to load", fontWeight = FontWeight.Bold)
                    Text(
                        msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

@Composable
private fun TradeHistoryRow(trade: TradeRecord) {
    val pnlColor = if (trade.isProfitable) Color(0xFF4CAF50) else Color(0xFFEF5350)
    val pnlSign = if (trade.pnl_usd >= 0) "+" else ""
    val sourceColor = if (trade.isForex) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
    val sourceIcon = if (trade.isForex) Icons.Default.CurrencyExchange else Icons.Default.CurrencyBitcoin
    val sourceLabel = if (trade.isForex) "FX" else "CR"
    val statusColor = if (trade.isOpen) Color(0xFF2196F3) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val dateLabel = if (trade.isOpen) "live" else trade.formattedDate

    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                sourceIcon,
                contentDescription = null,
                tint = sourceColor,
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text(
                    trade.symbol,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    "${trade.side} • $dateLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "$pnlSign$${"%,.2f".format(trade.pnl_usd)}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (trade.pnl_usd != 0.0) pnlColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                trade.status,
                style = MaterialTheme.typography.labelSmall,
                color = statusColor
            )
        }
    }
}

// ── Helpers ──
@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
    }
}

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .width(1.dp)
            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
    )
}

private fun formatUptime(seconds: Long): String {
    val days = seconds / 86400
    val hours = (seconds % 86400) / 3600
    val mins = (seconds % 3600) / 60
    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${mins}m"
        else -> "${mins}m"
    }
}
