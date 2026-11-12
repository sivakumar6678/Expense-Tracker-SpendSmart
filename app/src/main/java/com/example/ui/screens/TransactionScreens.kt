package com.example.ui.screens

import com.example.model.AccountType
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.ui.components.TransactionHistoryList
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Category
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.viewmodel.FinanceViewModel
import com.example.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    viewModel: FinanceViewModel,
    onNavigateToTransactionDetails: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.selectedTypeFilter.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val selectedPayment by viewModel.selectedPaymentFilter.collectAsState()
    val selectedPeriod by viewModel.selectedTimePeriod.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Transactions", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    if (selectedType != null || selectedCategory != null || selectedPayment != null || selectedPeriod != "ALL" || searchQuery.isNotEmpty()) {
                        TextButton(onClick = {
                            viewModel.searchQuery.value = ""
                            viewModel.selectedTypeFilter.value = null
                            viewModel.selectedCategoryFilter.value = null
                            viewModel.selectedPaymentFilter.value = null
                            viewModel.selectedTimePeriod.value = "ALL"
                        }) {
                            Text("Reset", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            TransactionHistoryList(
                transactions = filteredTransactions,
                categories = categories,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.searchQuery.value = it },
                selectedType = selectedType,
                onTypeSelect = { viewModel.selectedTypeFilter.value = it },
                selectedCategory = selectedCategory,
                onCategorySelect = { viewModel.selectedCategoryFilter.value = it },
                selectedPeriod = selectedPeriod,
                onPeriodSelect = { viewModel.selectedTimePeriod.value = it },
                onItemClick = { tx -> onNavigateToTransactionDetails(tx.id) },
                onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) },
                onResetFilters = {
                    viewModel.searchQuery.value = ""
                    viewModel.selectedTypeFilter.value = null
                    viewModel.selectedCategoryFilter.value = null
                    viewModel.selectedPaymentFilter.value = null
                    viewModel.selectedTimePeriod.value = "ALL"
                },
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
            )
        }
    }
}

@Composable
fun TransactionCardItemDetailed(
    transaction: Transaction,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val typeColor = when (transaction.type) {
        TransactionType.EXPENSE -> Color(0xFFFB7185)
        TransactionType.INCOME -> Color(0xFF34D399)
        TransactionType.TRANSFER -> Color(0xFF3B82F6)
    }

    val categoryColor = when (transaction.category.uppercase()) {
        "SALARY" -> Color(0xFF10B981) // emerald-500
        "FOOD" -> Color(0xFFF59E0B) // amber-500
        "SHOPPING" -> Color(0xFFEC4899) // pink-500
        "TRAVEL" -> Color(0xFF3B82F6) // blue-500
        "BILLS" -> Color(0xFF8B5CF6) // purple-500
        "RENT" -> Color(0xFFF97316) // orange-500
        else -> Color(0xFF6366F1) // indigo-500
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("transaction_item_${transaction.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(categoryColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (transaction.category.uppercase()) {
                                "SALARY" -> Icons.Default.Work
                                "FOOD" -> Icons.Default.Restaurant
                                "SHOPPING" -> Icons.Default.ShoppingBag
                                "TRAVEL" -> Icons.Default.Flight
                                "BILLS" -> Icons.Default.Receipt
                                "RENT" -> Icons.Default.Home
                                else -> Icons.Default.AccountBalanceWallet
                            },
                            contentDescription = transaction.category,
                            tint = categoryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = transaction.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(categoryColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = transaction.category,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = categoryColor
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            val dateString = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault()).format(Date(transaction.timestamp))
                            Text(
                                text = "• ${transaction.paymentMethod} • $dateString",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                val prefixSign = when (transaction.type) {
                    TransactionType.EXPENSE -> "-"
                    TransactionType.INCOME -> "+"
                    TransactionType.TRANSFER -> "⇅ "
                }
                Text(
                    text = "$prefixSign${CurrencyUtils.formatIndianCurrency(transaction.amount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = typeColor
                )
            }

            if (transaction.notes.isNotEmpty() || transaction.tags.isNotEmpty() || transaction.location != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(8.dp))

                if (transaction.notes.isNotEmpty()) {
                    Text(
                        text = transaction.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (transaction.tags.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            transaction.tags.split(",").forEach { tag ->
                                if (tag.trim().isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFF0F9FF), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "#${tag.trim()}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (transaction.location != null && transaction.location.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Place, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF64748B))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(transaction.location, style = MaterialTheme.typography.labelMedium, color = Color(0xFF64748B))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDuplicate,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF0F9FF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFFFF1F2), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// ADD / EDIT TRANSACTION SCREEN
// ----------------------------------------------------

@Composable
fun AddEditTransactionScreen(
    viewModel: FinanceViewModel,
    transactionId: Int?, // Pass null for adding, positive Int for editing
    defaultType: TransactionType = TransactionType.EXPENSE,
    onNavigateBack: () -> Unit
) {
    TransactionFormScreen(
        viewModel = viewModel,
        transactionId = transactionId,
        defaultType = defaultType,
        onNavigateBack = onNavigateBack
    )
}
