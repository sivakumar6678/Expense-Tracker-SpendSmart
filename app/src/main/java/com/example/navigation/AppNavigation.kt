package com.example.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.model.TransactionType
import com.example.ui.screens.*
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    financeViewModel: FinanceViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.AUTH
    ) {
        // 1. Passcode Authentication Screen
        composable(Screen.AUTH) {
            AuthScreen(
                viewModel = authViewModel,
                financeViewModel = financeViewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.DASHBOARD) {
                        popUpTo(Screen.AUTH) { inclusive = true }
                    }
                }
            )
        }

        // 2. Main Premium Dashboard Hub
        composable(Screen.DASHBOARD) {
            var currentTab by remember { mutableIntStateOf(0) }
            var showAddTransactionSheet by remember { mutableStateOf(false) }

            BackHandler(enabled = currentTab != 0) {
                currentTab = 0
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    bottomBar = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                        ) {
                            NavigationBar(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(32.dp)),
                                tonalElevation = 8.dp,
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == 0,
                                    onClick = { currentTab = 0 },
                                    icon = { Icon(if (currentTab == 0) Icons.Filled.Dashboard else Icons.Outlined.Dashboard, contentDescription = "Dashboard") }
                                )
                                NavigationBarItem(
                                    selected = currentTab == 1,
                                    onClick = { currentTab = 1 },
                                    icon = { Icon(if (currentTab == 1) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet, contentDescription = "Transactions") }
                                )
                                Spacer(modifier = Modifier.weight(1f)) // Space for FAB
                                NavigationBarItem(
                                    selected = currentTab == 2,
                                    onClick = { currentTab = 2 },
                                    icon = { Icon(if (currentTab == 2) Icons.Filled.Assignment else Icons.Outlined.Assignment, contentDescription = "Budgets") }
                                )
                                NavigationBarItem(
                                    selected = currentTab == 3,
                                    onClick = { currentTab = 3 },
                                    icon = { Icon(if (currentTab == 3) Icons.Filled.BarChart else Icons.Outlined.BarChart, contentDescription = "Reports") }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    val modifier = Modifier.padding(paddingValues)
                    when (currentTab) {
                        0 -> DashboardScreen(
                            financeViewModel = financeViewModel,
                            authViewModel = authViewModel,
                            onNavigateToSettings = { navController.navigate(Screen.SETTINGS) },
                            onNavigateToTransactionDetails = { id -> navController.navigate(Screen.transactionDetailsRoute(id)) },
                            onNavigateToTransactions = { currentTab = 1 },
                            onNavigateToReports = { currentTab = 3 },
                            onNavigateToManageBills = { navController.navigate(Screen.MANAGE_BILLS) },
                            onNavigateToBudgets = { currentTab = 2 }
                        )
                        1 -> TransactionListScreen(
                            viewModel = financeViewModel,
                            onNavigateToTransactionDetails = { id -> navController.navigate(Screen.transactionDetailsRoute(id)) },
                            onNavigateBack = { currentTab = 0 }
                        )
                        2 -> BudgetsScreen(
                            viewModel = financeViewModel,
                            authViewModel = authViewModel,
                            onNavigateBack = { currentTab = 0 }
                        )
                        3 -> ReportsScreen(
                            viewModel = financeViewModel,
                            onNavigateBack = { currentTab = 0 },
                            onNavigateToTransactionDetails = { id -> navController.navigate(Screen.transactionDetailsRoute(id)) }
                        )
                    }
                }

                // Centered Floating Action Button (+) right above bottom navigation bar
                FloatingActionButton(
                    onClick = { showAddTransactionSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-40).dp)
                        .size(64.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction", modifier = Modifier.size(32.dp))
                }
            }

            // Modern Bottom Sheet Option Chooser
            if (showAddTransactionSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showAddTransactionSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "What would you like to log?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Option 1: Add Expense
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAddTransactionSheet = false
                                    navController.navigate("${Screen.ADD_TRANSACTION}?type=EXPENSE")
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFFB7185), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.TrendingDown, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Add Expense", fontWeight = FontWeight.Bold, color = Color(0xFFE11D48))
                                    Text("Track your daily spending & utility bills", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                                }
                            }
                        }

                        // Option 2: Add Income
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAddTransactionSheet = false
                                    navController.navigate("${Screen.ADD_TRANSACTION}?type=INCOME")
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFF34D399), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Add Income", fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                    Text("Log salary, dividends, payouts or cashback", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                                }
                            }
                        }

                        // Option 3: Add Transfer
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAddTransactionSheet = false
                                    navController.navigate("${Screen.ADD_TRANSACTION}?type=TRANSFER")
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFF3B82F6), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.CompareArrows, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Add Transfer", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                    Text("Move funds between wallets, cash or savings accounts", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // 3. Add Transaction Screen (supports optional default type query parameter)
        composable(
            route = "${Screen.ADD_TRANSACTION}?type={type}",
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = "EXPENSE"
                }
            )
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("type") ?: "EXPENSE"
            val defaultType = when (typeStr.uppercase()) {
                "INCOME" -> TransactionType.INCOME
                "TRANSFER" -> TransactionType.TRANSFER
                else -> TransactionType.EXPENSE
            }
            AddEditTransactionScreen(
                viewModel = financeViewModel,
                transactionId = null,
                defaultType = defaultType,
                onNavigateBack = { navController.popBackStack() }
            )
        }

                composable(Screen.TRANSACTION_DETAILS) { backStackEntry ->
            val transactionIdStr = backStackEntry.arguments?.getString("transactionId")
            val id = transactionIdStr?.toIntOrNull()
            if (id != null) {
                com.example.ui.screens.TransactionDetailsScreen(
                    transactionId = id,
                    viewModel = financeViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { editId -> navController.navigate(Screen.editTransactionRoute(editId)) }
                )
            }
        }

        // 4. Edit Transaction Screen
        composable(Screen.EDIT_TRANSACTION) { backStackEntry ->
            val transactionIdStr = backStackEntry.arguments?.getString("transactionId")
            val id = transactionIdStr?.toIntOrNull()

            AddEditTransactionScreen(
                viewModel = financeViewModel,
                transactionId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SAVINGS_GOALS) {
            SavingsGoalsScreen(
                viewModel = financeViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MANAGE_ACCOUNTS) {
            ManageAccountsScreen(
                viewModel = financeViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.MANAGE_BILLS) {
            com.example.ui.screens.ManageBillsScreen(
                viewModel = financeViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5. Separate Settings Route (if navigated to directly)
        composable(Screen.SETTINGS) {
            SettingsScreen(
                authViewModel = authViewModel,
                financeViewModel = financeViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAccounts = { navController.navigate(Screen.MANAGE_ACCOUNTS) },
                onNavigateToSavingsGoals = { navController.navigate(Screen.SAVINGS_GOALS) }
            )
        }
    }
}
