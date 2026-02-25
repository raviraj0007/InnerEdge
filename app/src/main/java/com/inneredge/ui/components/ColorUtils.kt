package com.inneredge.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun pnlColor(value: Double): Color = when {
    value > 0 -> Color(0xFF2E7D32)
    value < 0 -> Color(0xFFC62828)
    else -> MaterialTheme.colorScheme.onSurface
}
