package org.example.project.domain.util

import kotlinx.datetime.LocalDate
import org.example.project.domain.model.Trade

fun groupTradesByDate(trades: List<Trade>): Map<LocalDate, List<Trade>> =
    trades.groupBy { it.date }

fun calculateWinRate(trades: List<Trade>): Double {
    val closed = trades.filter { it.pnl != null }
    if (closed.isEmpty()) return 0.0
    val wins = closed.count { (it.pnl ?: 0.0) > 0.0 }
    return wins.toDouble() / closed.size * 100
}

fun calculateTotalPnL(trades: List<Trade>): Double =
    trades.sumOf { it.pnl ?: 0.0 }
