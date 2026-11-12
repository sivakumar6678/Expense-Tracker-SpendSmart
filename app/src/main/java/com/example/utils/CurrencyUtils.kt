package com.example.utils

import java.text.DecimalFormat
import java.util.*

object CurrencyUtils {

    /**
     * Formats a double value according to the Indian Numbering System (Lakhs and Crores).
     * Examples:
     * - 250 -> ₹250
     * - 1250 -> ₹1,250
     * - 12540 -> ₹12,540
     * - 125000 -> ₹1,25,000
     * - 1050000 -> ₹10,50,000
     */
    fun formatIndianCurrency(amount: Double, includePaise: Boolean = false): String {
        val isNegative = amount < 0
        val absAmount = Math.abs(amount)
        
        // Round to 2 decimal places
        val roundedAmount = Math.round(absAmount * 100.0) / 100.0
        val integerPart = roundedAmount.toLong()
        val paise = Math.round((roundedAmount - integerPart) * 100.0)
        
        val intStr = integerPart.toString()
        val formattedInt = if (intStr.length <= 3) {
            intStr
        } else {
            val lastThree = intStr.substring(intStr.length - 3)
            var remaining = intStr.substring(0, intStr.length - 3)
            val parts = mutableListOf<String>()
            while (remaining.length > 2) {
                parts.add(0, remaining.substring(remaining.length - 2))
                remaining = remaining.substring(0, remaining.length - 2)
            }
            if (remaining.isNotEmpty()) {
                parts.add(0, remaining)
            }
            parts.joinToString(",") + "," + lastThree
        }
        
        val prefix = if (isNegative) "-₹" else "₹"
        
        return if (includePaise || paise > 0) {
            "$prefix$formattedInt.${String.format("%02d", paise)}"
        } else {
            "$prefix$formattedInt"
        }
    }
}
