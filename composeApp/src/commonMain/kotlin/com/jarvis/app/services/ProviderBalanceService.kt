package com.jarvis.app.services

import com.jarvis.app.models.ProviderBalancesResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ProviderBalanceService(
    private val baseUrl: String = "https://bridge.alisuhari.top",
    private val readKey: String = "d03kkX6ygVd1jdi__2JuweMy6mLv7Aas3oawRWbL1ausGUI1NbIQK_N_RU5GIHxa"
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

    suspend fun fetchBalances(): Result<ProviderBalancesResponse> = runCatching {
        client.get("$baseUrl/api/provider-balances") {
            header("X-Provider-Read-Key", readKey)
        }.body()
    }

    fun cleanup() {
        client.close()
    }
}
