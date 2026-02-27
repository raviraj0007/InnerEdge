package com.inneredge.ui.components

import kotlin.math.abs

fun formatCurrency(value: Double): String {
    val formatted = "%,.2f".format(abs(value))
    return if (value >= 0) "+₹$formatted" else "-₹$formatted"
}

fun formatPercent(value: Double, decimals: Int = 2): String = ("%." + decimals + "f%%").format(value)
fun Double.format(decimals: Int): String = ("%." + decimals + "f").format(this)
