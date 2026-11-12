package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.di.FinanceContainer
import com.example.navigation.AppNavigation
import com.example.ui.theme.FinanceTheme
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {
    private val container by lazy { FinanceContainer(applicationContext) }

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModel.Factory(container.preferencesManager)
    }

    private val financeViewModel: FinanceViewModel by viewModels {
        FinanceViewModel.Factory(container.repository, container.geminiService)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by authViewModel.themeMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> systemDark
            }

            FinanceTheme(darkTheme = isDark) {
                AppNavigation(
                    authViewModel = authViewModel,
                    financeViewModel = financeViewModel
                )
            }
        }
    }
}
