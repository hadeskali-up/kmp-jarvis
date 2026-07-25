package com.jarvis.app.navigation

// Simple sealed class navigation without external dependencies
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object AppList : Screen("app_list")
    data object Dashboard : Screen("day_013_dashboard")
    data object DailyCommits : Screen("daily_commits")
    data object Welcome : Screen("day_001_welcome")
    data object Counter : Screen("day_002_counter")
    data object Quote : Screen("day_003_quote")
    data object Todo : Screen("day_004_todo")
    data object Calculator : Screen("day_005_calculator")
    data object Palette : Screen("day_006_palette")
    data object Converter : Screen("day_007_converter")
    data object Weather : Screen("day_008_weather")
    data object Chat : Screen("day_009_chat")
    data object ExpenseCapture : Screen("day_010_expense_capture")
    data object ExpenseDetail : Screen("day_010_expense_detail/{expenseId}") {
        fun createRoute(expenseId: String) = "day_010_expense_detail/$expenseId"
    }
    data object ExpenseList : Screen("day_010_expense_list")
    data object Flashcard : Screen("day_011_flashcard")
    data object Timer : Screen("day_012_timer")
    data object Habit : Screen("day_014_habit")
    data object Notes : Screen("day_015_notes")
    data object Crypto : Screen("crypto_positions")
    data object Forex : Screen("forex_positions")
}
