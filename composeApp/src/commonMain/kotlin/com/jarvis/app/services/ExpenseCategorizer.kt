package com.jarvis.app.services

object ExpenseCategorizer {
    private val categoryKeywords = mapOf(
        "Food" to listOf("food", "restaurant", "groceries", "lunch", "dinner", "breakfast", "coffee", "pizza"),
        "Transport" to listOf("uber", "lyft", "taxi", "gas", "fuel", "bus", "train", "subway"),
        "Shopping" to listOf("amazon", "store", "mall", "clothes", "electronics"),
        "Bills" to listOf("rent", "electricity", "water", "internet", "phone", "insurance"),
        "Entertainment" to listOf("movie", "netflix", "spotify", "game", "concert", "ticket"),
        "Health" to listOf("doctor", "pharmacy", "hospital", "gym", "medicine"),
        "Other" to emptyList()
    )

    fun categorize(title: String): String {
        val lower = title.lowercase()
        for ((category, keywords) in categoryKeywords) {
            if (keywords.any { lower.contains(it) }) return category
        }
        return "Other"
    }
}
