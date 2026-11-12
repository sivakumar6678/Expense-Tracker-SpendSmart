package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Category
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.ui.screens.getCategoryColor
import com.example.ui.screens.getCategoryIcon
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data structure representing a date section header in the LazyColumn.
 */
data class DateSection(
    val dateKey: String,
    val displayDate: String,
    val transactionCount: Int,
    val netAmount: Double,
    val transactions: List<Transaction>
)

/**
 * High-performance, reactive LazyColumn UI component that observes Room database transactions.
 * Features:
 * - Grouped by date with sticky headers showing day net total and count
 * - Category icons with customized color badges
 * - Formatted currency amounts (+ / -) in Plus Jakarta Sans typography
 * - Swipe-to-dismiss gesture for quick deletion
 * - Tap to inspect transaction details
 * - Built-in search and filter chips
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryList(
    transactions: List<Transaction>,
    onItemClick: (Transaction) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    modifier: Modifier = Modifier,
    categories: List<Category> = emptyList(),
    searchQuery: String = "",
    onSearchQueryChange: ((String) -> Unit)? = null,
    selectedType: TransactionType? = null,
    onTypeSelect: ((TransactionType?) -> Unit)? = null,
    selectedCategory: String? = null,
    onCategorySelect: ((String?) -> Unit)? = null,
    selectedPeriod: String = "ALL",
    onPeriodSelect: ((String) -> Unit)? = null,
    onResetFilters: (() -> Unit)? = null
) {
    // 1. Group transactions by date
    val sections = remember(transactions) {
        val sdfDayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfDisplay = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())

        val calendarToday = Calendar.getInstance()
        val calendarYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        val todayKey = sdfDayKey.format(calendarToday.time)
        val yesterdayKey = sdfDayKey.format(calendarYesterday.time)

        // Sort descending by timestamp
        val sorted = transactions.sortedByDescending { it.timestamp }
        val grouped = sorted.groupBy { sdfDayKey.format(Date(it.timestamp)) }

        grouped.map { (key, txList) ->
            val firstTxDate = Date(txList.first().timestamp)
            val displayLabel = when (key) {
                todayKey -> "Today • ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(firstTxDate)}"
                yesterdayKey -> "Yesterday • ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(firstTxDate)}"
                else -> sdfDisplay.format(firstTxDate)
            }

            // Calculate day's net: Income - Expense
            val net = txList.sumOf { tx ->
                when (tx.type) {
                    TransactionType.INCOME -> tx.amount
                    TransactionType.EXPENSE -> -tx.amount
                    TransactionType.TRANSFER -> 0.0
                }
            }

            DateSection(
                dateKey = key,
                displayDate = displayLabel,
                transactionCount = txList.size,
                netAmount = net,
                transactions = txList
            )
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search & Filter Header (if callbacks provided)
        if (onSearchQueryChange != null) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_input"),
                placeholder = { Text("Search by title, notes, tags...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                singleLine = true
            )
        }

        // Horizontal Quick-Filter Chips
        if (onTypeSelect != null) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Type Filter Chips
                item {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { onTypeSelect(null) },
                        label = { Text("All Types") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = { onTypeSelect(if (selectedType == TransactionType.EXPENSE) null else TransactionType.EXPENSE) },
                        label = { Text("Expenses") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = { onTypeSelect(if (selectedType == TransactionType.INCOME) null else TransactionType.INCOME) },
                        label = { Text("Income") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedType == TransactionType.TRANSFER,
                        onClick = { onTypeSelect(if (selectedType == TransactionType.TRANSFER) null else TransactionType.TRANSFER) },
                        label = { Text("Transfers") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }

                // Period Chips (if provided)
                if (onPeriodSelect != null) {
                    listOf("TODAY", "WEEK", "MONTH").forEach { p ->
                        item {
                            FilterChip(
                                selected = selectedPeriod == p,
                                onClick = { onPeriodSelect(if (selectedPeriod == p) "ALL" else p) },
                                label = { Text(p.lowercase().replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }
            }
        }

        // Active Category Chips (if provided)
        if (onCategorySelect != null && categories.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onCategorySelect(null) },
                        label = { Text("All Categories") }
                    )
                }
                items(categories.take(8)) { cat ->
                    val isSelected = cat.name.equals(selectedCategory, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(if (isSelected) null else cat.name) },
                        label = { Text(cat.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(cat.name),
                                contentDescription = null,
                                tint = getCategoryColor(cat.name),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        // Transaction List or Empty State
        if (transactions.isEmpty()) {
            EmptyTransactionsState(onResetFilters = onResetFilters)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sections.forEach { section ->
                    // Sticky Header for the date
                    stickyHeader(key = "header_${section.dateKey}") {
                        TransactionStickyHeader(section = section)
                    }

                    // Transactions for this day
                    items(
                        items = section.transactions,
                        key = { it.id }
                    ) { transaction ->
                        SwipeToDismissTransactionItem(
                            transaction = transaction,
                            onClick = { onItemClick(transaction) },
                            onDelete = { onDeleteTransaction(transaction) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sticky Date Section Header with day name, count, and net cash flow amount.
 */
