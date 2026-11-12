package com.example.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesManager(private val context: Context) {

    companion object {
        val PIN_LOCK_CODE = stringPreferencesKey("pin_lock_code")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val THEME_MODE = stringPreferencesKey("theme_mode") // SYSTEM, LIGHT, DARK
        val USER_NAME = stringPreferencesKey("user_name")
        val MONTHLY_GOAL = doublePreferencesKey("monthly_goal")
        val SAVINGS_GOAL = doublePreferencesKey("savings_goal")
        val PROFILE_PHOTO_URI = stringPreferencesKey("profile_photo_uri")
    }

    val pinLockCode: Flow<String> = context.dataStore.data.map { it[PIN_LOCK_CODE] ?: "" }
    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { it[IS_BIOMETRIC_ENABLED] ?: false }
    val currencyCode: Flow<String> = context.dataStore.data.map { it[CURRENCY_CODE] ?: "INR" }
    val themeMode: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: "LIGHT" }
    val userName: Flow<String> = context.dataStore.data.map { it[USER_NAME] ?: "Financial Pro" }
    val monthlyGoal: Flow<Double> = context.dataStore.data.map { it[MONTHLY_GOAL] ?: 2500.0 }
    val savingsGoal: Flow<Double> = context.dataStore.data.map { it[SAVINGS_GOAL] ?: 500.0 }
    val profilePhotoUri: Flow<String> = context.dataStore.data.map { it[PROFILE_PHOTO_URI] ?: "" }

    suspend fun setPinLockCode(pin: String) {
        context.dataStore.edit { it[PIN_LOCK_CODE] = pin }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[IS_BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setCurrencyCode(code: String) {
        context.dataStore.edit { it[CURRENCY_CODE] = code }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[USER_NAME] = name }
    }

    suspend fun setMonthlyGoal(goal: Double) {
        context.dataStore.edit { it[MONTHLY_GOAL] = goal }
    }

    suspend fun setSavingsGoal(goal: Double) {
        context.dataStore.edit { it[SAVINGS_GOAL] = goal }
    }

    suspend fun setProfilePhotoUri(uri: String) {
        context.dataStore.edit { it[PROFILE_PHOTO_URI] = uri }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { it.clear() }
    }
}
