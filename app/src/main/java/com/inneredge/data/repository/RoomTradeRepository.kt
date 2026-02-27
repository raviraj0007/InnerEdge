package com.inneredge.data.repository

import com.inneredge.data.local.TradeDao
import com.inneredge.data.mapper.toDomain
import com.inneredge.data.mapper.toEntity
import com.inneredge.domain.model.Trade
import com.inneredge.domain.repository.TradeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomTradeRepository @Inject constructor(
    private val tradeDao: TradeDao
) : TradeRepository {
    override fun getAllTrades(): Flow<List<Trade>> = tradeDao.getAllTrades().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun insertTrade(trade: Trade) {
        tradeDao.insertTrade(trade.toEntity())
    }

    override suspend fun getTradeById(id: String): Trade? {
        return tradeDao.getTradeById(id)?.toDomain()
    }

    override suspend fun updateTrade(trade: Trade) {
        tradeDao.updateTrade(trade.toEntity())
    }
}
