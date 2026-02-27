package com.inneredge.domain

import com.inneredge.domain.model.Trade
import com.inneredge.domain.repository.TradeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTradeRepository(initialTrades: List<Trade> = emptyList()) : TradeRepository {
    private val flow = MutableStateFlow(initialTrades)
    override fun getAllTrades(): Flow<List<Trade>> = flow
    override suspend fun insertTrade(trade: Trade) {
        flow.value = flow.value + trade
    }
}