@Composable
fun TransactionStickyHeader(section: DateSection) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = section.displayDate,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlusJakartaSansFamily,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${section.transactionCount}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Day Net Total
            if (section.netAmount != 0.0) {
                val netColor = if (section.netAmount > 0) Color(0xFF10B981) else Color(0xFFEF4444)
                val prefix = if (section.netAmount > 0) "+ " else ""
                Text(
                    text = "$prefix${CurrencyUtils.formatIndianCurrency(section.netAmount)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = PlusJakartaSansFamily,
                    color = netColor
                )
            }
        }
    }
}

/**
 * SwipeToDismiss wrapper around TransactionCardItem for gesture-based quick deletion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissTransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val alignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEF4444))
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    ) {
        TransactionCardItem(
            transaction = transaction,
            onClick = onClick
        )
    }
}

/**
 * Polished Transaction Item Card with category icon, title, metadata badges, and formatted currency.
 */
@Composable
fun TransactionCardItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val isIncome = transaction.type == TransactionType.INCOME

    val amountColor = when (transaction.type) {
        TransactionType.INCOME -> Color(0xFF10B981) // Emerald
        TransactionType.EXPENSE -> Color(0xFFEF4444) // Coral Red
        TransactionType.TRANSFER -> Color(0xFF3B82F6) // Royal Blue
    }

    val amountPrefix = when (transaction.type) {
        TransactionType.INCOME -> "+ "
        TransactionType.EXPENSE -> "- "
        TransactionType.TRANSFER -> ""
    }

    val categoryColor = getCategoryColor(transaction.category)
    val categoryIcon = getCategoryIcon(transaction.category)

    val timeFormatted = remember(transaction.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(transaction.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = transaction.category,
                    tint = categoryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Transaction Info Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title.ifBlank { transaction.category },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = PlusJakartaSansFamily,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category & Account Tag
                    Text(
                        text = if (transaction.type == TransactionType.TRANSFER) {
                            "${transaction.paymentMethod} → ${transaction.toAccount ?: "Account"}"
                        } else {
                            "${transaction.category} • ${transaction.paymentMethod}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Time
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Optional Notes / Location Snippet
                if (transaction.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = transaction.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Formatted Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$amountPrefix${CurrencyUtils.formatIndianCurrency(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlusJakartaSansFamily,
                    color = amountColor
                )

                if (transaction.isRecurring) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Recurring",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Empty state graphic when no transactions are found.
 */
@Composable
fun EmptyTransactionsState(onResetFilters: (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Transactions Found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = PlusJakartaSansFamily,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Try adjusting your search query, clearing filters, or log a new transaction.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (onResetFilters != null) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onResetFilters,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear Active Filters")
                }
            }
        }
    }
}
