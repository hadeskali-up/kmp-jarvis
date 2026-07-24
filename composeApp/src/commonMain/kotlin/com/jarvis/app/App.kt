package com.jarvis.app

import androidx.compose.runtime.*
import com.jarvis.app.theme.JarvisTheme
import com.jarvis.app.navigation.Screen
import com.jarvis.app.screens.*

@Composable
fun App() {
    JarvisTheme {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
        var selectedExpenseId by remember { mutableStateOf<String?>(null) }

        when (currentScreen) {
            is Screen.Home -> HomeScreen(
                onNavigate = { screen -> currentScreen = screen }
            )
            is Screen.DailyCommits -> DailyCommitsScreen(
                onBack = { currentScreen = Screen.Home }
            )
            is Screen.Welcome -> WelcomeScreen(
                onBack = { currentScreen = Screen.Home }
            )
            is Screen.Counter -> CounterScreen(
                onBack = { currentScreen = Screen.Home }
            )
            is Screen.Quote -> QuoteScreen(
                onBack = { currentScreen = Screen.Home }
            )
            is Screen.Todo -> TodoScreen(
                onBack = { currentScreen = Screen.Home }
            )
            is Screen.Calculator -> CalculatorScreen(
                onBack = { currentScreen = Screen.Home }
            )
            is Screen.Palette -> PaletteScreen(
                onBack = { currentScreen = Screen.Home }
            )
            is Screen.Converter -> ConverterScreen(
                onBack = { currentScreen = Screen.Home }
            )
            is Screen.Weather -> WeatherScreen(
                onBack = { currentScreen = Screen.Home }
            )
            is Screen.Chat -> ChatScreen(
                onBack = { currentScreen = Screen.Home }
            )
            is Screen.ExpenseCapture -> ExpenseCaptureScreen(
                onBack = { currentScreen = Screen.Home },
                onNavigateToDetail = { id ->
                    selectedExpenseId = id
                    currentScreen = Screen.ExpenseDetail.createRoute(id)
                }
            )
            is Screen.ExpenseDetail -> {
                val expenseId = selectedExpenseId ?: ""
                ExpenseDetailScreen(
                    expenseId = expenseId,
                    onBack = { currentScreen = Screen.ExpenseCapture }
                )
            }
            is Screen.ExpenseList -> ExpenseListScreen(
                onBack = { currentScreen = Screen.Home },
                onNavigateToDetail = { id ->
                    selectedExpenseId = id
                    currentScreen = Screen.ExpenseDetail.createRoute(id)
                }
            )
            is Screen.Flashcard -> FlashcardScreen(
                onBack = { currentScreen = Screen.Home }
            )
            is Screen.Timer -> TimerScreen(
                onBack = { currentScreen = Screen.Home }
            )
            is Screen.Dashboard -> DashboardScreen(
                onBack = { currentScreen = Screen.Home }
            )
            is Screen.Habit -> HabitScreen(
                onBack = { currentScreen = Screen.Home }
            )
            is Screen.Notes -> NotesScreen(
                onBack = { currentScreen = Screen.Home }
            )
        }
    }
}
