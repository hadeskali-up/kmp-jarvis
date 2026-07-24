package com.jarvis.app.services

import com.jarvis.app.models.Expense
import kotlinx.coroutines.delay

/**
 * In-memory expense store with paginated query support.
 *
 * In production, this would use platform-specific SQLite via SQLDelight.
 * The simulateNetworkDelay flag is retained for realistic loading UX.
 */
class ExpenseDatabase {

    private val allExpenses = listOf(
        Expense("e01", "Nasi Lemak", 8.50, "Food", "2025-07-20", "Breakfast at mamak"),
        Expense("e02", "Grab to KLCC", 15.00, "Transport", "2025-07-20", ""),
        Expense("e03", "VS Code Copilot Monthly", 42.00, "Subscription", "2025-07-19", "Monthly sub"),
        Expense("e04", "Coffee beans 1kg", 68.00, "Food", "2025-07-18", "Liberica from Lazada"),
        Expense("e05", "Electricity bill", 125.40, "Utilities", "2025-07-17", "TNB"),
        Expense("e06", "Unifi 500Mbps", 139.00, "Subscription", "2025-07-16", "Internet"),
        Expense("e07", "Lunch at Kenny Hills", 45.00, "Food", "2025-07-15", "Team lunch"),
        Expense("e08", "Touch 'n Go topup", 50.00, "Transport", "2025-07-14", "Toll + parking"),
        Expense("e09", "GitHub Copilot Business", 89.00, "Subscription", "2025-07-13", "Annual plan"),
        Expense("e10", "Roti canai + teh tarik", 5.80, "Food", "2025-07-12", ""),
        Expense("e11", "LRT monthly pass", 50.00, "Transport", "2025-07-11", "My50 pass"),
        Expense("e12", "Figma Pro", 60.00, "Subscription", "2025-07-10", ""),
        Expense("e13", "Water bill", 18.00, "Utilities", "2025-07-09", "Air Selangor"),
        Expense("e14", "Dinner at Din Tai Fung", 92.00, "Food", "2025-07-08", "Family dinner"),
        Expense("e15", "Petrol Shell", 80.00, "Transport", "2025-07-07", "Shell V-Power"),
        Expense("e16", "Spotify Family", 23.80, "Subscription", "2025-07-06", ""),
        Expense("e17", "Groceries Aeon", 156.30, "Food", "2025-07-05", "Weekly groceries"),
        Expense("e18", "Car service", 320.00, "Transport", "2025-07-04", "Perodua service center"),
        Expense("e19", "AirAsia flight ticket", 189.00, "Travel", "2025-07-03", "KUL → PEN"),
        Expense("e20", "Mamak supper", 12.80, "Food", "2025-07-02", ""),
        Expense("e21", "Netflix Premium", 27.90, "Subscription", "2025-07-01", ""),
        Expense("e22", "Maxis postpaid", 98.00, "Subscription", "2025-06-30", ""),
        Expense("e23", "Dodol & kuih raya", 45.60, "Food", "2025-06-29", "Pasar Ramadan"),
        Expense("e24", "Parking KL Sentral", 24.00, "Transport", "2025-06-28", ""),
        Expense("e25", "Book - Designing Data-Intensive Applications", 152.00, "Education", "2025-06-27", "Kinokuniya"),
    )

    data class PaginatedResult(
        val items: List<Expense>,
        val hasMore: Boolean,
        val total: Int,
    )

    /**
     * Fetch a page of expenses.
     *
     * @param page     0-based page number.
     * @param pageSize Number of items per page.
     * @param simulateNetworkDelay Adds a small delay to show loading indicators (default true).
     */
    suspend fun getExpenses(
        page: Int = 0,
        pageSize: Int = 10,
        simulateNetworkDelay: Boolean = true,
    ): PaginatedResult {
        if (simulateNetworkDelay) {
            delay(600)
        }
        val offset = page * pageSize
        val pageItems = allExpenses.drop(offset).take(pageSize)
        return PaginatedResult(
            items = pageItems,
            hasMore = offset + pageSize < allExpenses.size,
            total = allExpenses.size,
        )
    }

    suspend fun getExpenseById(id: String): Expense? {
        return allExpenses.find { it.id == id }
    }

    suspend fun initialize() {
        // In-memory; nothing to init
    }
}
