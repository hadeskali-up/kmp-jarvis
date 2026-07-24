package com.jarvis.app.services

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class DashboardService(
    private val baseUrl: String = "http://10.0.2.2:3000"
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun fetchDashboardData(): String {
        return try {
            client.get("$baseUrl/api/dashboard").body()
        } catch (e: Exception) {
            "{\"error\": \"${e.message}\"}"
        }
    }

    fun cleanup() {
        client.close()
    }
}
