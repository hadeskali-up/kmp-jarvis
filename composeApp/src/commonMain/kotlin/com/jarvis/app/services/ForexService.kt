package com.jarvis.app.services

import com.jarvis.app.models.ForexPositionsResponse
import com.jarvis.app.models.ForexAlertsResponse
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

    fun cleanup() {
        client.close()
    }
}
