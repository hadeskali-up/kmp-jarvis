package com.jarvis.app.models

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val category: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val isPinned: Boolean = false
)
