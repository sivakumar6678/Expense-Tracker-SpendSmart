package com.example.di

import android.content.Context
import com.example.database.AppDatabase
import com.example.repository.FinanceRepository
import com.example.services.GeminiService
import com.example.utils.UserPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class FinanceContainer(private val context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context, applicationScope)
    }

    val repository: FinanceRepository by lazy {
        FinanceRepository(
            transactionDao = database.transactionDao(),
            categoryDao = database.categoryDao(),
            budgetDao = database.budgetDao(),
            accountDao = database.accountDao(),
            billDao = database.billDao(),
            recurringTransactionDao = database.recurringTransactionDao()
        )
    }

    val preferencesManager: UserPreferencesManager by lazy {
        UserPreferencesManager(context)
    }

    val geminiService: GeminiService by lazy {
        GeminiService()
    }
}
