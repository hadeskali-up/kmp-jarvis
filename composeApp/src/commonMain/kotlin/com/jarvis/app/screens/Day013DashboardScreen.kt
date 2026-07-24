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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.app.models.*
import com.jarvis.app.services.DashboardService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onBack: () -> Unit) {
    var snapshot by remember { mutableStateOf<SnapshotResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val service = remember { DashboardService() }

    fun loadData() {
        scope.launch {
            isLoading = true
            error = null
            service.fetchSnapshot().fold(
                onSuccess = { snapshot = it },
                onFailure = { error = it.message }
            )
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadData() }

    DisposableEffect(Unit) {
        onDispose { service.cleanup() }
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
                    IconButton(onClick = { loadData() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))
                        Text("Loading dashboard...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
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
                        Text("Connection failed", fontWeight = FontWeight.Bold)
                        Text(
                            error ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { loadData() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {
                val data = snapshot
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── DeepSeek Credits ──
                    data?.deepseek?.let { ds ->
                        SectionHeader("DeepSeek Credits", Icons.Default.CurrencyExchange)
                        CreditCard(ds)
                    }

                    // ── VPS Resources ──
                    data?.vps?.let { vps ->
                        SectionHeader("VPS Resources", Icons.Default.Memory)
                        VpsCard(vps)
                    }

                    // ── Gateway Status ──
                    data?.gateway?.let { gw ->
                        SectionHeader("Gateway", Icons.Default.Router)
                        GatewayCard(gw)
                    }

                    // ── Agent Stats ──
                    data?.agents?.let { agents ->
                        SectionHeader("Agents", Icons.Default.People)
                        AgentStatsGrid(agents)
                    }

                    // ── Overall Stats ──
                    data?.stats?.let { st ->
                        SectionHeader("Task Stats", Icons.Default.Assessment)
                        StatsRow(st)
                    }

                    // ── Page Hits ──
                    data?.page_hits?.let { ph ->
                        SectionHeader("Page Hits", Icons.Default.Visibility)
                        PageHitsCard(ph)
                    }

                    // ── Recent Activity ──
                    data?.activity?.let { acts ->
                        SectionHeader("Recent Activity", Icons.Default.Timeline)
                        ActivityFeed(acts.take(10))
                    }

                    // ── Activity by Day ──
                    data?.activity_by_day?.let { days ->
                        SectionHeader("Activity by Day", Icons.Default.BarChart)
                        DayActivityBars(days)
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

// ── Section Header ──
@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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

// ── VPS Card ──
@Composable
private fun VpsCard(vps: VpsData) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ResourceBar("CPU", vps.cpu_pct, 100.0, "%")
            ResourceBar("RAM", vps.mem_pct, 100.0, "${"%.0f".format(vps.mem_used_mb)} / ${"%.0f".format(vps.mem_total_mb)} MB")
            ResourceBar("Disk", vps.disk_pct, 100.0, "${"%.1f".format(vps.disk_used_gb)} / ${"%.1f".format(vps.disk_total_gb)} GB")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Uptime", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatUptime(vps.uptime_s), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
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
