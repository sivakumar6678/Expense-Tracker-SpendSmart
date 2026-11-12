package com.example.navigation

object Screen {
    const val AUTH = "auth"
    const val DASHBOARD = "dashboard"
    const val TRANSACTIONS = "transactions"
    const val ADD_TRANSACTION = "add_transaction"
    const val EDIT_TRANSACTION = "edit_transaction/{transactionId}"
    const val TRANSACTION_DETAILS = "transaction_details/{transactionId}"
    fun transactionDetailsRoute(id: Int) = "transaction_details/$id"
    const val REPORTS = "reports"
    const val BUDGETS = "budgets"
    const val SETTINGS = "settings"
    const val MANAGE_ACCOUNTS = "manage_accounts"
    const val MANAGE_BILLS = "manage_bills"
    const val SAVINGS_GOALS = "savings_goals"

    fun editTransactionRoute(id: Int) = "edit_transaction/$id"
}
