package org.example.project.ui.util

import kotlin.math.abs

fun formatCurrency(value: Double): String {
    val formatted = "%,.2f".format(abs(value))
    return if (value >= 0) "+₹$formatted" else "-₹$formatted"
}

fun formatPercent(value: Double, decimals: Int = 2): String {
    return ("%." + decimals + "f%%").format(value)
}

fun Double.format(decimals: Int): String {
    return ("%." + decimals + "f").format(this)
}
