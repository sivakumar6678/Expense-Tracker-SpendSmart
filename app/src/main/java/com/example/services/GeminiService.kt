package com.example.services

import com.example.BuildConfig
import com.example.model.FinancialInsight
import com.example.model.InsightType
import com.example.model.Transaction
import com.example.model.TransactionType
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiService {
    suspend fun analyzeFinancials(
        transactions: List<Transaction>,
        monthlyGoal: Double,
        savingsGoal: Double
    ): List<FinancialInsight> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return getOfflineInsights(transactions, monthlyGoal, savingsGoal)
        }

        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val incomes = transactions.filter { it.type == TransactionType.INCOME }

        val totalExpense = expenses.sumOf { it.amount }
        val totalIncome = incomes.sumOf { it.amount }

        val categoryGroups = expenses.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }
        val topCategoriesText = categoryGroups.entries.sortedByDescending { it.value }.take(3).joinToString { "${it.key}: ₹${it.value}" }

        val prompt = """
            You are an advanced financial advisor AI for our App 'Expense Tracker Pro'.
            Analyze the following real-time user financial data and output exactly 4 highly specific financial insights.
            
            Financial Summary:
            - Monthly Expenses Budget Limit: ₹monthlyGoal
            - Monthly Savings Target Goal: ₹savingsGoal
            - Actual Total Expenses: ₹totalExpense
            - Actual Total Income: ₹totalIncome
            - Top Expense Categories: ₹topCategoriesText
            
            Recent Transactions Details:
            ${expenses.take(10).joinToString("\n") { "- ${it.title} in ${it.category}: ₹${it.amount} on ${it.timestamp}" }}
            
            Based on this information, provide exactly 4 items in standard format, one per line.
            Each line must follow this format:
            TYPE|TITLE|DESCRIPTION
            
            Where:
            - TYPE must be exactly one of: TIP, ALERT, RECOMMENDATION, PREDICTION
            - TITLE is a short 3-5 word title
            - DESCRIPTION is a single concise sentence offering specific actionable advice or feedback about their real categories/spending.
            
            Output ONLY those 4 lines. No other text, numbers, or introductions.
        """.trimIndent()

        return try {
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(temperature = 0.2f)
            )
            val response = GeminiClient.apiService.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            parseInsightsResponse(responseText, transactions, monthlyGoal, savingsGoal)
        } catch (e: Exception) {
            getOfflineInsights(transactions, monthlyGoal, savingsGoal)
        }
    }

    private fun parseInsightsResponse(
        text: String,
        transactions: List<Transaction>,
        monthlyGoal: Double,
        savingsGoal: Double
    ): List<FinancialInsight> {
        val lines = text.split("\n").filter { it.contains("|") }
        if (lines.size < 2) {
            return getOfflineInsights(transactions, monthlyGoal, savingsGoal)
        }

        return lines.mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size >= 3) {
                val typeStr = parts[0].trim()
                val title = parts[1].trim()
                val desc = parts[2].trim()

                val type = when (typeStr) {
                    "ALERT" -> InsightType.ALERT
                    "RECOMMENDATION" -> InsightType.RECOMMENDATION
                    "PREDICTION" -> InsightType.PREDICTION
                    else -> InsightType.TIP
                }
                FinancialInsight(title, desc, type)
            } else {
                null
            }
        }
    }

    // Fallback/offline engine in case of missing API key or offline mode
    fun getOfflineInsights(
        transactions: List<Transaction>,
        monthlyGoal: Double,
        savingsGoal: Double
    ): List<FinancialInsight> {
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val incomes = transactions.filter { it.type == TransactionType.INCOME }
        val transfers = transactions.filter { it.type == TransactionType.TRANSFER }
        val totalExpense = expenses.sumOf { it.amount }
        val foodExpense = expenses.filter { it.category.equals("Food", ignoreCase = true) }.sumOf { it.amount }
        
        val list = mutableListOf<FinancialInsight>()
        
        list.add(
            FinancialInsight(
                title = "Smart Food Budgeting",
                description = "You spent ₹${String.format("%.2f", foodExpense)} on Food. Preparing meals at home can save up to 30% of your weekly costs.",
                type = InsightType.TIP
            )
        )
        
        // Find if user has a temple fund or any savings goal
        val lastGoalTransfer = transfers.lastOrNull { it.toAccount != null && it.toAccount.contains("Fund", true) }
        if (lastGoalTransfer != null) {
            list.add(
                FinancialInsight(
                    title = "Savings Goal Progress",
                    description = "You saved ₹${String.format("%.2f", lastGoalTransfer.amount)} towards ${lastGoalTransfer.toAccount} recently. Great job!",
                    type = InsightType.RECOMMENDATION
                )
            )
        } else {
            list.add(
                FinancialInsight(
                    title = "Start a Savings Goal",
                    description = "Set up a Savings Goal in Profile to start tracking funds for special purposes like Vacations or Gadgets.",
                    type = InsightType.RECOMMENDATION
                )
            )
        }

        if (totalExpense > monthlyGoal) {
            list.add(
                FinancialInsight(
                    title = "Budget Threshold Exceeded",
                    description = "Your total monthly spending (₹${String.format("%.2f", totalExpense)}) has surpassed your set limit of ₹${String.format("%.2f", monthlyGoal)}!",
                    type = InsightType.ALERT
                )
            )
        } else {
            val progress = if (monthlyGoal > 0) (totalExpense / monthlyGoal) * 100 else 0.0
            list.add(
                FinancialInsight(
                    title = "Healthy Spending Progress",
                    description = "You have utilized ${String.format("%.1f", progress)}% of your monthly expense limit of ₹${String.format("%.2f", monthlyGoal)}.",
                    type = InsightType.ALERT
                )
            )
        }

        val currentSavings = incomes.sumOf { it.amount } - totalExpense
        if (currentSavings >= savingsGoal) {
            list.add(
                FinancialInsight(
                    title = "Savings Goal Completed!",
                    description = "Congratulations! Your savings (₹${String.format("%.2f", currentSavings)}) have successfully reached your target of ₹${String.format("%.2f", savingsGoal)}.",
                    type = InsightType.PREDICTION
                )
            )
        } else {
            val remaining = savingsGoal - currentSavings
            list.add(
                FinancialInsight(
                    title = "Savings Target Forecast",
                    description = "You need ₹${String.format("%.2f", remaining)} more to meet your target. Keep spending optimized to reach it by month's end.",
                    type = InsightType.PREDICTION
                )
            )
        }

        return list
    }
}
