package com.jarvis.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(onBack: () -> Unit) {
    var selectedCategory by remember { mutableStateOf("Length") }
    var inputValue by remember { mutableStateOf("") }
    var fromUnit by remember { mutableStateOf("Meters") }
    var toUnit by remember { mutableStateOf("Feet") }
    var result by remember { mutableStateOf("") }

    val categories = listOf("Length", "Weight", "Temperature", "Volume")

    val units = when (selectedCategory) {
        "Length" -> listOf("Meters", "Feet", "Inches", "Kilometers", "Miles")
        "Weight" -> listOf("Kilograms", "Pounds", "Ounces", "Grams")
        "Temperature" -> listOf("Celsius", "Fahrenheit", "Kelvin")
        "Volume" -> listOf("Liters", "Gallons", "Cups", "Milliliters")
        else -> emptyList()
    }

    fun convert() {
        val input = inputValue.toDoubleOrNull() ?: return
        result = when {
            selectedCategory == "Length" -> convertLength(input, fromUnit, toUnit)
            selectedCategory == "Weight" -> convertWeight(input, fromUnit, toUnit)
            selectedCategory == "Temperature" -> convertTemperature(input, fromUnit, toUnit)
            selectedCategory == "Volume" -> convertVolume(input, fromUnit, toUnit)
            else -> "N/A"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unit Converter") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Category chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input
            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text("Value") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // From unit
            Text("From:", style = MaterialTheme.typography.bodyMedium)
            units.forEach { unit ->
                FilterChip(
                    selected = fromUnit == unit,
                    onClick = { fromUnit = unit },
                    label = { Text(unit) },
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("To:", style = MaterialTheme.typography.bodyMedium)
            units.forEach { unit ->
                FilterChip(
                    selected = toUnit == unit,
                    onClick = { toUnit = unit },
                    label = { Text(unit) },
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { convert() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Convert")
            }

            if (result.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "$inputValue $fromUnit = $result $toUnit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

private fun convertLength(value: Double, from: String, to: String): String {
    val meters = when (from) {
        "Meters" -> value
        "Feet" -> value * 0.3048
        "Inches" -> value * 0.0254
        "Kilometers" -> value * 1000.0
        "Miles" -> value * 1609.344
        else -> value
    }
    val result = when (to) {
        "Meters" -> meters
        "Feet" -> meters / 0.3048
        "Inches" -> meters / 0.0254
        "Kilometers" -> meters / 1000.0
        "Miles" -> meters / 1609.344
        else -> meters
    }
    return String.format("%.4f", result)
}

private fun convertWeight(value: Double, from: String, to: String): String {
    val kg = when (from) {
        "Kilograms" -> value
        "Pounds" -> value * 0.453592
        "Ounces" -> value * 0.0283495
        "Grams" -> value / 1000.0
        else -> value
    }
    val result = when (to) {
        "Kilograms" -> kg
        "Pounds" -> kg / 0.453592
        "Ounces" -> kg / 0.0283495
        "Grams" -> kg * 1000.0
        else -> kg
    }
    return String.format("%.4f", result)
}

private fun convertTemperature(value: Double, from: String, to: String): String {
    val celsius = when (from) {
        "Celsius" -> value
        "Fahrenheit" -> (value - 32.0) * 5.0 / 9.0
        "Kelvin" -> value - 273.15
        else -> value
    }
    val result = when (to) {
        "Celsius" -> celsius
        "Fahrenheit" -> celsius * 9.0 / 5.0 + 32.0
        "Kelvin" -> celsius + 273.15
        else -> celsius
    }
    return String.format("%.2f", result)
}

private fun convertVolume(value: Double, from: String, to: String): String {
    val liters = when (from) {
        "Liters" -> value
        "Gallons" -> value * 3.78541
        "Cups" -> value * 0.236588
        "Milliliters" -> value / 1000.0
        else -> value
    }
    val result = when (to) {
        "Liters" -> liters
        "Gallons" -> liters / 3.78541
        "Cups" -> liters / 0.236588
        "Milliliters" -> liters * 1000.0
        else -> liters
    }
    return String.format("%.4f", result)
}
