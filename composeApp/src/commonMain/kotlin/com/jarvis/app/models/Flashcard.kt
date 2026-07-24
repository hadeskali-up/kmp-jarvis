package com.jarvis.app.models

import kotlinx.serialization.Serializable

@Serializable
data class Flashcard(
    val id: String,
    val question: String,
    val answer: String,
    val category: String = "",
    val difficulty: Int = 1 // 1-5
)
