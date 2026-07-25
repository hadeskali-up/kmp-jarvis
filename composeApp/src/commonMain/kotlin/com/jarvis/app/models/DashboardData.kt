package com.jarvis.app.models

import kotlinx.serialization.Serializable

@Serializable
data class SnapshotResponse(
    val vps: VpsData? = null,
    val agents: Map<String, AgentStat>? = null,
    val activity: List<ActivityItem>? = null,
    val activity_by_day: List<DayActivity>? = null,
    val sessions: SessionsData? = null,
    val gateway: GatewayData? = null,
    val stats: StatsData? = null,
    val deepseek: DeepSeekData? = null,
    val rootsys: RootsysUsageData? = null,
    val page_hits: PageHitsData? = null
)

@Serializable
data class VpsData(
    val cpu_pct: Double = 0.0,
    val cpu_load: Double = 0.0,
    val mem_pct: Double = 0.0,
    val mem_used_mb: Double = 0.0,
    val mem_total_mb: Double = 0.0,
    val disk_pct: Double = 0.0,
    val disk_used_gb: Double = 0.0,
    val disk_total_gb: Double = 0.0,
    val uptime_s: Long = 0,
    val db_size_mb: Double = 0.0
)

@Serializable
data class AgentStat(
    val total: Int = 0,
    val completed: Int = 0,
    val failed: Int = 0,
    val last_seen: String = ""
)

@Serializable
data class ActivityItem(
    val id: String? = null,
    val agent_name: String = "",
    val task_description: String = "",
    val status: String = "",
    val model_used: String = "",
    val created_at: String = ""
)

@Serializable
data class DayActivity(
    val day: String = "",
    val t: Int = 0
)

@Serializable
data class SessionsData(
    val count: Int = 0,
    val totals: SessionTotals? = null
)

@Serializable
data class SessionTotals(
    val messages: Int = 0,
    val input_tokens: Int = 0,
    val cache_read_tokens: Int = 0
)

@Serializable
data class GatewayData(
    val status: String = "",
    val uptime_seconds: Long = 0
)

@Serializable
data class StatsData(
    val total: Int = 0,
    val completed: Int = 0,
    val failed: Int = 0
)

@Serializable
data class DeepSeekData(
    val configured: Boolean = false,
    val is_available: Boolean = false,
    val total_balance: String = "",
    val currency: String = "USD",
    val topped_up_balance: String = "",
    val granted_balance: String = "",
    val display: String = "",
    val today_used: String = ""
)

@Serializable
data class PageHitsData(
    val total: Int = 0,
    val today: Int = 0,
    val last_date: String = "",
    val daily: Map<String, Int> = emptyMap()
)

@Serializable
data class RootsysUsageData(
    val configured: Boolean = false,
    val sessions_total: Int = 0,
    val input_tokens: Long = 0,
    val output_tokens: Long = 0,
    val cache_read_tokens: Long = 0,
    val reasoning_tokens: Long = 0,
    val total_tokens: Long = 0,
    val today_input: Long = 0,
    val today_output: Long = 0,
    val today_sessions: Int = 0,
    val today_total: Long = 0,
    val daily: Map<String, Long> = emptyMap()
)
