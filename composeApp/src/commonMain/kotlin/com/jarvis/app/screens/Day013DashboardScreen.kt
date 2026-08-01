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
import com.jarvis.app.services.ProviderBalanceService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onBack: () -> Unit) {
    val viewModel: DashboardViewModel = koinInject()
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val providerBalanceService: ProviderBalanceService = koinInject()
    var providerBalancesState by remember { mutableStateOf<UiState<ProviderBalancesResponse>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    fun loadProviderBalances() {
        scope.launch {
            providerBalanceService.fetchBalances().fold(
                onSuccess = { providerBalancesState = UiState.Success(it) },
                onFailure = { providerBalancesState = UiState.Error(it.message ?: "Failed to fetch provider balances") }
            )
        }
    }

    LaunchedEffect(Unit) {
        loadProviderBalances()
    }

    // Auto-refresh provider balances every 60s
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000)
            loadProviderBalances()
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
                        loadProviderBalances()
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

                // ── Provider Balances ──
                SectionHeader("Provider Balances", Icons.Default.CurrencyExchange)
                ProviderBalancesSection(providerBalancesState)

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

// ── Provider Balances Section ──
@Composable
private fun ProviderBalancesSection(state: UiState<ProviderBalancesResponse>) {
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                data.providers.forEach { provider ->
                    ProviderBalanceCard(provider)
                }
                if (data.providers.isEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "No provider balance data yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
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

private fun formatBalance(value: Double, unit: String): String = when (unit) {
    "USD" -> "$${"%,.2f".format(value)}"
    "tokens", "credits" -> {
        val millions = value / 1_000_000.0
        if (millions >= 1.0) "${"%,.1f".format(millions)}M $unit" else "${"%,.0f".format(value)} $unit"
    }
    else -> "${"%,.2f".format(value)} $unit"
}

@Composable
private fun ProviderBalanceCard(provider: ProviderBalanceItem) {
    val usagePct = provider.usage_percent
    val barColor = when {
        usagePct == null -> MaterialTheme.colorScheme.primary
        usagePct > 80 -> MaterialTheme.colorScheme.error
        usagePct > 60 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    val statusColor = if (provider.status == "active") {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Circle,
                        null,
                        tint = statusColor,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        provider.provider.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    formatBalance(provider.balance, provider.balance_unit),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            if (usagePct != null) {
                Column {
                    LinearProgressIndicator(
                        progress = { (usagePct / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = barColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "${"%.1f".format(usagePct)}% used",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        provider.requests?.let {
                            Text(
                                "$it reqs",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (provider.requests != null) {
                Text(
                    "${provider.requests} requests",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Helpers ──
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
