package com.jarvis.app.models

import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val notes: String = ""
)
