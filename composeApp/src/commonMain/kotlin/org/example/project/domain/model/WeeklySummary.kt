package org.example.project.domain.model

import kotlinx.datetime.LocalDate

data class WeeklySummary(
    val startDate: LocalDate,
    val endDate: LocalDate,

    val totalTrades: Int,
    val winRate: Double,
    val netPnL: Double,

    val averageRisk: Double,
    val disciplineScore: Int,
    val dominantEmotion: Emotion
)
