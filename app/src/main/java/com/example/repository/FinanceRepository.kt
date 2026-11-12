package com.example.repository

import com.example.database.AccountDao
import com.example.database.BillDao
import com.example.database.RecurringTransactionDao
import com.example.database.BudgetDao
import com.example.database.CategoryDao
import com.example.database.TransactionDao
import com.example.model.Account
import com.example.model.Budget
import com.example.model.Category
import com.example.model.Transaction
import com.example.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val accountDao: AccountDao,
    private val billDao: BillDao,
    private val recurringTransactionDao: RecurringTransactionDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategoriesFlow()
    val allBudgets: Flow<List<Budget>> = budgetDao.getAllBudgetsFlow()
        val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()
    val allBills: Flow<List<com.example.model.Bill>> = billDao.getAllBills()
    val allRecurringTransactions: Flow<List<com.example.model.RecurringTransaction>> = recurringTransactionDao.getAllRecurringTransactions()

    val totalIncome: Flow<Double> = transactionDao.getTotalIncomeFlow().map { it ?: 0.0 }
    val totalExpense: Flow<Double> = transactionDao.getTotalExpenseFlow().map { it ?: 0.0 }

    suspend fun getTransactionById(id: Int): Transaction? {
        return transactionDao.getTransactionById(id)
    }

    suspend fun insertTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactions(ids: List<Int>) {
        transactionDao.deleteTransactions(ids)
    }

    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(type)
    }

    val favoriteTransactions: Flow<List<Transaction>> = transactionDao.getFavoriteTransactions()

    suspend fun getRecurringTransactions(): List<Transaction> {
        return transactionDao.getRecurringTransactions()
    }

    fun searchTransactions(query: String): Flow<List<Transaction>> {
        return transactionDao.searchTransactions(query)
    }

    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    suspend fun getAllCategories(): List<Category> {
        return categoryDao.getAllCategories()
    }

    fun getBudgetsByMonth(monthYear: String): Flow<List<Budget>> {
        return budgetDao.getBudgetsByMonth(monthYear)
    }

    suspend fun insertBudget(budget: Budget): Long {
        return budgetDao.insertBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun clearAllBudgets() {
        budgetDao.clearAllBudgets()
    }

    suspend fun insertAccount(account: Account) {
        accountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(account)
    }

    suspend fun deleteAccount(account: Account) {
        accountDao.deleteAccount(account)
    }

    suspend fun insertBill(bill: com.example.model.Bill) = billDao.insertBill(bill)
    suspend fun updateBill(bill: com.example.model.Bill) = billDao.updateBill(bill)
    suspend fun deleteBill(bill: com.example.model.Bill) = billDao.deleteBill(bill)

    suspend fun insertRecurringTransaction(txn: com.example.model.RecurringTransaction) = recurringTransactionDao.insertRecurringTransaction(txn)
    suspend fun updateRecurringTransaction(txn: com.example.model.RecurringTransaction) = recurringTransactionDao.updateRecurringTransaction(txn)
    suspend fun deleteRecurringTransaction(txn: com.example.model.RecurringTransaction) = recurringTransactionDao.deleteRecurringTransaction(txn)
}
