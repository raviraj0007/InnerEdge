package org.example.project.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.domain.model.Trade

interface TradeRepository {

    fun getAllTrades(): Flow<List<Trade>>

    suspend fun getTradeById(id: String): Trade?

    suspend fun insertTrade(trade: Trade)

    suspend fun updateTrade(trade: Trade)

    suspend fun deleteTrade(id: String)
}
