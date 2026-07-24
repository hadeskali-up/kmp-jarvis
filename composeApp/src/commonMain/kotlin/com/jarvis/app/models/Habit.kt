package com.jarvis.app.models

import kotlinx.serialization.Serializable

@Serializable
data class Habit(
    val id: String,
    val title: String,
    val description: String = "",
    val streak: Int = 0,
    val completedDates: List<String> = emptyList(),
    val createdAt: String = ""
)
