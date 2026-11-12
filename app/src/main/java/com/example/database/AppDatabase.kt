package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.Budget
import com.example.model.Category
import com.example.model.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Transaction::class, Category::class, Budget::class, com.example.model.Account::class, com.example.model.Bill::class, com.example.model.RecurringTransaction::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun accountDao(): AccountDao
    abstract fun billDao(): BillDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_pro_db"
                )
                .addCallback(AppDatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateCategories(database.categoryDao())
                }
            }
        }

        suspend fun populateCategories(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                // Income Categories
                Category(name = "Salary", iconName = "work", colorHex = "#4CAF50", isIncome = true),
                Category(name = "Business", iconName = "store", colorHex = "#009688", isIncome = true),
                Category(name = "Freelancing", iconName = "laptop", colorHex = "#8BC34A", isIncome = true),
                Category(name = "Bonus", iconName = "card_membership", colorHex = "#FFD700", isIncome = true),
                Category(name = "Interest", iconName = "trending_up", colorHex = "#00BCD4", isIncome = true),
                Category(name = "Cashback", iconName = "monetization_on", colorHex = "#E91E63", isIncome = true),
                Category(name = "Gift", iconName = "redeem", colorHex = "#FF4081", isIncome = true),
                Category(name = "Refund", iconName = "settings_backup_restore", colorHex = "#9C27B0", isIncome = true),
                Category(name = "Rental Income", iconName = "home", colorHex = "#795548", isIncome = true),
                Category(name = "Investments", iconName = "show_chart", colorHex = "#3F51B5", isIncome = true),
                Category(name = "Other", iconName = "more_horiz", colorHex = "#9E9E9E", isIncome = true),

                // Expense Categories
                Category(name = "Food", iconName = "restaurant", colorHex = "#FF5722", isIncome = false),
                Category(name = "Groceries", iconName = "shopping_cart", colorHex = "#FF9800", isIncome = false),
                Category(name = "Fuel", iconName = "local_gas_station", colorHex = "#FFEB3B", isIncome = false),
                Category(name = "Travel", iconName = "directions_car", colorHex = "#2196F3", isIncome = false),
                Category(name = "Shopping", iconName = "shopping_bag", colorHex = "#E91E63", isIncome = false),
                Category(name = "Medical", iconName = "medical_services", colorHex = "#F44336", isIncome = false),
                Category(name = "Education", iconName = "school", colorHex = "#9C27B0", isIncome = false),
                Category(name = "Rent", iconName = "home", colorHex = "#795548", isIncome = false),
                Category(name = "EMI", iconName = "credit_card", colorHex = "#E040FB", isIncome = false),
                Category(name = "Electricity", iconName = "electric_bolt", colorHex = "#FFC107", isIncome = false),
                Category(name = "Water Bill", iconName = "water_drop", colorHex = "#03A9F4", isIncome = false),
                Category(name = "Gas", iconName = "local_fire_department", colorHex = "#FF5722", isIncome = false),
                Category(name = "Internet", iconName = "wifi", colorHex = "#0288D1", isIncome = false),
                Category(name = "Mobile Recharge", iconName = "phone_android", colorHex = "#4CAF50", isIncome = false),
                Category(name = "Subscriptions", iconName = "subscriptions", colorHex = "#00E676", isIncome = false),
                Category(name = "Entertainment", iconName = "sports_esports", colorHex = "#673AB7", isIncome = false),
                Category(name = "Investment", iconName = "trending_up", colorHex = "#3F51B5", isIncome = false),
                Category(name = "Insurance", iconName = "verified_user", colorHex = "#607D8B", isIncome = false),
                Category(name = "Charity", iconName = "favorite", colorHex = "#F50057", isIncome = false),
                Category(name = "Personal Care", iconName = "face", colorHex = "#FF80AB", isIncome = false),
                Category(name = "Family", iconName = "groups", colorHex = "#81C784", isIncome = false),
                Category(name = "Others", iconName = "more_horiz", colorHex = "#9E9E9E", isIncome = false)
            )
            categoryDao.insertCategories(defaultCategories)
        }
    }
}
