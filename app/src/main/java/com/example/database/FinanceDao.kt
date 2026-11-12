package com.example.database

import androidx.room.*
import com.example.model.Budget
import com.example.model.Category
import com.example.model.Transaction
import com.example.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id IN (:ids)")
    suspend fun deleteTransactions(ids: List<Int>)

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE isRecurring = 1")
    suspend fun getRecurringTransactions(): List<Transaction>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncomeFlow(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpenseFlow(): Flow<Double?>

    @Query("SELECT * FROM transactions WHERE title LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchTransactions(query: String): Flow<List<Transaction>>
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategoriesFlow(): Flow<List<Category>>

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Delete
    suspend fun deleteCategory(category: Category)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun getAllBudgetsFlow(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    fun getBudgetsByMonth(monthYear: String): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget): Long

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("DELETE FROM budgets")
    suspend fun clearAllBudgets()
}

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY dueDate ASC")
    fun getAllBills(): Flow<List<com.example.model.Bill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: com.example.model.Bill)

    @Update
    suspend fun updateBill(bill: com.example.model.Bill)

    @Delete
    suspend fun deleteBill(bill: com.example.model.Bill)
}

@Dao
interface RecurringTransactionDao {
    @Query("SELECT * FROM recurring_transactions ORDER BY nextDate ASC")
    fun getAllRecurringTransactions(): Flow<List<com.example.model.RecurringTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTransaction(txn: com.example.model.RecurringTransaction)

    @Update
    suspend fun updateRecurringTransaction(txn: com.example.model.RecurringTransaction)

    @Delete
    suspend fun deleteRecurringTransaction(txn: com.example.model.RecurringTransaction)
}
