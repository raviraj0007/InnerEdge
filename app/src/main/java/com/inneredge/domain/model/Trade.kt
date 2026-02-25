package com.inneredge.domain.model

import java.time.LocalDate

data class Trade(
    val id: String,
    val date: LocalDate,
    val instrument: String,
    val entryPrice: Double,
    val exitPrice: Double?,
    val quantity: Int,
    val stopLoss: Double?,
    val target: Double?,
    val riskPercent: Double?,
    val pnl: Double?,
    val marketType: MarketType,
    val direction: TradeDirection,
    val status: TradeStatus,
    val strategy: String? = null,
    val mistakes: List<String> = emptyList(),
    val emotion: String? = null
)
