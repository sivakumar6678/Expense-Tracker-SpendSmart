package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.utils.UserPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Checking : AuthUiState
    object SetupPin : AuthUiState
    object EnterPin : AuthUiState
    object Authenticated : AuthUiState
}

class AuthViewModel(private val preferencesManager: UserPreferencesManager) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Checking)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    val userName: StateFlow<String> = preferencesManager.userName.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Financial Pro"
    )

    val currencyCode: StateFlow<String> = preferencesManager.currencyCode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "INR"
    )

    val profilePhotoUri: StateFlow<String> = preferencesManager.profilePhotoUri.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val monthlyGoal: StateFlow<Double> = preferencesManager.monthlyGoal.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 2500.0
    )

    val savingsGoal: StateFlow<Double> = preferencesManager.savingsGoal.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 500.0
    )

    val isBiometricEnabled: StateFlow<Boolean> = preferencesManager.isBiometricEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val themeMode: StateFlow<String> = preferencesManager.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "LIGHT"
    )

    init {
        checkPinStatus()
    }

    private fun checkPinStatus() {
        viewModelScope.launch {
            val pin = preferencesManager.pinLockCode.first()
            if (pin.isEmpty()) {
                _uiState.value = AuthUiState.SetupPin
            } else {
                _uiState.value = AuthUiState.EnterPin
            }
        }
    }

    fun setupPin(newPin: String) {
        viewModelScope.launch {
            if (newPin.length == 4) {
                preferencesManager.setPinLockCode(newPin)
                _uiState.value = AuthUiState.Authenticated
                _pinError.value = null
            } else {
                _pinError.value = "PIN must be exactly 4 digits"
            }
        }
    }

    fun verifyPin(enteredPin: String) {
        viewModelScope.launch {
            val actualPin = preferencesManager.pinLockCode.first()
            if (enteredPin == actualPin) {
                _uiState.value = AuthUiState.Authenticated
                _pinError.value = null
            } else {
                _pinError.value = "Invalid PIN"
            }
        }
    }

    fun bypassAuth() {
        _uiState.value = AuthUiState.Authenticated
    }

    fun logOut() {
        viewModelScope.launch {
            val pin = preferencesManager.pinLockCode.first()
            if (pin.isEmpty()) {
                _uiState.value = AuthUiState.SetupPin
            } else {
                _uiState.value = AuthUiState.EnterPin
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setBiometricEnabled(enabled)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun updateProfile(name: String, currency: String, photoUri: String, mGoal: Double, sGoal: Double) {
        viewModelScope.launch {
            preferencesManager.setUserName(name)
            preferencesManager.setCurrencyCode(currency)
            preferencesManager.setProfilePhotoUri(photoUri)
            preferencesManager.setMonthlyGoal(mGoal)
            preferencesManager.setSavingsGoal(sGoal)
        }
    }

    fun clearAllDataAndReset() {
        viewModelScope.launch {
            preferencesManager.clearAllData()
            _uiState.value = AuthUiState.SetupPin
        }
    }

    class Factory(private val preferencesManager: UserPreferencesManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(preferencesManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
