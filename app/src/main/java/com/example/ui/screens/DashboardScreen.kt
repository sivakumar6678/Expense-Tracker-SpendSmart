package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.ui.components.TransactionCardItem
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.FinancialInsight
import com.example.model.InsightType
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.FinanceViewModel
import com.example.viewmodel.InsightsUiState
import com.example.utils.CurrencyUtils
import com.example.model.AccountType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    financeViewModel: FinanceViewModel,
    authViewModel: AuthViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToTransactionDetails: (Int) -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToManageBills: () -> Unit,
    onNavigateToBudgets: () -> Unit
) {
    val totalIncome by financeViewModel.totalIncome.collectAsState()
    val totalExpense by financeViewModel.totalExpense.collectAsState()
    val availableBalance by financeViewModel.availableBalance.collectAsState()
    val savingsBalance by financeViewModel.savingsBalance.collectAsState()
    val walletBalance by financeViewModel.walletBalance.collectAsState()
    val totalBalance by financeViewModel.totalBalance.collectAsState()
    val accountsWithBalance by financeViewModel.accountsWithBalance.collectAsState()
    var showAccountsBottomSheet by remember { mutableStateOf(false) }
    var showFullBalanceSheet by remember { mutableStateOf(false) }
    val transactions by financeViewModel.transactions.collectAsState()
    val bills by financeViewModel.bills.collectAsState()
    val recurringTransactions by financeViewModel.recurringTransactions.collectAsState()
    val userName by authViewModel.userName.collectAsState()
    val monthlyGoal by authViewModel.monthlyGoal.collectAsState()
    val savingsGoal by authViewModel.savingsGoal.collectAsState()
    val insightsState by financeViewModel.insightsState.collectAsState()

    var showQuickAddDialog by remember { mutableStateOf(false) }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0F2FE)) // Sky-50
                                .border(BorderStroke(1.dp, Color(0xFFBAE6FD)), CircleShape), // Sky-100
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1) // Sky-700
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Welcome back,",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFF1F5F9), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF334155)
                        )
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
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Balance Glass Card
                item {
                    BalanceCard(
                        availableBalance = availableBalance,
                        income = totalIncome,
                        expense = totalExpense,
                        onClickBalance = { showAccountsBottomSheet = true }
                    )
                }

                // Quick Stats Section (Added for High Density premium visual analysis)
                item {
                    QuickStatsSection(
                        savingsGoal = savingsGoal,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense
                    )
                }

                // AI Insights Engine Trigger
                item {
                    AiInsightsSection(
                        insightsState = insightsState,
                        onAskAi = { financeViewModel.generateInsights(monthlyGoal, savingsGoal) }
                    )
                }

                // Budget Alerts
                item {
                    BudgetSummarySection(
                        totalExpense = totalExpense,
                        monthlyGoal = monthlyGoal,
                        onClick = onNavigateToBudgets
                    )
                }

                // Recent Transactions Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Transactions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onNavigateToTransactions) {
                            Text("See All")
                        }
                    }
                }

                // Transaction Items (Limit to 5)
                val recentList = transactions.take(5)
                if (recentList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No transactions recorded yet",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Tap the '+' button at the bottom right to log your first income or expense.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(recentList) { transaction ->
                        TransactionCardItem(
                            transaction = transaction,
                            onClick = { onNavigateToTransactionDetails(transaction.id) }
                        )
                    }
                }

                // Upcoming mock bills section for extra premium polish
                item {
                    UpcomingBillsSection()
                }
            }
        }

        if (showAccountsBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAccountsBottomSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Spendable Accounts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val spendableAccounts = accountsWithBalance.filter { it.account.includeInAvailableBalance }
                    
                    spendableAccounts.forEach { accountItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(accountItem.account.name, style = MaterialTheme.typography.bodyLarge)
                            Text(CurrencyUtils.formatIndianCurrency(accountItem.currentBalance), fontWeight = FontWeight.Bold)
                        }
                        Divider(color = Color(0xFFF1F5F9))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { 
                        showAccountsBottomSheet = false
                        showFullBalanceSheet = true
                    }) {
                        Text("View Full Balance")
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        if (showFullBalanceSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFullBalanceSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Net Worth",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Spendable section
                    val spendableAccounts = accountsWithBalance.filter { it.account.includeInAvailableBalance }
                    val spendableTotal = spendableAccounts.sumOf { it.currentBalance }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Available Balance", fontWeight = FontWeight.Bold)
                        Text(CurrencyUtils.formatIndianCurrency(spendableTotal), fontWeight = FontWeight.Bold)
                    }

                    // Savings Goals section
                    val savingsAccounts = accountsWithBalance.filter { it.account.type == AccountType.SAVINGS }
                    if (savingsAccounts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Savings Goals", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        savingsAccounts.forEach { acc ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(acc.account.name, style = MaterialTheme.typography.bodyMedium)
                                Text(CurrencyUtils.formatIndianCurrency(acc.currentBalance), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // Investments section
                    val investAccounts = accountsWithBalance.filter { it.account.type == AccountType.INVESTMENT }
                    if (investAccounts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Investments", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        investAccounts.forEach { acc ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(acc.account.name, style = MaterialTheme.typography.bodyMedium)
                                Text(CurrencyUtils.formatIndianCurrency(acc.currentBalance), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val netWorth = accountsWithBalance.sumOf { it.currentBalance }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Net Worth", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(CurrencyUtils.formatIndianCurrency(netWorth), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun BalanceCard(
    availableBalance: Double,
    income: Double,
    expense: Double,
    onClickBalance: () -> Unit
) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF10B981), // Emerald Green
            Color(0xFF38BDF8)  // Sky Blue
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("balance_card")
            .clickable(onClick = onClickBalance),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(24.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Available Balance",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "View Accounts",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = CurrencyUtils.formatIndianCurrency(availableBalance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income Mini section
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "INCOME",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Income",
                                tint = Color(0xFF34D399), // emerald-400
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = CurrencyUtils.formatIndianCurrency(income),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Expense Mini section
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "EXPENSES",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Expense",
                                tint = Color(0xFFFB7185), // rose-400
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = CurrencyUtils.formatIndianCurrency(expense),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiInsightsSection(
    insightsState: InsightsUiState,
    onAskAi: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0FDF4) // emerald-50
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFDCFCE7)) // emerald-100
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "AI Insights",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Financial Analyst",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onAskAi,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Analyze Data", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (insightsState) {
                is InsightsUiState.Idle -> {
                    Text(
                        text = "Tap 'Analyze Data' to unlock custom tips, budget recommendations, and saving forecasts computed by Gemini AI.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is InsightsUiState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Gemini is analyzing your transaction records...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                is InsightsUiState.Success -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (insight in insightsState.insights) {
                            InsightCardItem(insight)
                        }
                    }
                }
                is InsightsUiState.Error -> {
                    Text(
                        text = "Error receiving insights: ${insightsState.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun InsightCardItem(insight: FinancialInsight) {
    val (color, icon) = when (insight.type) {
        InsightType.ALERT -> Color(0xFFF44336) to Icons.Default.Warning
        InsightType.RECOMMENDATION -> Color(0xFF009688) to Icons.Default.ThumbUp
        InsightType.PREDICTION -> Color(0xFF2196F3) to Icons.Default.TrendingUp
        InsightType.TIP -> Color(0xFFFF9800) to Icons.Default.Lightbulb
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = insight.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = insight.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BudgetSummarySection(totalExpense: Double, monthlyGoal: Double, onClick: () -> Unit) {
    if (monthlyGoal <= 0) return

    val progress = (totalExpense / monthlyGoal).coerceIn(0.0, 1.0).toFloat()
    val isNearLimit = totalExpense >= (monthlyGoal * 0.85)
    val isExceeded = totalExpense > monthlyGoal
    val remaining = (monthlyGoal - totalExpense).coerceAtLeast(0.0)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Monthly Budget",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Budget",
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF64748B)
                        )
                    }
                    Text(
                        text = if (isExceeded) "Exceeded by ${CurrencyUtils.formatIndianCurrency(totalExpense - monthlyGoal)}"
                               else "${CurrencyUtils.formatIndianCurrency(remaining)} left of ${CurrencyUtils.formatIndianCurrency(monthlyGoal)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }

                // Spent Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isExceeded) Color(0xFFFFE4E6) // rose-100
                            else if (isNearLimit) Color(0xFFFEF3C7) // amber-100
                            else Color(0xFFF0F9FF) // indigo-50
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${String.format("%.0f", progress * 100)}% Spent",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isExceeded) Color(0xFFE11D48) // rose-600
                                else if (isNearLimit) Color(0xFFD97706) // amber-600
                                else Color(0xFF10B981) // indigo-600
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isExceeded) Color(0xFFFB7185) // rose-400
                        else if (isNearLimit) Color(0xFFF59E0B) // amber-500
                        else Color(0xFF38BDF8), // Sky Blue
                trackColor = Color(0xFFF1F5F9) // slate-100
            )

            if (isExceeded) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF1F2)) // rose-50
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Exceeded your set limit by ${CurrencyUtils.formatIndianCurrency(totalExpense - monthlyGoal)}!",
                        color = Color(0xFFE11D48),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onItemClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val typeColor = when (transaction.type) {
        TransactionType.EXPENSE -> Color(0xFFFB7185)
        TransactionType.INCOME -> Color(0xFF34D399)
        TransactionType.TRANSFER -> Color(0xFF3B82F6)
    }

    val categoryColor = when (transaction.category.uppercase()) {
        "SALARY" -> Color(0xFF10B981)
        "FOOD" -> Color(0xFFF59E0B)
        "SHOPPING" -> Color(0xFFEC4899)
        "TRAVEL" -> Color(0xFF3B82F6)
        "BILLS" -> Color(0xFF8B5CF6)
        "RENT" -> Color(0xFFF97316)
        else -> Color(0xFF6366F1)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${transaction.paymentMethod}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    val prefixSign = when (transaction.type) {
                        TransactionType.EXPENSE -> "-"
                        TransactionType.INCOME -> "+"
                        TransactionType.TRANSFER -> "⇅ "
                    }
                    Text(
                        text = "$prefixSign${CurrencyUtils.formatIndianCurrency(transaction.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(transaction.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (transaction.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (transaction.isFavorite) Color(0xFFF43F5E) else Color(0xFFCBD5E1),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UpcomingBillsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upcoming Bills & Recurring",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(BorderStroke(1.dp, Color(0xFFF1F5F9)), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Adobe Creative Cloud", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Due in 3 days • Subscription", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                }
                Text("₹1,499", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(BorderStroke(1.dp, Color(0xFFF1F5F9)), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Internet & Wifi Bill", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Due in 6 days • Utilities", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                }
                Text("₹3,999", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
            }
        }
    }
}

