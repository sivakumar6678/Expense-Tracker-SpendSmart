package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ==========================================
// Primary Brand & Finance Palette
// ==========================================
// Emerald Green (Prosperity, Balance, Growth)
val FinanceEmeraldPrimaryLight = Color(0xFF059669)
val FinanceEmeraldOnPrimaryLight = Color(0xFFFFFFFF)
val FinanceEmeraldContainerLight = Color(0xFFD1FAE5)
val FinanceEmeraldOnContainerLight = Color(0xFF064E3B)

val FinanceEmeraldPrimaryDark = Color(0xFF34D399)
val FinanceEmeraldOnPrimaryDark = Color(0xFF064E3B)
val FinanceEmeraldContainerDark = Color(0xFF065F46)
val FinanceEmeraldOnContainerDark = Color(0xFFA7F3D0)

// Sky Blue / Slate Cyan (Stability, Trust, Banking)
val FinanceSkySecondaryLight = Color(0xFF0284C7)
val FinanceSkyOnSecondaryLight = Color(0xFFFFFFFF)
val FinanceSkyContainerLight = Color(0xFFE0F2FE)
val FinanceSkyOnContainerLight = Color(0xFF0369A1)

val FinanceSkySecondaryDark = Color(0xFF38BDF8)
val FinanceSkyOnSecondaryDark = Color(0xFF0C4A6E)
val FinanceSkyContainerDark = Color(0xFF075985)
val FinanceSkyOnContainerDark = Color(0xFFBAE6FD)

// Amber / Gold (Wealth, Budgets, Milestone Goals)
val FinanceAmberTertiaryLight = Color(0xFFD97706)
val FinanceAmberOnTertiaryLight = Color(0xFFFFFFFF)
val FinanceAmberContainerLight = Color(0xFFFEF3C7)
val FinanceAmberOnContainerLight = Color(0xFF78350F)

val FinanceAmberTertiaryDark = Color(0xFFFBBF24)
val FinanceAmberOnTertiaryDark = Color(0xFF78350F)
val FinanceAmberContainerDark = Color(0xFF92400E)
val FinanceAmberOnContainerDark = Color(0xFFFDE68A)

// Background & Surfaces - Light
val FinanceBackgroundLight = Color(0xFFF8FAFC) // Slate 50
val FinanceOnBackgroundLight = Color(0xFF0F172A) // Slate 900
val FinanceSurfaceLight = Color(0xFFFFFFFF) // Pure White Card
val FinanceOnSurfaceLight = Color(0xFF1E293B) // Slate 800
val FinanceSurfaceVariantLight = Color(0xFFF1F5F9) // Slate 100
val FinanceOnSurfaceVariantLight = Color(0xFF64748B) // Slate 500
val FinanceOutlineLight = Color(0xFFE2E8F0) // Slate 200
val FinanceOutlineVariantLight = Color(0xFFCBD5E1) // Slate 300

// Background & Surfaces - Dark
val FinanceBackgroundDark = Color(0xFF0B132B) // Deep Midnight Navy
val FinanceOnBackgroundDark = Color(0xFFF8FAFC) // Slate 50
val FinanceSurfaceDark = Color(0xFF1E293B) // Slate 800 Card
val FinanceOnSurfaceDark = Color(0xFFF1F5F9) // Slate 100
val FinanceSurfaceVariantDark = Color(0xFF334155) // Slate 700
val FinanceOnSurfaceVariantDark = Color(0xFF94A3B8) // Slate 400
val FinanceOutlineDark = Color(0xFF475569) // Slate 600
val FinanceOutlineVariantDark = Color(0xFF334155) // Slate 700

// Errors / Alerts
val FinanceErrorLight = Color(0xFFDC2626) // Crimson Red
val FinanceOnErrorLight = Color(0xFFFFFFFF)
val FinanceErrorContainerLight = Color(0xFFFEE2E2)
val FinanceOnErrorContainerLight = Color(0xFF7F1D1D)

