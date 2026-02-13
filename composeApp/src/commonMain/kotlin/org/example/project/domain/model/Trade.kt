package org.example.project.domain.model

import kotlinx.datetime.LocalDate

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
    val strategy: String = "",
    val mistakes: List<String> = emptyList(),
    val emotion: String = "Neutral"
)

data class EmotionStats(
    val emotion: String,
    val tradeCount: Int,
    val totalPnl: Double,
    val averageWin: Double,
    val averageLoss: Double
)

fun List<Trade>.groupTradesByDate(): Map<LocalDate, List<Trade>> = groupBy { it.date }

fun List<Trade>.groupTradesByEmotion(): Map<String, List<Trade>> =
    groupBy { it.emotion.ifBlank { "Unspecified" } }

fun List<Trade>.emotionStats(): List<EmotionStats> =
    groupTradesByEmotion().map { (emotion, trades) ->
        val wins = trades.mapNotNull { it.pnl }.filter { it > 0 }
        val losses = trades.mapNotNull { it.pnl }.filter { it < 0 }
        EmotionStats(
            emotion = emotion,
            tradeCount = trades.size,
            totalPnl = trades.sumOf { it.pnl ?: 0.0 },
            averageWin = if (wins.isEmpty()) 0.0 else wins.average(),
            averageLoss = if (losses.isEmpty()) 0.0 else losses.average()
        )
    }.sortedByDescending { it.tradeCount }
