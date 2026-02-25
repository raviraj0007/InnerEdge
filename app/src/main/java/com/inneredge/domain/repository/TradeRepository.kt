package com.inneredge.domain.repository

import com.inneredge.domain.model.Trade
import kotlinx.coroutines.flow.Flow

interface TradeRepository {
    fun getAllTrades(): Flow<List<Trade>>
    suspend fun insertTrade(trade: Trade)
}