val FinanceErrorDark = Color(0xFFF87171)
val FinanceOnErrorDark = Color(0xFF450A0A)
val FinanceErrorContainerDark = Color(0xFF7F1D1D)
val FinanceOnErrorContainerDark = Color(0xFFFECACA)

// ==========================================
// Semantic Extended Financial Tokens
// ==========================================
@Immutable
data class FinanceCustomColors(
    val income: Color,
    val onIncome: Color,
    val incomeContainer: Color,
    val expense: Color,
    val onExpense: Color,
    val expenseContainer: Color,
    val transfer: Color,
    val transferContainer: Color,
    val savings: Color,
    val savingsContainer: Color,
    val investment: Color,
    val investmentContainer: Color,
    val glassCard: Color,
    val borderSubtle: Color,
    val chartColors: List<Color>
)

val LightFinanceCustomColors = FinanceCustomColors(
    income = Color(0xFF10B981),
    onIncome = Color(0xFFFFFFFF),
    incomeContainer = Color(0xFFD1FAE5),
    expense = Color(0xFFEF4444),
    onExpense = Color(0xFFFFFFFF),
    expenseContainer = Color(0xFFFEE2E2),
    transfer = Color(0xFF3B82F6),
    transferContainer = Color(0xFFDBEAFE),
    savings = Color(0xFF8B5CF6),
    savingsContainer = Color(0xFFEDE9FE),
    investment = Color(0xFFF59E0B),
    investmentContainer = Color(0xFFFEF3C7),
    glassCard = Color(0xCCFFFFFF),
    borderSubtle = Color(0xFFE2E8F0),
    chartColors = listOf(
        Color(0xFF10B981),
        Color(0xFF38BDF8),
        Color(0xFFF59E0B),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899),
        Color(0xFF6366F1),
        Color(0xFF14B8A6)
    )
)

val DarkFinanceCustomColors = FinanceCustomColors(
    income = Color(0xFF34D399),
    onIncome = Color(0xFF064E3B),
    incomeContainer = Color(0xFF065F46),
    expense = Color(0xFFF87171),
    onExpense = Color(0xFF450A0A),
    expenseContainer = Color(0xFF7F1D1D),
    transfer = Color(0xFF60A5FA),
    transferContainer = Color(0xFF1E3A8A),
    savings = Color(0xFFA78BFA),
    savingsContainer = Color(0xFF4C1D95),
    investment = Color(0xFFFBBF24),
    investmentContainer = Color(0xFF78350F),
    glassCard = Color(0xCC1E293B),
    borderSubtle = Color(0xFF334155),
    chartColors = listOf(
        Color(0xFF34D399),
        Color(0xFF38BDF8),
        Color(0xFFFBBF24),
        Color(0xFFA78BFA),
        Color(0xFFF472B6),
        Color(0xFF818CF8),
        Color(0xFF2DD4BF)
    )
)

val LocalFinanceCustomColors = staticCompositionLocalOf { LightFinanceCustomColors }

// Backward compatibility constants for existing screens
val LiquidPrimary = FinanceEmeraldPrimaryLight
val LiquidSecondary = FinanceSkySecondaryLight
val LiquidAccent = FinanceAmberTertiaryLight
val LiquidBackground = FinanceBackgroundLight
val LiquidSurface = FinanceSurfaceLight
val LiquidOnBackground = FinanceOnBackgroundLight
val LiquidOnSurface = FinanceOnSurfaceLight
val LiquidSuccess = Color(0xFF10B981)
val LiquidError = Color(0xFFEF4444)
val LiquidWarning = Color(0xFFF59E0B)
val LiquidSurfaceVariant = FinanceSurfaceVariantLight
val LiquidOnSurfaceVariant = FinanceOnSurfaceVariantLight
val LiquidBorder = FinanceOutlineLight
val LiquidCardBackground = Color(0xB3FFFFFF)
