package com.jarvis.app.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = 0L
)

@Serializable
data class ChatApiResponse(
    val response: String = "",
    val used_model: String = ""
)
