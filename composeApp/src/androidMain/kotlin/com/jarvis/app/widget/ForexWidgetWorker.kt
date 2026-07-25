package com.jarvis.app.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

class ForexWidgetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val client = HttpClient {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
            val response = client.get("https://bridge.alisuhari.top/api/forex-positions")
            val body = response.body<ForexWidgetResponse>()

            val pairs = body.positions.joinToString(", ") { it.pair }
            ForexWidgetStore.writeCache(
                context = applicationContext,
                totalPnl = body.total_pnl,
                count = body.count,
                pairsSummary = pairs
            )

            client.close()

            // Force widget refresh
            val manager = GlanceAppWidgetManager(applicationContext)
            val widget = ForexWidget()
            val ids = manager.getGlanceIds(ForexWidget::class.java)
            ids.forEach { id -> widget.update(applicationContext, id) }

            Result.success()
        } catch (e: Exception) {
            // Retry on failure (WorkManager handles backoff)
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "forex_widget_refresh"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ForexWidgetWorker>(
                15, TimeUnit.MINUTES
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}

// Lightweight response models for the worker (separate from commonMain to avoid KMP dependency)
@kotlinx.serialization.Serializable
data class ForexWidgetResponse(
    val positions: List<ForexWidgetItem> = emptyList(),
    val count: Int = 0,
    val total_pnl: Double = 0.0
)

@kotlinx.serialization.Serializable
data class ForexWidgetItem(
    val pair: String = ""
)
