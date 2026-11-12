package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AccountType
import com.example.model.Category
import com.example.model.TransactionType
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.viewmodel.FinanceViewModel
import com.example.viewmodel.TransactionFormState
import java.text.SimpleDateFormat
import java.util.*

/**
 * Reusable Composable form UI for creating or updating financial transactions.
 * Integrates directly with [FinanceViewModel.transactionFormState] for unidirectional data flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    viewModel: FinanceViewModel,
    transactionId: Int? = null,
    defaultType: TransactionType = TransactionType.EXPENSE,
    onNavigateBack: () -> Unit
) {
    // Initialize form state in ViewModel
    LaunchedEffect(transactionId, defaultType) {
        viewModel.initTransactionForm(transactionId, defaultType)
    }

    val formState by viewModel.transactionFormState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    val isEditing = formState.transactionId != null && formState.transactionId!! > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Transaction" else "Add Transaction",
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSansFamily,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.initTransactionForm(null, defaultType) }) {
                        Text("Reset", color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            TransactionFormContent(
                formState = formState,
                categories = categories,
                accounts = accounts.map { it.name },
                onTitleChange = viewModel::onFormTitleChange,
                onAmountChange = viewModel::onFormAmountChange,
                onDateChange = viewModel::onFormDateChange,
                onTypeChange = viewModel::onFormTypeChange,
                onCategoryChange = viewModel::onFormCategoryChange,
                onPaymentMethodChange = viewModel::onFormPaymentMethodChange,
                onToAccountChange = viewModel::onFormToAccountChange,
                onNotesChange = viewModel::onFormNotesChange,
                onTagsChange = viewModel::onFormTagsChange,
                onLocationChange = viewModel::onFormLocationChange,
                onRecurringChange = viewModel::onFormRecurringChange,
                onSubmit = {
                    viewModel.submitTransactionForm(onSuccess = onNavigateBack)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormContent(
    formState: TransactionFormState,
    categories: List<Category>,
    accounts: List<String>,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onToAccountChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onRecurringChange: (Boolean, String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showMoreCategoriesDialog by remember { mutableStateOf(false) }
    var showAdvancedFields by remember { mutableStateOf(false) }

    // Date Picker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = formState.dateMillis
    )

    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selected ->
                            // Preserve current time-of-day
                            val cal = Calendar.getInstance().apply { timeInMillis = formState.dateMillis }
                            val hour = cal.get(Calendar.HOUR_OF_DAY)
                            val minute = cal.get(Calendar.MINUTE)

                            val newCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = selected
                            }
                            val localCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, newCal.get(Calendar.YEAR))
                                set(Calendar.MONTH, newCal.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, newCal.get(Calendar.DAY_OF_MONTH))
                                set(Calendar.HOUR_OF_DAY, hour)
                                set(Calendar.MINUTE, minute)
                            }
                            onDateChange(localCal.timeInMillis)
                        }
                        showDatePickerDialog = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Transaction Type Segmented Row (Expense, Income, Transfer)
        TypeSelectorRow(
            selectedType = formState.type,
            onTypeSelected = onTypeChange
        )

        // 2. Amount Input Hero Card
        AmountInputField(
            amountText = formState.amountText,
            amountError = formState.amountError,
            type = formState.type,
            onAmountChange = onAmountChange
        )

        // 3. Date Selection Section
        DateSelectorSection(
            dateMillis = formState.dateMillis,
            onDateSelected = onDateChange,
            onOpenDatePicker = { showDatePickerDialog = true }
        )

        // 4. Category Selection Section (for Expense and Income)
        if (formState.type != TransactionType.TRANSFER) {
            CategorySelectorSection(
                selectedCategory = formState.category,
                type = formState.type,
                availableCategories = categories,
                onCategorySelected = onCategoryChange,
                onOpenMore = { showMoreCategoriesDialog = true }
            )
        }

        // 5. Account & Payment Method
        AccountSelectorSection(
            type = formState.type,
            paymentMethod = formState.paymentMethod,
            toAccount = formState.toAccount,
            accounts = accounts,
            onPaymentMethodChange = onPaymentMethodChange,
            onToAccountChange = onToAccountChange
        )

        // 6. Title / Description Input
        OutlinedTextField(
            value = formState.title,
            onValueChange = onTitleChange,
            label = { Text("Description / Merchant") },
            placeholder = {
                Text(
                    when (formState.type) {
                        TransactionType.EXPENSE -> "e.g. Grocery Store, Coffee, Electricity Bill"
                        TransactionType.INCOME -> "e.g. Monthly Salary, Freelance Project"
                        TransactionType.TRANSFER -> "e.g. Savings Allocation, ATM Withdrawal"
                    }
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (formState.title.isNotBlank()) {
                    IconButton(onClick = { onTitleChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            isError = formState.titleError != null,
            supportingText = formState.titleError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("transaction_title_input"),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        // 7. Advanced Details Toggle (Notes, Tags, Location, Recurring)
        TextButton(
            onClick = { showAdvancedFields = !showAdvancedFields },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = if (showAdvancedFields) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (showAdvancedFields) "Fewer Options" else "More Details (Notes, Tags, Recurring)")
        }

        AnimatedVisibility(visible = showAdvancedFields) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Notes Input
                OutlinedTextField(
                    value = formState.notes,
                    onValueChange = onNotesChange,
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Add any extra notes or transaction details...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                // Tags Input
                OutlinedTextField(
                    value = formState.tags,
                    onValueChange = onTagsChange,
                    label = { Text("Tags (comma separated)") },
                    placeholder = { Text("e.g. dining, client, tax-deductible") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                // Location Input (only for Expense/Income)
                if (formState.type != TransactionType.TRANSFER) {
                    OutlinedTextField(
                        value = formState.location,
                        onValueChange = onLocationChange,
                        label = { Text("Location (Optional)") },
                        placeholder = { Text("e.g. City Mall, Starbucks Downtown") },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }

                // Recurring Transaction Switch
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Repeat Transaction",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Automatically log this recurring payment",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = formState.isRecurring,
                                onCheckedChange = { onRecurringChange(it, formState.recurrencePeriod) }
                            )
                        }

                        if (formState.isRecurring) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("WEEKLY", "MONTHLY", "YEARLY").forEach { period ->
                                    FilterChip(
                                        selected = formState.recurrencePeriod == period,
                                        onClick = { onRecurringChange(true, period) },
                                        label = { Text(period.lowercase().replaceFirstChar { it.uppercase() }) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 8. Save / Submit Button
        Button(
            onClick = onSubmit,
            enabled = !formState.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("save_transaction_button"),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = when (formState.type) {
                    TransactionType.EXPENSE -> Color(0xFFEF4444)
                    TransactionType.INCOME -> Color(0xFF10B981)
                    TransactionType.TRANSFER -> Color(0xFF3B82F6)
                }
            )
        ) {
            if (formState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (formState.transactionId != null && formState.transactionId > 0) "Update Transaction" else "Save Transaction",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlusJakartaSansFamily
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Modal Dialog for All Categories
    if (showMoreCategoriesDialog) {
        val filtered = categories.filter { it.isIncome == (formState.type == TransactionType.INCOME) }
        AlertDialog(
            onDismissRequest = { showMoreCategoriesDialog = false },
            title = { Text("Select Category") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filtered.forEach { cat ->
                        Surface(
                            onClick = {
                                onCategoryChange(cat.name)
                                showMoreCategoriesDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (cat.name.equals(formState.category, ignoreCase = true)) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(getCategoryColor(cat.name).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(cat.name),
                                        contentDescription = null,
                                        tint = getCategoryColor(cat.name),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (cat.name.equals(formState.category, ignoreCase = true)) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMoreCategoriesDialog = false }) { Text("Close") }
            }
        )
    }
}

// ----------------------------------------------------
// SUB-COMPONENTS
// ----------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeSelectorRow(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = selectedType == TransactionType.EXPENSE,
            onClick = { onTypeSelected(TransactionType.EXPENSE) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = Color(0xFFFEE2E2),
                activeContentColor = Color(0xFFDC2626)
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingDown, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Expense", fontWeight = FontWeight.Bold)
            }
        }
        SegmentedButton(
            selected = selectedType == TransactionType.INCOME,
            onClick = { onTypeSelected(TransactionType.INCOME) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = Color(0xFFD1FAE5),
                activeContentColor = Color(0xFF059669)
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Income", fontWeight = FontWeight.Bold)
            }
        }
        SegmentedButton(
            selected = selectedType == TransactionType.TRANSFER,
            onClick = { onTypeSelected(TransactionType.TRANSFER) },
            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = Color(0xFFE0F2FE),
                activeContentColor = Color(0xFF0284C7)
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Transfer", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AmountInputField(
    amountText: String,
    amountError: String?,
    type: TransactionType,
    onAmountChange: (String) -> Unit
) {
    val accentColor = when (type) {
        TransactionType.EXPENSE -> Color(0xFFEF4444)
        TransactionType.INCOME -> Color(0xFF10B981)
        TransactionType.TRANSFER -> Color(0xFF3B82F6)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.5.dp,
            if (amountError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Enter Amount",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Currency Symbol Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₹",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                // Amount Text Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = onAmountChange,
                    placeholder = { Text("0.00", fontSize = 28.sp, color = Color.Gray.copy(alpha = 0.5f)) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("transaction_amount_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSansFamily
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent
                    )
                )

                if (amountText.isNotEmpty()) {
                    IconButton(onClick = { onAmountChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear amount", tint = Color.Gray)
                    }
                }
            }

            if (amountError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = amountError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Quick Amount Presets
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(100, 500, 1000, 5000).forEach { inc ->
                    SuggestionChip(
                        onClick = {
                            val current = amountText.toDoubleOrNull() ?: 0.0
                            val updated = current + inc
                            onAmountChange(if (updated % 1.0 == 0.0) updated.toLong().toString() else updated.toString())
                        },
                        label = { Text("+₹$inc", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DateSelectorSection(
    dateMillis: Long,
    onDateSelected: (Long) -> Unit,
    onOpenDatePicker: () -> Unit
) {
    val sdfDate = remember { SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()) }
    val isToday = remember(dateMillis) {
        val today = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = dateMillis }
        today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    val isYesterday = remember(dateMillis) {
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val target = Calendar.getInstance().apply { timeInMillis = dateMillis }
        yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    val dateLabel = when {
        isToday -> "Today • ${sdfDate.format(Date(dateMillis))}"
        isYesterday -> "Yesterday • ${sdfDate.format(Date(dateMillis))}"
        else -> sdfDate.format(Date(dateMillis))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_date_picker_button")
            .clickable(onClick = onOpenDatePicker),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Date",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Date of Transaction",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = PlusJakartaSansFamily
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.EditCalendar,
                    contentDescription = "Change Date",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = isToday,
                    onClick = { onDateSelected(System.currentTimeMillis()) },
                    label = { Text("Today") }
                )
                FilterChip(
                    selected = isYesterday,
                    onClick = {
                        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                        onDateSelected(cal.timeInMillis)
                    },
                    label = { Text("Yesterday") }
                )
                AssistChip(
                    onClick = onOpenDatePicker,
                    label = { Text("Custom Date...") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }
    }
}

@Composable
fun CategorySelectorSection(
    selectedCategory: String,
    type: TransactionType,
    availableCategories: List<Category>,
    onCategorySelected: (String) -> Unit,
    onOpenMore: () -> Unit
) {
    // Top common categories based on type
    val defaultExpenseCategories = listOf("Food", "Shopping", "Transport", "Bills", "Groceries", "Entertainment", "Health", "Education")
    val defaultIncomeCategories = listOf("Salary", "Freelance", "Investment", "Business", "Gifts", "Rental")

    val quickList = if (type == TransactionType.INCOME) defaultIncomeCategories else defaultExpenseCategories

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = PlusJakartaSansFamily
            )
            TextButton(onClick = onOpenMore) {
                Text("All Categories (${availableCategories.size})", style = MaterialTheme.typography.labelMedium)
            }
        }

        // Horizontal Category Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(quickList) { catName ->
                val isSelected = catName.equals(selectedCategory, ignoreCase = true)
                val catColor = getCategoryColor(catName)
                val catIcon = getCategoryIcon(catName)

                Surface(
                    onClick = { onCategorySelected(catName) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.testTag("category_chip_${catName.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(catColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = catIcon,
                                contentDescription = null,
                                tint = catColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = catName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelectorSection(
    type: TransactionType,
    paymentMethod: String,
    toAccount: String,
    accounts: List<String>,
    onPaymentMethodChange: (String) -> Unit,
    onToAccountChange: (String) -> Unit
) {
    val accountOptions = if (accounts.isEmpty()) listOf("Cash", "Bank", "Wallet", "Card") else accounts

    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    if (type == TransactionType.TRANSFER) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // From Account Dropdown
            Box(modifier = Modifier.weight(1f)) {
                ExposedDropdownMenuBox(
                    expanded = fromExpanded,
                    onExpandedChange = { fromExpanded = !fromExpanded }
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = fromExpanded,
                        onDismissRequest = { fromExpanded = false }
                    ) {
                        accountOptions.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc) },
                                onClick = {
                                    onPaymentMethodChange(acc)
                                    fromExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // To Account Dropdown
            Box(modifier = Modifier.weight(1f)) {
                ExposedDropdownMenuBox(
                    expanded = toExpanded,
                    onExpandedChange = { toExpanded = !toExpanded }
                ) {
                    OutlinedTextField(
                        value = toAccount,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = toExpanded,
                        onDismissRequest = { toExpanded = false }
                    ) {
                        accountOptions.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc) },
                                onClick = {
                                    onToAccountChange(acc)
                                    toExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Single Account Selection
        var expanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (type == TransactionType.INCOME) "Deposit To Account" else "Payment Account") },
                    leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    accountOptions.forEach { acc ->
                        DropdownMenuItem(
                            text = { Text(acc) },
                            onClick = {
                                onPaymentMethodChange(acc)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// HELPERS
// ----------------------------------------------------

fun getCategoryIcon(name: String): ImageVector {
    return when (name.lowercase().trim()) {
        "food", "food & dining", "restaurant", "cafe", "coffee" -> Icons.Default.Restaurant
        "groceries", "supermarket" -> Icons.Default.LocalGroceryStore
        "shopping", "clothing" -> Icons.Default.ShoppingBag
        "transport", "transit", "fuel", "gas", "taxi" -> Icons.Default.DirectionsCar
        "bills", "utilities", "electricity", "water" -> Icons.AutoMirrored.Filled.ReceiptLong
        "entertainment", "movies", "games" -> Icons.Default.SportsEsports
        "health", "medical", "pharmacy" -> Icons.Default.LocalHospital
        "education", "books", "tuition" -> Icons.Default.School
        "housing", "rent", "mortgage" -> Icons.Default.Home
        "salary", "paycheck" -> Icons.Default.AttachMoney
        "freelance", "side hustle" -> Icons.Default.Work
        "investment", "stocks", "dividends" -> Icons.Default.TrendingUp
        "business" -> Icons.Default.BusinessCenter
        "gifts", "donation" -> Icons.Default.Redeem
        "transfer" -> Icons.Default.SwapHoriz
        else -> Icons.Default.Category
    }
}

fun getCategoryColor(name: String): Color {
    return when (name.lowercase().trim()) {
        "food", "food & dining" -> Color(0xFFF59E0B) // Amber
        "groceries" -> Color(0xFF10B981) // Emerald
        "shopping" -> Color(0xFFEC4899) // Pink
        "transport" -> Color(0xFF0284C7) // Sky Blue
        "bills", "utilities" -> Color(0xFFEF4444) // Red
        "entertainment" -> Color(0xFF8B5CF6) // Violet
        "health" -> Color(0xFF14B8A6) // Teal
        "education" -> Color(0xFF6366F1) // Indigo
        "salary" -> Color(0xFF10B981) // Green
        "freelance" -> Color(0xFF38BDF8) // Light Blue
        "investment" -> Color(0xFFFBBF24) // Gold
        "business" -> Color(0xFF475569) // Slate
        "transfer" -> Color(0xFF3B82F6) // Royal Blue
        else -> Color(0xFF64748B) // Slate
    }
}
