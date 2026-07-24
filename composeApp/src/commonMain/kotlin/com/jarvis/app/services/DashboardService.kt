package com.jarvis.app.services

import com.jarvis.app.models.SnapshotResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class DashboardService(
    private val baseUrl: String = "https://dashboard.alisuhari.top"
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun fetchSnapshot(): Result<SnapshotResponse> {
        return try {
            val response: SnapshotResponse = client.get("$baseUrl/api/snapshot").body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cleanup() {
        client.close()
    }
}
