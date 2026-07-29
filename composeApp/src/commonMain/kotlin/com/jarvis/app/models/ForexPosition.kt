package com.jarvis.app.models

import kotlinx.serialization.Serializable

// ─── IG Forex (legacy, still used by some endpoints) ──

@Serializable
data class ForexPositionsResponse(
    val positions: List<ForexPosition> = emptyList(),
    val count: Int = 0,
    val total_pnl: Double = 0.0
)

@Serializable
data class ForexPosition(
    val pair: String = "",
    val epic: String = "",
    val direction: String = "",
    val size: Double = 0.0,
    val level: Double = 0.0,
    val bid: Double = 0.0,
    val offer: Double = 0.0,
    val pnl_usd: Double = 0.0,
    val pnl_pct: Double = 0.0,
    val percentage_change: Double = 0.0,
    val stop: Double = 0.0,
    val limit: Double = 0.0
) {
    val isProfitable: Boolean get() = pnl_usd >= 0
    val pnlColor: String get() = if (isProfitable) "green" else "red"
    val isLong: Boolean get() = direction.equals("BUY", ignoreCase = true)
}

@Serializable
data class ForexAlertsResponse(val alerts: List<ForexAlert> = emptyList(), val count: Int = 0)

@Serializable
data class ForexAlert(
    val event: String = "",
    val title: String = "",
    val message: String = "",
    val pair: String = "",
    val deal_id: String = "",
    val timestamp: String = ""
)

// ─── MT5 (Windows PC push) ──

@Serializable
data class MT5PositionsResponse(
    val positions: List<MT5Position> = emptyList(),
    val count: Int = 0,
    val total_pnl: Double = 0.0,
    val last_updated: String = "",
    val account: MT5Account = MT5Account()
)

@Serializable
data class MT5Account(
    val login: Long = 0,
    val balance: Double = 0.0,
    val equity: Double = 0.0,
    val margin: Double = 0.0,
    val margin_free: Double = 0.0,
    val margin_level: Double? = null,  // Nullable when no open positions
    val profit: Double = 0.0,
    val currency: String = "USD",
    val server: String = "",
    val leverage: Int = 0,
    val name: String = "",
    val company: String = ""
)

@Serializable
data class MT5Position(
    val ticket: Long = 0,
    val symbol: String = "",
    val type: String = "",        // "BUY" or "SELL"
    val type_raw: Int = 0,
    val volume: Double = 0.0,
    val price_open: Double = 0.0,
    val price_current: Double = 0.0,
    val sl: Double = 0.0,
    val tp: Double = 0.0,
    val profit: Double = 0.0,
    val swap: Double = 0.0,
    val pnl_pct: Double = 0.0,
    val tp_progress: Double = 0.0,
    val sl_progress: Double = 0.0,
    val time: String = "",
    val comment: String = "",
    val magic: Long = 0
) {
    val isProfitable: Boolean get() = profit >= 0
    val isLong: Boolean get() = type.equals("BUY", ignoreCase = true)
}

@Serializable
data class MT5HistoryResponse(
    val deals: List<MT5Deal> = emptyList(),
    val count: Int = 0,
    val total_pnl: Double = 0.0,
    val last_updated: String = ""
)

@Serializable
data class MT5Deal(
    val ticket: Long = 0,
    val order: Long = 0,
    val symbol: String = "",
    val type: String = "",        // "BUY", "SELL", or "BALANCE"
    val type_raw: Int = 0,
    val entry: Int = 0,           // 0=in, 1=out, 2=reverse
    val volume: Double = 0.0,
    val price: Double = 0.0,
    val profit: Double = 0.0,
    val commission: Double = 0.0,
    val swap: Double = 0.0,
    val fee: Double = 0.0,
    val time: String = "",
    val comment: String = "",
    val magic: Long = 0
) {
    val isProfitable: Boolean get() = profit > 0
    val isBalance: Boolean get() = type == "BALANCE"
    val isEntry: Boolean get() = entry == 0
    val isExit: Boolean get() = entry == 1
    val formattedTime: String get() {
        return if (time.contains("T")) {
            time.replace("T", " ").take(19)
        } else {
            time
        }
    }
}

@Serializable
data class MT5StatusResponse(
    val connected: Boolean = false,
    val fresh: Boolean = false,
    val last_seen: String? = null,
    val age_seconds: Double = 0.0,
    val positions: Int = 0,
    val account_login: Long? = null,
    val message: String? = null
)
