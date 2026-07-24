package com.jarvis.app.models

import kotlinx.serialization.Serializable

@Serializable
data class WeatherCondition(
    val temperature: Double = 0.0,
    val feelsLike: Double = 0.0,
    val condition: String = "Clear",
    val description: String = "",
    val humidity: Int = 0,
    val windSpeed: Double = 0.0,
    val icon: String = "01d",
    val location: String = "Unknown"
)
