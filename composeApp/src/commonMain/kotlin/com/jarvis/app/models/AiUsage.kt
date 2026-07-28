package com.jarvis.app.models

import kotlinx.serialization.Serializable

@Serializable
data class AiUsageData(
    val ok: Boolean = false,
    val balance: Double = 0.0,
    val budget: Double = 0.0,
    val spent: Double = 0.0,
    val used_pct: Double = 0.0,
    val currency: String = "USD",
    val usage_24h_spent: Double = 0.0,
    val recent_requests: Int = 0,
    val fetched_at: String = "",
    val error: String = ""
)
