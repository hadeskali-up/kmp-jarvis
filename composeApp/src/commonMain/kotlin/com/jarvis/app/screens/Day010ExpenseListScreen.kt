package com.jarvis.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jarvis.app.core.PaginatedList
import com.jarvis.app.models.Expense
import com.jarvis.app.services.ExpenseDatabase
import org.koin.compose.koinInject

private val CATEGORIES = listOf("All", "Food", "Transport", "Subscription", "Utilities", "Travel", "Education")
private const val PAGE_SIZE = 10

val CATEGORY_ICONS: Map<String, ImageVector> = mapOf(
    "Food" to Icons.Default.Restaurant,
    "Transport" to Icons.Default.DirectionsCar,
    "Subscription" to Icons.Default.CardMembership,
    "Utilities" to Icons.Default.ElectricalServices,
    "Travel" to Icons.Default.Flight,
    "Education" to Icons.Default.School,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
) {
    val db: ExpenseDatabase = koinInject()
    val scope = rememberCoroutineScope()

    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var hasMore by remember { mutableStateOf(true) }
    var currentPage by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf("All") }
    var isRefreshing by remember { mutableStateOf(false) }

    // Initial load + refresh
    LaunchedEffect(Unit) {
        val result = db.getExpenses(page = 0, pageSize = PAGE_SIZE)
        expenses = result.items
        hasMore = result.hasMore
        currentPage = 1
    }

    suspend fun loadNextPage() {
        if (isLoadingMore || !hasMore) return
        isLoadingMore = true
        val result = db.getExpenses(page = currentPage, pageSize = PAGE_SIZE)
        expenses = expenses + result.items
        hasMore = result.hasMore
        currentPage++
        isLoadingMore = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Expenses") },
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
        ) {
            // Category filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CATEGORIES) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            // Divider
            HorizontalDivider()

            // Paginated List
            PaginatedList(
                items = expenses,
                isLoadingMore = isLoadingMore,
                hasMore = hasMore,
                onLoadMore = { scope.launch { loadNextPage() } },
                modifier = Modifier.weight(1f),
                emptyMessage = "No expenses found."
            ) { expense ->
                ExpenseCard(
                    expense = expense,
                    onClick = { onNavigateToDetail(expense.id) }
                )
            }
        }
    }
}

@Composable
private fun ExpenseCard(
    expense: Expense,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            val icon = CATEGORY_ICONS[expense.category] ?: Icons.Default.ShoppingCart
            Icon(
                imageVector = icon,
                contentDescription = expense.category,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(12.dp))

            // Title + category + date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                expense.category,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.height(24.dp)
                    )
                    Text(
                        text = expense.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Amount
            Text(
                text = "RM %.2f".format(expense.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (expense.amount > 100)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