@Composable
fun QuickStatsSection(savingsGoal: Double, totalIncome: Double, totalExpense: Double) {
    val savings = (totalIncome - totalExpense).coerceAtLeast(0.0)
    val percentOfGoal = if (savingsGoal > 0) (savings / savingsGoal * 100).coerceIn(0.0, 100.0) else 0.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Savings Card
        Card(
            modifier = Modifier
                .weight(1f)
                .height(110.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SAVINGS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold
                )
                Column {
                    Text(
                        text = CurrencyUtils.formatIndianCurrency(savings),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (savingsGoal > 0) "${String.format("%.0f", percentOfGoal)}% of goal" else "No goal set",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Analysis Donut Graphic Card
        Card(
            modifier = Modifier
                .weight(1f)
                .height(110.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.size(64.dp)
                ) {
                    val strokeWidth = 8.dp.toPx()
                    // Background circle
                    drawCircle(
                        color = Color(0xFFF8FAFC),
                        radius = size.minDimension / 2 - strokeWidth / 2,
                        style = Stroke(width = strokeWidth)
                    )
                    // Expense segment (rose)
                    val expenseAngle = if (totalIncome > 0) ((totalExpense / totalIncome) * 360.0).coerceIn(0.0, 360.0).toFloat() else 120f
                    drawArc(
                        color = Color(0xFFFB7185), // rose-400
                        startAngle = -90f,
                        sweepAngle = expenseAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    // Savings segment (sky blue)
                    val savingsAngle = 360f - expenseAngle
                    drawArc(
                        color = Color(0xFF38BDF8), // Sky Blue
                        startAngle = -90f + expenseAngle,
                        sweepAngle = savingsAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "Analysis",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
            }
        }
    }
}

