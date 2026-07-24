package com.jarvis.app.services

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class NotificationService(
    private val wsUrl: String = "ws://10.0.2.2:3000/ws"
) {
    private val _notifications = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val notifications: SharedFlow<String> = _notifications.asSharedFlow()

    private var session: WebSocketSession? = null
    private var job: Job? = null

    suspend fun connect() {
        val client = HttpClient {
            install(WebSockets)
        }
        job = CoroutineScope(Dispatchers.Default).launch {
            try {
                client.webSocket(wsUrl) {
                    session = this
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            _notifications.emit(frame.readText())
                        }
                    }
                }
            } catch (e: Exception) {
                _notifications.emit("Connection error: ${e.message}")
            }
        }
    }

    fun disconnect() {
        job?.cancel()
        session = null
    }
}
