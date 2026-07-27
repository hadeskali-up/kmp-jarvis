package com.jarvis.app.services

import com.jarvis.app.models.CryptoPositionsResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class CryptoService(
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

    suspend fun fetchPositions(): Result<CryptoPositionsResponse> {
        return try {
            val response: CryptoPositionsResponse =
                client.get("$baseUrl/api/crypto-positions").body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cleanup() {
        client.close()
    }
}
