package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.ui.screens.getCategoryColor
import com.example.ui.screens.getCategoryIcon
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.atan2
import kotlin.math.sqrt

enum class TimePeriodScope {
    WEEKLY,
    MONTHLY,
    YEARLY
}

data class CategorySpendSlice(
    val categoryName: String,
    val amount: Double,
    val percentage: Float,
    val color: Color,
    val startAngle: Float,
    val sweepAngle: Float
)

/**
 * Interactive Dashboard for visualizing spending trends by category.
 * Features:
 * - Time period selector (Weekly, Monthly, Yearly) with previous/next navigation
 * - Interactive Donut Chart with animated arc sweeps and touch angle detection
 * - Center total spend and dynamic category selection details
 * - Interactive Legend with percentage breakdown
 * - Filtered transaction drill-down list for the selected category
 */
@Composable
fun CategorySpendingDashboard(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
    onNavigateToTransactionDetails: ((Int) -> Unit)? = null
) {
    var selectedScope by remember { mutableStateOf(TimePeriodScope.MONTHLY) }
    var periodOffset by remember { mutableIntStateOf(0) } // 0 = current, -1 = previous, etc.
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Reset selected category when time scope or offset changes
    LaunchedEffect(selectedScope, periodOffset) {
        selectedCategory = null
    }

    // Filter transactions within the selected time period
    val (periodLabel, filteredExpenses) = remember(transactions, selectedScope, periodOffset) {
        val calendar = Calendar.getInstance()

        when (selectedScope) {
            TimePeriodScope.WEEKLY -> {
                calendar.add(Calendar.WEEK_OF_YEAR, periodOffset)
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.add(Calendar.DAY_OF_WEEK, 6)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                val end = calendar.timeInMillis

                val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                val label = "${sdf.format(Date(start))} - ${sdf.format(Date(end))}"

                val list = transactions.filter {
                    it.type == TransactionType.EXPENSE && it.timestamp in start..end
                }
                label to list
            }
            TimePeriodScope.MONTHLY -> {
                calendar.add(Calendar.MONTH, periodOffset)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val start = calendar.timeInMillis

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                val end = calendar.timeInMillis

                val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                val label = sdf.format(Date(start))

                val list = transactions.filter {
                    it.type == TransactionType.EXPENSE && it.timestamp in start..end
                }
                label to list
            }
            TimePeriodScope.YEARLY -> {
                calendar.add(Calendar.YEAR, periodOffset)
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                val start = calendar.timeInMillis

                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                val end = calendar.timeInMillis

                val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
                val label = sdf.format(Date(start))

                val list = transactions.filter {
                    it.type == TransactionType.EXPENSE && it.timestamp in start..end
                }
                label to list
            }
        }
    }

    val totalExpense = remember(filteredExpenses) { filteredExpenses.sumOf { it.amount } }

    // Group expenses by category
    val categorySlices = remember(filteredExpenses, totalExpense) {
        if (totalExpense <= 0.0) emptyList()
        else {
            val grouped = filteredExpenses.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
                .toList()
                .sortedByDescending { it.second }

            var currentAngle = 0f
            grouped.map { (catName, amount) ->
                val sweep = ((amount / totalExpense) * 360.0).toFloat()
                val percentage = ((amount / totalExpense) * 100.0).toFloat()
                val slice = CategorySpendSlice(
                    categoryName = catName,
                    amount = amount,
                    percentage = percentage,
                    color = getCategoryColor(catName),
                    startAngle = currentAngle,
                    sweepAngle = sweep
                )
                currentAngle += sweep
                slice
            }
        }
    }

    // Drill-down transactions for selected category
    val drillDownTransactions = remember(filteredExpenses, selectedCategory) {
        if (selectedCategory == null) emptyList()
        else filteredExpenses.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Time Range Scope Selector & Period Navigator
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Scope Tabs (Weekly, Monthly, Yearly)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedScope == TimePeriodScope.WEEKLY,
                        onClick = {
                            selectedScope = TimePeriodScope.WEEKLY
                            periodOffset = 0
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) { Text("Weekly") }

                    SegmentedButton(
                        selected = selectedScope == TimePeriodScope.MONTHLY,
                        onClick = {
                            selectedScope = TimePeriodScope.MONTHLY
                            periodOffset = 0
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) { Text("Monthly") }

                    SegmentedButton(
                        selected = selectedScope == TimePeriodScope.YEARLY,
                        onClick = {
                            selectedScope = TimePeriodScope.YEARLY
                            periodOffset = 0
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) { Text("Yearly") }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Period Navigation (Previous < Label > Next)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { periodOffset -= 1 },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Period",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = periodLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PlusJakartaSansFamily
                        )
                        if (periodOffset != 0) {
                            TextButton(
                                onClick = { periodOffset = 0 },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Jump to Current", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    IconButton(
                        onClick = { periodOffset += 1 },
                        enabled = periodOffset < 0, // Prevent future empty dates if preferred, or allow
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Period",
                            tint = if (periodOffset < 0) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }

        // 2. Interactive Donut Chart Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spending by Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSansFamily
                    )

                    if (selectedCategory != null) {
                        Surface(
                            onClick = { selectedCategory = null },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Clear Selection", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (filteredExpenses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.outlineVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No expenses in this period",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Donut Canvas
                    InteractiveDonutChart(
                        slices = categorySlices,
                        totalExpense = totalExpense,
                        selectedCategory = selectedCategory,
                        onSliceSelected = { cat ->
                            selectedCategory = if (selectedCategory == cat) null else cat
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. Category Legend Breakdown
                    CategoryLegendBreakdown(
                        slices = categorySlices,
                        selectedCategory = selectedCategory,
                        onCategoryClick = { cat ->
                            selectedCategory = if (selectedCategory == cat) null else cat
                        }
                    )
                }
            }
        }

        // 4. Drill-Down Transaction List for Selected Category
        AnimatedVisibility(visible = selectedCategory != null && drillDownTransactions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(getCategoryColor(selectedCategory ?: "").copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(selectedCategory ?: ""),
                                    contentDescription = null,
                                    tint = getCategoryColor(selectedCategory ?: ""),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${selectedCategory} Breakdown (${drillDownTransactions.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = PlusJakartaSansFamily
                            )
                        }

                        Text(
                            text = CurrencyUtils.formatIndianCurrency(drillDownTransactions.sumOf { it.amount }),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        drillDownTransactions.forEach { tx ->
                            TransactionCardItem(
                                transaction = tx,
                                onClick = { onNavigateToTransactionDetails?.invoke(tx.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Native Jetpack Compose Canvas Donut Chart with touch angle detection.
 */
@Composable
fun InteractiveDonutChart(
    slices: List<CategorySpendSlice>,
    totalExpense: Double,
    selectedCategory: String?,
    onSliceSelected: (String) -> Unit
) {
    // Animation progress for smooth reveals
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "donut_animation"
    )

    val activeSlice = remember(slices, selectedCategory) {
        slices.find { it.categoryName.equals(selectedCategory, ignoreCase = true) }
    }

    Box(
        modifier = Modifier
            .size(230.dp)
            .testTag("donut_chart_container"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(230.dp)
                .pointerInput(slices) {
                    detectTapGestures { offset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val dx = offset.x - centerX
                        val dy = offset.y - centerY
                        val distance = sqrt(dx * dx + dy * dy)

                        val strokeWidthPx = 42.dp.toPx()
                        val minDim = kotlin.math.min(size.width, size.height).toFloat()
                        val outerRadius = minDim / 2f
                        val innerRadius = outerRadius - strokeWidthPx

                        if (distance in innerRadius..outerRadius) {
                            // Touch is inside the donut ring
                            val angleRad = atan2(dy, dx)
                            val angleDeg = (Math.toDegrees(angleRad.toDouble()).toFloat() + 360f) % 360f

                            // Slices start at -90 deg (top), so adjust touch angle
                            val normalizedTouchAngle = (angleDeg - 270f + 360f) % 360f

                            val clickedSlice = slices.find { slice ->
                                normalizedTouchAngle in slice.startAngle..(slice.startAngle + slice.sweepAngle)
                            }
                            if (clickedSlice != null) {
                                onSliceSelected(clickedSlice.categoryName)
                            }
                        } else if (distance < innerRadius) {
                            // Tap on center hole deselects
                            onSliceSelected("")
                        }
                    }
                }
        ) {
            val strokeWidth = 38.dp.toPx()
            val selectedStrokeWidth = 46.dp.toPx()
            val canvasSize = size.minDimension

            slices.forEach { slice ->
                val isSelected = slice.categoryName.equals(selectedCategory, ignoreCase = true)
                val currentStroke = if (isSelected) selectedStrokeWidth else strokeWidth

                // Offset drawing for stroke padding
                val padding = (selectedStrokeWidth - strokeWidth) / 2f
                val arcSize = Size(canvasSize - currentStroke, canvasSize - currentStroke)
                val topLeft = Offset(currentStroke / 2f, currentStroke / 2f)

                // Start at top (-90 degrees)
                val start = -90f + slice.startAngle
                val sweep = slice.sweepAngle * animationProgress

                drawArc(
                    color = if (selectedCategory == null || isSelected) slice.color else slice.color.copy(alpha = 0.35f),
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = currentStroke, cap = StrokeCap.Round)
                )
            }
        }

        // Center Hole Content (Total Spend or Selected Category)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            if (activeSlice != null) {
                Text(
                    text = activeSlice.categoryName,
                    style = MaterialTheme.typography.labelSmall,
                    color = activeSlice.color,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = CurrencyUtils.formatIndianCurrency(activeSlice.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlusJakartaSansFamily,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = String.format(Locale.getDefault(), "%.1f%% of total", activeSlice.percentage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            } else {
                Text(
                    text = "Total Spend",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyUtils.formatIndianCurrency(totalExpense),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlusJakartaSansFamily,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Tap slice to filter",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Category Legend showing color badge, name, percent bar, and currency amount.
 */
@Composable
fun CategoryLegendBreakdown(
    slices: List<CategorySpendSlice>,
    selectedCategory: String?,
    onCategoryClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        slices.forEach { slice ->
            val isSelected = slice.categoryName.equals(selectedCategory, ignoreCase = true)

            Surface(
                onClick = { onCategoryClick(slice.categoryName) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
                border = BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Icon with Color Indicator
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(slice.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(slice.categoryName),
                            contentDescription = slice.categoryName,
                            tint = slice.color,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Title & Progress Bar Column
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = slice.categoryName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f%%", slice.percentage),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Category proportional progress bar
                        LinearProgressIndicator(
                            progress = { slice.percentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = slice.color,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Category Amount
                    Text(
                        text = CurrencyUtils.formatIndianCurrency(slice.amount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSansFamily,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
