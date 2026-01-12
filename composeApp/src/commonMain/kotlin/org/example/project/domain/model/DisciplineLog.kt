package org.example.project.domain.model

import kotlinx.datetime.LocalDate

data class DisciplineLog(
    val date: LocalDate,

    val followedMaxTradesRule: Boolean,
    val followedStopLossRule: Boolean,
    val followedRiskRule: Boolean,
    val avoidedRevengeTrading: Boolean,

    val disciplineScore: Int // 0–100
)
