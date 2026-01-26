package org.example.project.domain.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

// ✅ This import will work now because the Build was Successful!
import org.example.project.db.TradeDataBase
import org.example.project.db.TradeEntity

import org.example.project.domain.model.MarketType
import org.example.project.domain.model.Trade
import org.example.project.domain.model.TradeDirection
import org.example.project.domain.model.TradeStatus

class SqlDelightTradeRepository(
    db: TradeDataBase
) : TradeRepository {

    // ✅ Make sure this matches your .sq file name (TradeDataBase.sq -> tradeDataBaseQueries)
    private val queries = db.tradeDataBaseQueries

    override fun getAllTrades(): Flow<List<Trade>> {
        return queries.selectAllTrades()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities: List<TradeEntity> ->
                entities.map { entity ->
                    Trade(
                        id = entity.id,
                        date = LocalDate.parse(entity.date),
                        instrument = entity.instrument,
                        marketType = MarketType.valueOf(entity.marketType),
                        direction = TradeDirection.valueOf(entity.direction),
                        entryPrice = entity.entryPrice,
                        exitPrice = entity.exitPrice,
                        quantity = entity.quantity.toInt(),
                        stopLoss = entity.stopLoss,
                        target = entity.target,
                        riskPercent = entity.riskPercent,
                        pnl = entity.pnl,
                        status = TradeStatus.valueOf(entity.status)
                    )
                }
            }
    }

    override suspend fun insertTrade(trade: Trade) {
        queries.insertTrade(
            id = trade.id,
            date = trade.date.toString(),
            instrument = trade.instrument,
            marketType = trade.marketType.name,
            direction = trade.direction.name,
            entryPrice = trade.entryPrice,
            exitPrice = trade.exitPrice,
            quantity = trade.quantity.toLong(),
            stopLoss = trade.stopLoss,
            target = trade.target,
            riskPercent = trade.riskPercent,
            pnl = trade.pnl,
            status = trade.status.name
        )
    }
}