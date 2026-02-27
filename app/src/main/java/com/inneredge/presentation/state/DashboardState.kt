package com.inneredge.presentation.state

import com.inneredge.domain.model.Trade

data class DashboardState(
    val trades: List<Trade> = emptyList(),
    val totalPnl: Double = 0.0,
    val winRate: Int = 0,
    val todayPnl: Double = 0.0,
    val totalTrades: Int = 0
)
