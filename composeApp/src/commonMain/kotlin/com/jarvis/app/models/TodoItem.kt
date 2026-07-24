package com.jarvis.app.models

import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val priority: Int = 0, // 0=low, 1=medium, 2=high
    val category: String = "",
    val createdAt: String = ""
)
