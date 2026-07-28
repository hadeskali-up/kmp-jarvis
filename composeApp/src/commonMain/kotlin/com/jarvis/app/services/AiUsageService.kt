package com.jarvis.app.services

import com.jarvis.app.models.AiUsageData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class AiUsageService(
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

    suspend fun fetchUsage(): Result<AiUsageData> {
        return try {
            val response: AiUsageData = client.get("$baseUrl/api/ai-usage").body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cleanup() {
        client.close()
    }
}
