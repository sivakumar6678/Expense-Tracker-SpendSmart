package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Bill
import com.example.model.RecurringTransaction
import com.example.model.TransactionType
import com.example.utils.CurrencyUtils
import com.example.viewmodel.FinanceViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBillsScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit
) {
    val bills by viewModel.bills.collectAsState()
    val recurring by viewModel.recurringTransactions.collectAsState()
    var showAddBillDialog by remember { mutableStateOf(false) }
    var showAddRecurringDialog by remember { mutableStateOf(false) }
    var tabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bills & Recurring", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (tabIndex == 0) showAddBillDialog = true else showAddRecurringDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            TabRow(selectedTabIndex = tabIndex) {
                Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Upcoming Bills") })
                Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Recurring Transactions") })
            }

            if (tabIndex == 0) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(bills) { bill ->
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(bill.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("${bill.repeatType} • ${bill.category}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text(CurrencyUtils.formatIndianCurrency(bill.amount), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                                }
                                IconButton(onClick = { viewModel.deleteBill(bill) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(recurring) { txn ->
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(txn.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("${txn.repeatType} • ${txn.type.name}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    val color = if (txn.type == TransactionType.INCOME) Color(0xFF10B981) else Color(0xFFEF4444)
                                    Text(CurrencyUtils.formatIndianCurrency(txn.amount), style = MaterialTheme.typography.titleSmall, color = color)
                                }
                                IconButton(onClick = { viewModel.deleteRecurringTransaction(txn) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddBillDialog) {
        var name by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        var repeat by remember { mutableStateOf("MONTHLY") }
        AlertDialog(
            onDismissRequest = { showAddBillDialog = false },
            title = { Text("Add Bill") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Bill Name") })
                    OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Amount") })
                    OutlinedTextField(value = repeat, onValueChange = { repeat = it }, label = { Text("Repeat (MONTHLY, WEEKLY)") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        viewModel.addBill(Bill(name = name, amount = amt, category = "Bills", dueDate = System.currentTimeMillis(), repeatType = repeat, reminder = true, account = "Bank", status = "UNPAID"))
                        showAddBillDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAddBillDialog = false }) { Text("Cancel") } }
        )
    }

    if (showAddRecurringDialog) {
        var title by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        var type by remember { mutableStateOf(TransactionType.EXPENSE) }
        var repeat by remember { mutableStateOf("MONTHLY") }
        AlertDialog(
            onDismissRequest = { showAddRecurringDialog = false },
            title = { Text("Add Recurring Txn") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                    OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Amount") })
                    OutlinedTextField(value = repeat, onValueChange = { repeat = it }, label = { Text("Repeat (MONTHLY, YEARLY)") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank()) {
                        viewModel.addRecurringTransaction(RecurringTransaction(title = title, amount = amt, type = type, category = "General", account = "Bank", repeatType = repeat, nextDate = System.currentTimeMillis(), reminder = false))
                        showAddRecurringDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAddRecurringDialog = false }) { Text("Cancel") } }
        )
    }
}
