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
fun CalculatorScreen(onBack: () -> Unit) {
    var display by remember { mutableStateOf("0") }
    var firstNumber by remember { mutableStateOf<Double?>(null) }
    var operation by remember { mutableStateOf<String?>(null) }
    var newInput by remember { mutableStateOf(true) }

    fun onNumberClick(number: String) {
        if (newInput) {
            display = number
            newInput = false
        } else {
            display = if (display == "0") number else display + number
        }
    }

    fun onOperationClick(op: String) {
        firstNumber = display.toDoubleOrNull()
        operation = op
        newInput = true
    }

    fun onEqualsClick() {
        val second = display.toDoubleOrNull() ?: return
        val first = firstNumber ?: return
        val result = when (operation) {
            "+" -> first + second
            "-" -> first - second
            "×" -> first * second
            "÷" -> if (second != 0.0) first / second else Double.NaN
            else -> second
        }
        display = if (result.isNaN()) "Error" else
            if (result == result.toLong().toDouble()) result.toLong().toString()
            else String.format("%.2f", result)
        firstNumber = null
        operation = null
        newInput = true
    }

    fun onClear() {
        display = "0"
        firstNumber = null
        operation = null
        newInput = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculator") },
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
            // Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = display,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val buttons = listOf(
                listOf("C", "±", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=")
            )

            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { label ->
                        val isOperator = label in listOf("+", "-", "×", "÷", "=")
                        val isClear = label == "C"
                        val span = if (label == "0") 2 else 1

                        Button(
                            onClick = {
                                when (label) {
                                    "C" -> onClear()
                                    in listOf("+", "-", "×", "÷") -> onOperationClick(label)
                                    "=" -> onEqualsClick()
                                    "." -> if (!display.contains(".")) display += "."
                                    else -> onNumberClick(label)
                                }
                            },
                            modifier = Modifier
                                .weight(span.toFloat())
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    isClear -> MaterialTheme.colorScheme.error
                                    isOperator -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                contentColor = when {
                                    isClear || isOperator -> MaterialTheme.colorScheme.onPrimary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                label,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
