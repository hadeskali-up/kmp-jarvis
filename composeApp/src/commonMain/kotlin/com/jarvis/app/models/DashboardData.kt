package com.jarvis.app.models

import kotlinx.serialization.Serializable

@Serializable
data class DashboardData(
    val activeTasks: Int = 0,
    val completedToday: Int = 0,
    val agentsOnline: Int = 0,
    val systemUptime: String = "",
    val recentActivity: List<String> = emptyList(),
    val alerts: List<String> = emptyList()
)
