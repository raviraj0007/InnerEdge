package com.inneredge.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trades")
data class TradeEntity(
    @PrimaryKey val id: String,
    val date: String,
    val instrument: String,
    val marketType: String,
    val direction: String,
    val entryPrice: Double,
    val exitPrice: Double?,
    val quantity: Int,
    val stopLoss: Double?,
    val target: Double?,
    val riskPercent: Double?,
    val pnl: Double?,
    val status: String,
    val strategy: String?,
    val mistakes: String,
    val emotion: String?
)
