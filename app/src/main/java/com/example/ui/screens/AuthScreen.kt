package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AuthUiState
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.FinanceViewModel
import com.example.model.AccountType

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    financeViewModel: FinanceViewModel,
    onAuthSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pinError by viewModel.pinError.collectAsState()
    var enteredPin by remember { mutableStateOf("") }
    var confirmPinMode by remember { mutableStateOf(false) }
    var firstPin by remember { mutableStateOf("") }
    var showInitialAccounts by remember { mutableStateOf(false) }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surface
        )
    )

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Authenticated) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Security Lock",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    )
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            val headerText = when (uiState) {
                is AuthUiState.SetupPin -> {
                    if (confirmPinMode) "Confirm Your PIN" else "Create a Secure PIN"
                }
                is AuthUiState.EnterPin -> "Welcome Back"
                else -> "Expense Tracker Pro"
            }

            val subtitleText = when (uiState) {
                is AuthUiState.SetupPin -> "Set a 4-digit security code to secure your transactions"
                is AuthUiState.EnterPin -> "Enter your 4-digit security passcode"
                else -> "Authenticating..."
            }

            Text(
                text = headerText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val filled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Message
            AnimatedVisibility(
                visible = pinError != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = pinError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Keypad
            val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "BIO", "0", "DEL")
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .fillMaxWidth()
            ) {
                items(keys.size) { index ->
                    val key = keys[index]
                    when (key) {
                        "BIO" -> {
                            IconButton(
                                onClick = {
                                    // Bypass or Biometric simulated triggers
                                    viewModel.bypassAuth()
                                },
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Biometric Sign In",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        "DEL" -> {
                            IconButton(
                                onClick = {
                                    if (enteredPin.isNotEmpty()) {
                                        enteredPin = enteredPin.dropLast(1)
                                    }
                                },
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Delete key",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        else -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable {
                                        if (enteredPin.length < 4) {
                                            enteredPin += key
                                            if (enteredPin.length == 4) {
                                                if (uiState is AuthUiState.SetupPin) {
                                                    if (!confirmPinMode) {
                                                        // First PIN entered
                                                        firstPin = enteredPin
                                                        confirmPinMode = true
                                                        enteredPin = ""
                                                    } else {
                                                        // Verify confirmation
                                                        if (enteredPin == firstPin) {
                                                            showInitialAccounts = true
                                                        } else {
                                                            // Mismatch
                                                            confirmPinMode = false
                                                            firstPin = ""
                                                            enteredPin = ""
                                                            // Trigger mock error locally for mismatch
                                                            viewModel.setupPin("MISMATCH")
                                                        }
                                                    }
                                                } else {
                                                    viewModel.verifyPin(enteredPin)
                                                    enteredPin = ""
                                                }
                                            }
                                        }
                                    }
                                    .testTag("pin_key_$key")
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bypass Button for easy Developer/First Launch Use
            TextButton(
                onClick = { viewModel.bypassAuth() },
                modifier = Modifier.testTag("bypass_auth")
            ) {
                Text(
                    text = "Bypass Passcode (Developer Mode)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
        }
    }

    if (showInitialAccounts) {
        var cashText by remember { mutableStateOf("") }
        var bankText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = {}, // block dismiss
            title = { Text("Initial Setup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Please enter your current balances to get started.")
                    OutlinedTextField(
                        value = cashText,
                        onValueChange = { cashText = it },
                        label = { Text("Current Cash") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = bankText,
                        onValueChange = { bankText = it },
                        label = { Text("Current Bank Balance") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val cash = cashText.toDoubleOrNull() ?: 0.0
                    val bank = bankText.toDoubleOrNull() ?: 0.0
                    financeViewModel.addAccount("Cash", AccountType.CASH, cash, true)
                    financeViewModel.addAccount("Bank", AccountType.BANK, bank, true)
                    viewModel.setupPin(firstPin)
                    showInitialAccounts = false
                }) {
                    Text("Continue")
                }
            }
        )
    }
}