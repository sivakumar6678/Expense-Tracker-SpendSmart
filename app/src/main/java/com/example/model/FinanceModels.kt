package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}

enum class AccountType {
    CASH, BANK, SAVINGS, WALLET, CREDIT_CARD, INVESTMENT, CUSTOM
}

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: AccountType,
    val initialBalance: Double,
    val includeInAvailableBalance: Boolean
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val timestamp: Long,
    val category: String,
    val paymentMethod: String, // Treat as Account Name for Income/Expense/Transfer(From)
    val toAccount: String? = null, // Used for Transfer (To)
    val notes: String = "",
    val receiptUri: String? = null,
    val location: String? = null,
    val currency: String = "USD",
    val type: TransactionType,
    val tags: String = "",
    val isRecurring: Boolean = false,
    val recurrencePeriod: String? = null,
    val isFavorite: Boolean = false
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val isIncome: Boolean,
    val isCustom: Boolean = false
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryName: String,
    val amount: Double,
    val period: String,
    val monthYear: String
)

data class FinancialInsight(
    val title: String,
    val description: String,
    val type: InsightType,
    val amount: Double? = null
)

enum class InsightType {
    TIP,
    ALERT,
    RECOMMENDATION,
    PREDICTION
}

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val category: String,
    val dueDate: Long,
    val repeatType: String,
    val reminder: Boolean,
    val account: String,
    val status: String
)

@Entity(tableName = "recurring_transactions")
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val account: String,
    val repeatType: String,
    val nextDate: Long,
    val reminder: Boolean,
    val enabled: Boolean = true
)
