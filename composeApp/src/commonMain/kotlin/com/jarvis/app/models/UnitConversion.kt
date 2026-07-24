package com.jarvis.app.models

import kotlinx.serialization.Serializable

@Serializable
data class UnitConversion(
    val id: String,
    val category: String, // length, weight, temperature, volume
    val fromUnit: String,
    val toUnit: String,
    val inputValue: Double,
    val outputValue: Double
)
