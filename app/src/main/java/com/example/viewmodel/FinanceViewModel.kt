package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.model.*
import com.example.repository.FinanceRepository
import com.example.services.GeminiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

sealed interface InsightsUiState {
    object Idle : InsightsUiState
    object Loading : InsightsUiState
    data class Success(val insights: List<FinancialInsight>) : InsightsUiState
    data class Error(val message: String) : InsightsUiState
}

data class TransactionFormState(
    val transactionId: Int? = null,
    val title: String = "",
    val amountText: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "Food",
    val paymentMethod: String = "Cash",
    val toAccount: String = "Bank",
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = "",
    val tags: String = "",
    val location: String = "",
    val isRecurring: Boolean = false,
    val recurrencePeriod: String = "MONTHLY",
    val isFavorite: Boolean = false,
    val titleError: String? = null,
    val amountError: String? = null,
    val isSubmitting: Boolean = false
)

class FinanceViewModel(
    private val repository: FinanceRepository,
    private val geminiService: GeminiService
) : ViewModel() {

    // Transaction Form UI State Management
    private val _transactionFormState = MutableStateFlow(TransactionFormState())
    val transactionFormState: StateFlow<TransactionFormState> = _transactionFormState.asStateFlow()

    fun initTransactionForm(transactionId: Int?, defaultType: TransactionType = TransactionType.EXPENSE) {
        if (transactionId != null && transactionId > 0) {
            val existing = transactions.value.find { it.id == transactionId }
            if (existing != null) {
                _transactionFormState.value = TransactionFormState(
                    transactionId = existing.id,
                    title = existing.title,
                    amountText = if (existing.amount % 1.0 == 0.0) existing.amount.toLong().toString() else existing.amount.toString(),
                    type = existing.type,
                    category = existing.category,
                    paymentMethod = existing.paymentMethod,
                    toAccount = existing.toAccount ?: (accounts.value.firstOrNull { it.name != existing.paymentMethod }?.name ?: "Bank"),
                    dateMillis = existing.timestamp,
                    notes = existing.notes,
                    tags = existing.tags,
                    location = existing.location ?: "",
                    isRecurring = existing.isRecurring,
                    recurrencePeriod = existing.recurrencePeriod ?: "MONTHLY",
                    isFavorite = existing.isFavorite
                )
                return
            }
        }
        val defaultAccount = accounts.value.firstOrNull()?.name ?: "Cash"
        val defaultTo = accounts.value.getOrNull(1)?.name ?: "Bank"
        val defaultCategory = if (defaultType == TransactionType.INCOME) "Salary" else "Food"
        _transactionFormState.value = TransactionFormState(
            transactionId = null,
            title = "",
            amountText = "",
            type = defaultType,
            category = defaultCategory,
            paymentMethod = defaultAccount,
            toAccount = defaultTo,
            dateMillis = System.currentTimeMillis()
        )
    }

    fun onFormTitleChange(title: String) {
        _transactionFormState.update { it.copy(title = title, titleError = null) }
    }

    fun onFormAmountChange(amount: String) {
        // Allow digits and at most one decimal point
        val sanitized = amount.filter { it.isDigit() || it == '.' }
        _transactionFormState.update { it.copy(amountText = sanitized, amountError = null) }
    }

    fun onFormDateChange(dateMillis: Long) {
        _transactionFormState.update { it.copy(dateMillis = dateMillis) }
    }

    fun onFormTypeChange(type: TransactionType) {
        val defaultCat = when (type) {
            TransactionType.INCOME -> "Salary"
            TransactionType.EXPENSE -> "Food"
            TransactionType.TRANSFER -> "Transfer"
        }
        _transactionFormState.update { it.copy(type = type, category = defaultCat) }
    }

    fun onFormCategoryChange(category: String) {
        _transactionFormState.update { it.copy(category = category) }
    }

    fun onFormPaymentMethodChange(paymentMethod: String) {
        _transactionFormState.update { it.copy(paymentMethod = paymentMethod) }
    }

    fun onFormToAccountChange(toAccount: String) {
        _transactionFormState.update { it.copy(toAccount = toAccount) }
    }

    fun onFormNotesChange(notes: String) {
        _transactionFormState.update { it.copy(notes = notes) }
    }

    fun onFormTagsChange(tags: String) {
        _transactionFormState.update { it.copy(tags = tags) }
    }

    fun onFormLocationChange(location: String) {
        _transactionFormState.update { it.copy(location = location) }
    }

    fun onFormRecurringChange(isRecurring: Boolean, period: String = "MONTHLY") {
        _transactionFormState.update { it.copy(isRecurring = isRecurring, recurrencePeriod = period) }
    }

    fun submitTransactionForm(onSuccess: () -> Unit): Boolean {
        val state = _transactionFormState.value
        val amount = state.amountText.toDoubleOrNull()
        var hasError = false
        var titleErr: String? = null
        var amountErr: String? = null

        if (state.title.isBlank()) {
            titleErr = "Description is required"
            hasError = true
        }
        if (amount == null || amount <= 0.0) {
            amountErr = "Enter a valid positive amount"
            hasError = true
        }

        if (hasError) {
            _transactionFormState.update { it.copy(titleError = titleErr, amountError = amountErr) }
            return false
        }

        viewModelScope.launch {
            _transactionFormState.update { it.copy(isSubmitting = true) }
            if (state.transactionId != null && state.transactionId > 0) {
                val existing = transactions.value.find { it.id == state.transactionId }
                if (existing != null) {
                    val updated = existing.copy(
                        title = state.title.trim(),
                        amount = amount!!,
                        timestamp = state.dateMillis,
                        category = if (state.type == TransactionType.TRANSFER) "Transfer" else state.category,
                        paymentMethod = state.paymentMethod,
                        toAccount = if (state.type == TransactionType.TRANSFER) state.toAccount else null,
                        notes = state.notes.trim(),
                        tags = state.tags.trim(),
                        location = if (state.type == TransactionType.TRANSFER) null else state.location.trim(),
                        isRecurring = state.isRecurring,
                        recurrencePeriod = state.recurrencePeriod,
                        isFavorite = state.isFavorite
                    )
                    updateTransaction(updated)
                }
            } else {
                addTransaction(
                    title = state.title.trim(),
                    amount = amount!!,
                    type = state.type,
                    category = if (state.type == TransactionType.TRANSFER) "Transfer" else state.category,
                    paymentMethod = state.paymentMethod,
                    toAccount = if (state.type == TransactionType.TRANSFER) state.toAccount else null,
                    notes = state.notes.trim(),
                    tags = state.tags.trim(),
                    location = if (state.type == TransactionType.TRANSFER) null else state.location.trim(),
                    isRecurring = state.isRecurring,
                    recurrencePeriod = state.recurrencePeriod,
                    timestamp = state.dateMillis
                )
            }
            _transactionFormState.update { it.copy(isSubmitting = false) }
            onSuccess()
        }
        return true
    }

    // Main streams
    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<Budget>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Metrics
    val totalIncome: StateFlow<Double> = repository.totalIncome
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = repository.totalExpense
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val accounts: StateFlow<List<Account>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bills: StateFlow<List<com.example.model.Bill>> = repository.allBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringTransactions: StateFlow<List<com.example.model.RecurringTransaction>> = repository.allRecurringTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    data class AccountWithBalance(
        val account: Account,
        val currentBalance: Double
    )

    val accountsWithBalance: StateFlow<List<AccountWithBalance>> = combine(accounts, transactions) { accs, txs ->
        accs.map { acc ->
            var bal = acc.initialBalance
            txs.forEach { tx ->
                if (tx.type == TransactionType.INCOME && tx.paymentMethod == acc.name) {
                    bal += tx.amount
                } else if (tx.type == TransactionType.EXPENSE && tx.paymentMethod == acc.name) {
                    bal -= tx.amount
                } else if (tx.type == TransactionType.TRANSFER) {
                    if (tx.paymentMethod == acc.name) {
                        bal -= tx.amount
                    }
                    if (tx.toAccount == acc.name) {
                        bal += tx.amount
                    }
                }
            }
            AccountWithBalance(acc, bal)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableBalance: StateFlow<Double> = accountsWithBalance.map { accs ->
        accs.filter { it.account.includeInAvailableBalance }.sumOf { it.currentBalance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val savingsBalance: StateFlow<Double> = accountsWithBalance.map { accs ->
        accs.filter { it.account.type == AccountType.SAVINGS }.sumOf { it.currentBalance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val walletBalance: StateFlow<Double> = accountsWithBalance.map { accs ->
        accs.filter { it.account.type == AccountType.WALLET }.sumOf { it.currentBalance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalBalance: StateFlow<Double> = accountsWithBalance.map { accs ->
        accs.sumOf { it.currentBalance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Filter and Search states
    val searchQuery = MutableStateFlow("")
    val selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    val selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedPaymentFilter = MutableStateFlow<String?>(null)
    val selectedTimePeriod = MutableStateFlow("ALL") // "TODAY", "WEEK", "MONTH", "ALL"

    // Combined filtered transactions
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        transactions,
        searchQuery,
        selectedTypeFilter,
        selectedCategoryFilter,
        combine(selectedPaymentFilter, selectedTimePeriod) { pay, period -> pay to period }
    ) { txList, query, type, cat, payPeriod ->
        val pay = payPeriod.first
        val period = payPeriod.second
        var list = txList

        // Query
        if (query.isNotEmpty()) {
            list = list.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.notes.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.tags.contains(query, ignoreCase = true)
            }
        }

        // Type
        if (type != null) {
            list = list.filter { it.type == type }
        }

        // Category
        if (cat != null) {
            list = list.filter { it.category.equals(cat, ignoreCase = true) }
        }

        // Payment Method
        if (pay != null) {
            list = list.filter { it.paymentMethod.equals(pay, ignoreCase = true) }
        }

        // Time Period
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        list = when (period) {
            "TODAY" -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                list.filter { it.timestamp >= startOfDay }
            }
            "WEEK" -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfWeek = calendar.timeInMillis
                list.filter { it.timestamp >= startOfWeek }
            }
            "MONTH" -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.timeInMillis
                list.filter { it.timestamp >= startOfMonth }
            }
            else -> list
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Insights state
    private val _insightsState = MutableStateFlow<InsightsUiState>(InsightsUiState.Idle)
    val insightsState: StateFlow<InsightsUiState> = _insightsState.asStateFlow()

    fun generateInsights(monthlyGoal: Double, savingsGoal: Double) {
        viewModelScope.launch {
            _insightsState.value = InsightsUiState.Loading
            try {
                val txList = transactions.value
                val insights = geminiService.analyzeFinancials(txList, monthlyGoal, savingsGoal)
                _insightsState.value = InsightsUiState.Success(insights)
            } catch (e: Exception) {
                _insightsState.value = InsightsUiState.Error(e.message ?: "Analysis failed")
            }
        }
    }

    // CRUD Transaction
    // CRUD Account
    fun addAccount(name: String, type: AccountType, initialBalance: Double, includeInAvailableBalance: Boolean) {
        viewModelScope.launch {
            val acc = Account(name = name, type = type, initialBalance = initialBalance, includeInAvailableBalance = includeInAvailableBalance)
            repository.insertAccount(acc)
        }
    }
    fun updateAccount(acc: Account) {
        viewModelScope.launch {
            repository.updateAccount(acc)
        }
    }
    fun deleteAccount(acc: Account) {
        viewModelScope.launch {
            repository.deleteAccount(acc)
        }
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        paymentMethod: String,
        toAccount: String? = null,
        notes: String = "",
        tags: String = "",
        receiptUri: String? = null,
        location: String? = null,
        isRecurring: Boolean = false,
        recurrencePeriod: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val tx = Transaction(
                title = title,
                amount = amount,
                timestamp = timestamp,
                category = category,
                paymentMethod = paymentMethod,
                toAccount = toAccount,

                notes = notes,
                type = type,
                tags = tags,
                receiptUri = receiptUri,
                location = location,
                isRecurring = isRecurring,
                recurrencePeriod = recurrencePeriod
            )
            repository.insertTransaction(tx)
        }
    }

    fun updateTransaction(tx: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(tx)
        }
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    fun deleteTransactions(ids: List<Int>) {
        viewModelScope.launch {
            repository.deleteTransactions(ids)
        }
    }

    fun toggleFavorite(tx: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(tx.copy(isFavorite = !tx.isFavorite))
        }
    }

    fun duplicateTransaction(tx: Transaction) {
        viewModelScope.launch {
            val duplicated = tx.copy(id = 0, timestamp = System.currentTimeMillis())
            repository.insertTransaction(duplicated)
        }
    }

    // CRUD Budgets
    fun setBudget(categoryName: String, amount: Double, period: String = "MONTHLY") {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val format = SimpleDateFormat("MM/yyyy", Locale.getDefault())
            val monthYear = format.format(calendar.time)

            // Overwrite existing budget if same category and period/month
            val existing = budgets.value.find { it.categoryName == categoryName && it.period == period && it.monthYear == monthYear }
            val budget = Budget(
                id = existing?.id ?: 0,
                categoryName = categoryName,
                amount = amount,
                period = period,
                monthYear = monthYear
            )
            repository.insertBudget(budget)
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    // CRUD Category
    fun addCustomCategory(name: String, iconName: String, colorHex: String, isIncome: Boolean) {
        viewModelScope.launch {
            val cat = Category(
                name = name,
                iconName = iconName,
                colorHex = colorHex,
                isIncome = isIncome,
                isCustom = true
            )
            repository.insertCategory(cat)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // Export Reports to CSV
    suspend fun exportToCsv(): String = withContext(Dispatchers.Default) {
        val list = transactions.value
        val sb = java.lang.StringBuilder()
        sb.append("ID,Title,Amount,Type,Category,Payment Method,Date,Notes,Tags,Location\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        for (tx in list) {
            val dateStr = sdf.format(Date(tx.timestamp))
            val cleanTitle = tx.title.replace(",", " ")
            val cleanNotes = tx.notes.replace(",", " ")
            val cleanTags = tx.tags.replace(",", ";")
            val cleanLocation = (tx.location ?: "").replace(",", " ")
            sb.append("${tx.id},$cleanTitle,${tx.amount},${tx.type},${tx.category},${tx.paymentMethod},$dateStr,$cleanNotes,$cleanTags,$cleanLocation\n")
        }
        sb.toString()
    }

    // Import Reports/Backup from CSV
    fun importFromCsv(csvText: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            var count = 0
            val lines = csvText.split("\n")
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            for (i in 1 until lines.size) {
                val line = lines[i].trim()
                if (line.isEmpty()) continue
                val parts = line.split(",")
                if (parts.size >= 7) {
                    try {
                        val title = parts[1]
                        val amount = parts[2].toDoubleOrNull() ?: 0.0
                        val type = if (parts[3].uppercase() == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE
                        val category = parts[4]
                        val paymentMethod = parts[5]
                        val dateText = parts[6]
                        val timestamp = try {
                            sdf.parse(dateText)?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }
                        val notes = if (parts.size >= 8) parts[7] else ""
                        val tags = if (parts.size >= 9) parts[8].replace(";", ",") else ""
                        val location = if (parts.size >= 10) parts[9] else ""

                        val tx = Transaction(
                            title = title,
                            amount = amount,
                            timestamp = timestamp,
                            category = category,
                            paymentMethod = paymentMethod,

                            notes = notes,
                            type = type,
                            tags = tags,
                            location = location
                        )
                        repository.insertTransaction(tx)
                        count++
                    } catch (e: Exception) {
                        // skip corrupt line
                    }
                }
            }
            onComplete(count)
        }
    }

    class Factory(
        private val repository: FinanceRepository,
        private val geminiService: GeminiService
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FinanceViewModel(repository, geminiService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    fun addBill(bill: com.example.model.Bill) {
        viewModelScope.launch { repository.insertBill(bill) }
    }
    fun updateBill(bill: com.example.model.Bill) {
        viewModelScope.launch { repository.updateBill(bill) }
    }
    fun deleteBill(bill: com.example.model.Bill) {
        viewModelScope.launch { repository.deleteBill(bill) }
    }
    fun addRecurringTransaction(txn: com.example.model.RecurringTransaction) {
        viewModelScope.launch { repository.insertRecurringTransaction(txn) }
    }
    fun updateRecurringTransaction(txn: com.example.model.RecurringTransaction) {
        viewModelScope.launch { repository.updateRecurringTransaction(txn) }
    }
    fun deleteRecurringTransaction(txn: com.example.model.RecurringTransaction) {
        viewModelScope.launch { repository.deleteRecurringTransaction(txn) }
    }
}
