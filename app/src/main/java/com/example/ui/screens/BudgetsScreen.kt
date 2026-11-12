package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Budget
import com.example.model.Category
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.viewmodel.FinanceViewModel
import com.example.viewmodel.AuthViewModel
import com.example.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: FinanceViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val budgets by viewModel.budgets.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var selectedCategory by remember { mutableStateOf("Food") }
    var amountText by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
    val monthlyGoal by authViewModel.monthlyGoal.collectAsState()
    val userName by authViewModel.userName.collectAsState()
    val currency by authViewModel.currencyCode.collectAsState()
    val photoUri by authViewModel.profilePhotoUri.collectAsState()
    val savingsGoal by authViewModel.savingsGoal.collectAsState()
    var showEditMonthlyGoalDialog by remember { mutableStateOf(false) }
    
    if (showEditMonthlyGoalDialog) {
        var goalText by remember { mutableStateOf(monthlyGoal.toString()) }
        AlertDialog(
            onDismissRequest = { showEditMonthlyGoalDialog = false },
            title = { Text("Edit Monthly Budget") },
            text = {
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it },
                    label = { Text("Amount") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = { 
                    val goal = goalText.toDoubleOrNull() ?: monthlyGoal
                    authViewModel.updateProfile(userName, currency, photoUri, goal, savingsGoal)
                    showEditMonthlyGoalDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { 
                        authViewModel.updateProfile(userName, currency, photoUri, 0.0, savingsGoal)
                        showEditMonthlyGoalDialog = false
                    }) { Text("Reset", color = MaterialTheme.colorScheme.error) }
                    TextButton(onClick = { showEditMonthlyGoalDialog = false }) { Text("Cancel") }
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Planner", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Monthly Budget Summary Card
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showEditMonthlyGoalDialog = true },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Monthly Budget", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        Text(CurrencyUtils.formatIndianCurrency(monthlyGoal), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { showEditMonthlyGoalDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Setup Budget Card Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("budget_setup_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Create Category Budget",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category Select
                        Box(modifier = Modifier.weight(1.2f)) {
                            ExposedDropdownMenuBox(
                                expanded = categoryExpanded,
                                onExpandedChange = { categoryExpanded = !categoryExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedCategory,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Category") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = Color(0xFFCBD5E1)
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false }
                                ) {
                                    val expenseCategories = categories.filter { !it.isIncome }
                                    expenseCategories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat.name) },
                                            onClick = {
                                                selectedCategory = cat.name
                                                categoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Amount Input
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Limit ($)") },
                            placeholder = { Text("150") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("budget_amount_input"),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull()
                            if (amt != null && amt > 0.0) {
                                viewModel.setBudget(selectedCategory, amt)
                                amountText = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("set_budget_button"),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Add Category Budget", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Active Budgets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (budgets.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No category budgets set", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Configure category spending limits above to stay on track.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(budgets) { budget ->
                        // Calculate spent amount dynamically in current category
                        val categoryExpenses = expenses.filter { it.category.equals(budget.categoryName, ignoreCase = true) }
                        val spent = categoryExpenses.sumOf { it.amount }

                        BudgetItemCard(
                            budget = budget,
                            spent = spent,
                            onEdit = { selectedCategory = budget.categoryName; amountText = budget.amount.toString() },
                            onDelete = { viewModel.deleteBudget(budget) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetItemCard(
    budget: Budget,
    spent: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = if (budget.amount > 0) (spent / budget.amount).coerceIn(0.0, 1.0).toFloat() else 0f
    val isExceeded = spent > budget.amount

    val categoryColor = when (budget.categoryName.uppercase()) {
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
            .testTag("budget_item_${budget.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isExceeded) Color(0xFFFFE4E6) else categoryColor.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (budget.categoryName.uppercase()) {
                                "FOOD" -> Icons.Default.Restaurant
                                "SHOPPING" -> Icons.Default.ShoppingBag
                                "TRAVEL" -> Icons.Default.Flight
                                "BILLS" -> Icons.Default.Receipt
                                "RENT" -> Icons.Default.Home
                                else -> Icons.Default.AccountBalanceWallet
                            },
                            contentDescription = null,
                            tint = if (isExceeded) Color(0xFFEF4444) else categoryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = budget.categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFEFF6FF), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(16.dp)
                        )
                    }
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

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isExceeded) Color(0xFFFB7185) else Color(0xFF38BDF8),
                trackColor = Color(0xFFF1F5F9)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${CurrencyUtils.formatIndianCurrency(spent)} of ${CurrencyUtils.formatIndianCurrency(budget.amount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )

                if (isExceeded) {
                    Text(
                        text = "Over by ${CurrencyUtils.formatIndianCurrency(spent - budget.amount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE11D48),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "${String.format("%.0f", (1f - progress) * 100)}% Left",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
