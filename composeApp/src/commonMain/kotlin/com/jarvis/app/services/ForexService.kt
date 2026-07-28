package com.jarvis.app.services

import com.jarvis.app.models.ForexPositionsResponse
import com.jarvis.app.models.ForexAlertsResponse
import com.jarvis.app.models.MT5PositionsResponse
import com.jarvis.app.models.MT5HistoryResponse
import com.jarvis.app.models.MT5StatusResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ForexService(
    private val baseUrl: String = "https://bridge.alisuhari.top"
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }
    }

    // ─── IG Forex (legacy) ──

    suspend fun fetchPositions(): Result<ForexPositionsResponse> {
        return try {
            val response: ForexPositionsResponse =
                client.get("$baseUrl/api/forex-positions").body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAlerts(limit: Int = 50): Result<ForexAlertsResponse> = runCatching {
        client.get("$baseUrl/api/forex-alerts?limit=$limit").body()
    }

    // ─── MT5 (Windows PC push) ──

    suspend fun fetchMT5Positions(): Result<MT5PositionsResponse> = runCatching {
        client.get("$baseUrl/api/mt5-positions").body()
    }

    suspend fun fetchMT5History(limit: Int = 50): Result<MT5HistoryResponse> = runCatching {
        client.get("$baseUrl/api/mt5-history?limit=$limit").body()
    }

    suspend fun fetchMT5Status(): Result<MT5StatusResponse> = runCatching {
        client.get("$baseUrl/api/mt5-status").body()
    }

    fun cleanup() {
        client.close()
    }
}
