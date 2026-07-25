package com.jarvis.app

import androidx.compose.runtime.*
import com.jarvis.app.theme.JarvisTheme
import com.jarvis.app.navigation.Screen
import com.jarvis.app.screens.*
import com.jarvis.app.util.BackHandler

@Composable
fun App() {
    JarvisTheme {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
        var selectedExpenseId by remember { mutableStateOf<String?>(null) }
        val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }

        fun navigateTo(screen: Screen) {
            backStack.add(screen)
            currentScreen = screen
        }

        fun goBack() {
            if (backStack.size > 1) {
                backStack.removeLast()
                currentScreen = backStack.last()
            }
        }

        // Handle system back gesture
        BackHandler(enabled = backStack.size > 1) { goBack() }

        when (currentScreen) {
            is Screen.Home -> HomeScreen(
                onNavigate = { screen -> navigateTo(screen) }
            )
            is Screen.AppList -> AppListScreen(
                onBack = { goBack() },
                onNavigate = { screen -> navigateTo(screen) }
            )
            is Screen.Dashboard -> DashboardScreen(
                onBack = { goBack() }
            )
            is Screen.DailyCommits -> DailyCommitsScreen(
                onBack = { goBack() }
            )
            is Screen.Welcome -> WelcomeScreen(
                onBack = { goBack() }
            )
            is Screen.Counter -> CounterScreen(
                onBack = { goBack() }
            )
            is Screen.Quote -> QuoteScreen(
                onBack = { goBack() }
            )
            is Screen.Todo -> TodoScreen(
                onBack = { goBack() }
            )
            is Screen.Calculator -> CalculatorScreen(
                onBack = { goBack() }
            )
            is Screen.Palette -> PaletteScreen(
                onBack = { goBack() }
            )
            is Screen.Converter -> ConverterScreen(
                onBack = { goBack() }
            )
            is Screen.Weather -> WeatherScreen(
                onBack = { goBack() }
            )
            is Screen.Chat -> ChatScreen(
                onBack = { goBack() }
            )
            is Screen.ExpenseCapture -> ExpenseCaptureScreen(
                onBack = { goBack() },
                onNavigateToDetail = { id ->
                    selectedExpenseId = id
                    navigateTo(Screen.ExpenseDetail)
                }
            )
            is Screen.ExpenseDetail -> {
                val expenseId = selectedExpenseId ?: ""
                ExpenseDetailScreen(
                    expenseId = expenseId,
                    onBack = { goBack() }
                )
            }
            is Screen.ExpenseList -> ExpenseListScreen(
                onBack = { goBack() },
                onNavigateToDetail = { id ->
                    selectedExpenseId = id
                    navigateTo(Screen.ExpenseDetail)
                }
            )
            is Screen.Flashcard -> FlashcardScreen(
                onBack = { goBack() }
            )
            is Screen.Timer -> TimerScreen(
                onBack = { goBack() }
            )
            is Screen.Habit -> HabitScreen(
                onBack = { goBack() }
            )
            is Screen.Notes -> NotesScreen(
                onBack = { goBack() }
            )
            is Screen.Crypto -> CryptoScreen(
                onBack = { goBack() }
            )
            is Screen.Forex -> ForexScreen(
                onBack = { goBack() }
            )
        }
    }
}
