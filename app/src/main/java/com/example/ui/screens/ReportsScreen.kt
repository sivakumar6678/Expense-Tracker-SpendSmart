package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import com.example.ui.components.CategorySpendingDashboard
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.viewmodel.FinanceViewModel
import com.example.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTransactionDetails: ((Int) -> Unit)? = null
) {
    val transactions by viewModel.transactions.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
    val incomes = transactions.filter { it.type == TransactionType.INCOME }

    val totalExpense = expenses.sumOf { it.amount }
    val totalIncome = incomes.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Reports", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
        ) {
            // Tabs selectors
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "Pie Chart") },
                    text = { Text("Categories", style = MaterialTheme.typography.bodySmall) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Bar Chart") },
                    text = { Text("Income/Exp", style = MaterialTheme.typography.bodySmall) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = "Line Chart") },
                    text = { Text("Trends", style = MaterialTheme.typography.bodySmall) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary mini metrics card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Income", style = MaterialTheme.typography.bodySmall)
                            Text(CurrencyUtils.formatIndianCurrency(totalIncome), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Expenses", style = MaterialTheme.typography.bodySmall)
                            Text(CurrencyUtils.formatIndianCurrency(totalExpense), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFF44336))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Net Savings", style = MaterialTheme.typography.bodySmall)
                            Text(CurrencyUtils.formatIndianCurrency(totalIncome - totalExpense), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Render Chart based on selected tab
                when (selectedTab) {
                    0 -> CategorySpendingDashboard(
                        transactions = transactions,
                        onNavigateToTransactionDetails = onNavigateToTransactionDetails
                    )
                    1 -> IncomeExpenseBarCard(totalIncome = totalIncome, totalExpense = totalExpense)
                    2 -> SpendingTrendLineCard(expenses = expenses)
                }
            }
        }
        }
    }
}

@Composable
fun CategoriesPieCard(expenses: List<Transaction>, totalExpense: Double) {
    val categorySum = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val chartColors = listOf(
        Color(0xFFFF5722), // Orange
        Color(0xFFE91E63), // Pink
        Color(0xFF2196F3), // Blue
        Color(0xFFFFEB3B), // Yellow
        Color(0xFF9C27B0), // Purple
        Color(0xFF009688), // Teal
        Color(0xFF4CAF50), // Green
        Color(0xFF9E9E9E)  // Grey for remaining
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Category Spending Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (expenses.isEmpty()) {
                Text("No data available to display chart", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                // Canvas Pie Chart drawing
                Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                    val backgroundColor = MaterialTheme.colorScheme.surface
                    Canvas(modifier = Modifier.size(200.dp)) {
                        var startAngle = 0f
                        categorySum.forEachIndexed { index, (name, amount) ->
                            val sweepAngle = ((amount / totalExpense) * 360f).toFloat()
                            val color = chartColors.getOrElse(index) { Color.LightGray }
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                size = size,
                                style = Stroke(width = 40.dp.toPx())
                            )
                            startAngle += sweepAngle
                        }
                    }

                    // Inside donut hole label
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Spend", style = MaterialTheme.typography.labelSmall)
                        Text(CurrencyUtils.formatIndianCurrency(totalExpense), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Legend List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categorySum.forEachIndexed { index, (name, amount) ->
                        val color = chartColors.getOrElse(index) { Color.LightGray }
                        val percentage = (amount / totalExpense) * 100
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(name, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                text = "${CurrencyUtils.formatIndianCurrency(amount)} (${String.format("%.1f", percentage)}%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        }
}

@Composable
fun IncomeExpenseBarCard(totalIncome: Double, totalExpense: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Income vs Expenses Ratio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (totalIncome == 0.0 && totalExpense == 0.0) {
                Text("No data to display comparison", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val maxAmount = maxOf(totalIncome, totalExpense)
                val incomeRatio = if (maxAmount > 0) (totalIncome / maxAmount).toFloat() else 0f
                val expenseRatio = if (maxAmount > 0) (totalExpense / maxAmount).toFloat() else 0f

                val barColorIncome = Color(0xFF4CAF50)
                val barColorExpense = Color(0xFFF44336)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        val barWidth = canvasWidth * 0.25f
                        val spacing = canvasWidth * 0.15f

                        // Draw Grid lines
                        for (i in 0..3) {
                            val y = canvasHeight * (i / 3f)
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.2f),
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Draw Income Bar
                        val incomeBarHeight = canvasHeight * incomeRatio
                        drawRect(
                            color = barColorIncome,
                            topLeft = Offset((canvasWidth / 2f) - barWidth - (spacing / 2f), canvasHeight - incomeBarHeight),
                            size = Size(barWidth, incomeBarHeight)
                        )

                        // Draw Expense Bar
                        val expenseBarHeight = canvasHeight * expenseRatio
                        drawRect(
                            color = barColorExpense,
                            topLeft = Offset((canvasWidth / 2f) + (spacing / 2f), canvasHeight - expenseBarHeight),
                            size = Size(barWidth, expenseBarHeight)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).background(barColorIncome, RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Income (${CurrencyUtils.formatIndianCurrency(totalIncome)})", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).background(barColorExpense, RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Expenses (${CurrencyUtils.formatIndianCurrency(totalExpense)})", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        }
}

@Composable
fun SpendingTrendLineCard(expenses: List<Transaction>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Spending Trends (Last 7 transactions)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            val points = expenses.take(7).reversed().map { it.amount.toFloat() }

            if (points.size < 2) {
                Text("Log at least 2 expense entries to trace spending trends.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val lineColor = MaterialTheme.colorScheme.primary
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val width = size.width
                    val height = size.height

                    val maxPoint = points.maxOrNull() ?: 1f
                    val minPoint = points.minOrNull() ?: 0f
                    val range = if (maxPoint - minPoint == 0f) 1f else maxPoint - minPoint

                    val stepX = width / (points.size - 1)

                    // Draw Grid
                    for (i in 0..2) {
                        val y = height * (i / 2f)
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.15f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Draw bezier line
                    val pointsOffset = points.mapIndexed { idx, value ->
                        val x = idx * stepX
                        val normalizedY = (value - minPoint) / range
                        val y = height - (normalizedY * height * 0.8f) - (height * 0.1f)
                        Offset(x, y)
                    }

                    for (i in 0 until pointsOffset.size - 1) {
                        val p1 = pointsOffset[i]
                        val p2 = pointsOffset[i + 1]
                        drawLine(
                            color = lineColor,
                            start = p1,
                            end = p2,
                            strokeWidth = 3.dp.toPx()
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 4.dp.toPx(),
                            center = p1
                        )
                    }
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = pointsOffset.last()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Values range from ${CurrencyUtils.formatIndianCurrency((points.minOrNull() ?: 0f).toDouble())} to ${CurrencyUtils.formatIndianCurrency((points.maxOrNull() ?: 0f).toDouble())}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        }
}
