package com.jarvis.app.models

import kotlinx.serialization.Serializable

@Serializable
data class TradeHistoryResponse(
    val trades: List<TradeRecord> = emptyList(),
    val count: Int = 0,
    val total_pnl: Double = 0.0
)

@Serializable
data class TradeRecord(
    val source: String = "",       // "crypto" or "forex"
    val symbol: String = "",
    val side: String = "",         // "BUY" or "SELL"
    val entry: Double = 0.0,
    val exit: Double = 0.0,
    val qty: Double = 0.0,
    val pnl_usd: Double = 0.0,
    val pnl_pct: Double = 0.0,
    val status: String = "",       // "filled", "rejected", "closed", "opened"
    val date: String = ""
) {
    val isProfitable: Boolean get() = pnl_usd > 0
    val pnlColor: String get() = when {
        pnl_usd > 0 -> "green"
        pnl_usd < 0 -> "red"
        else -> "gray"
    }
    val isForex: Boolean get() = source == "forex"
    val isCrypto: Boolean get() = source == "crypto"
    val formattedDate: String get() {
        // Handle both ISO format and DD/MM/YY format
        return if (date.contains("T")) {
            date.substringBefore("T")
        } else {
            date.substringBefore(" ")
        }
    }
}
