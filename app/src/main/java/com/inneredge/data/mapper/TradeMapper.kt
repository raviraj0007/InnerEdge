package com.inneredge.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.inneredge.data.local.TradeEntity
import com.inneredge.domain.model.MarketType
import com.inneredge.domain.model.Trade
import com.inneredge.domain.model.TradeDirection
import com.inneredge.domain.model.TradeStatus
import java.time.LocalDate

private const val MISTAKE_DELIMITER = "|"

@RequiresApi(Build.VERSION_CODES.O)
fun TradeEntity.toDomain(): Trade {
    return Trade(
        id = id,
        date = runCatching { LocalDate.parse(date) }.getOrDefault(LocalDate.now()),
        instrument = instrument,
        entryPrice = entryPrice,
        exitPrice = exitPrice,
        quantity = quantity,
        stopLoss = stopLoss,
        target = target,
        riskPercent = riskPercent,
        pnl = pnl,
        marketType = runCatching { MarketType.valueOf(marketType) }.getOrDefault(MarketType.STOCK),
        direction = runCatching { TradeDirection.valueOf(direction) }.getOrDefault(TradeDirection.BUY),
        status = runCatching { TradeStatus.valueOf(status) }.getOrDefault(TradeStatus.OPEN),
        strategy = strategy,
        mistakes = mistakes.takeIf { it.isNotBlank() }?.split(MISTAKE_DELIMITER) ?: emptyList(),
        emotion = emotion
    )
}

fun Trade.toEntity(): TradeEntity = TradeEntity(
    id = id,
    date = date.toString(),
    instrument = instrument,
    marketType = marketType.name,
    direction = direction.name,
    entryPrice = entryPrice,
    exitPrice = exitPrice,
    quantity = quantity,
    stopLoss = stopLoss,
    target = target,
    riskPercent = riskPercent,
    pnl = pnl,
    status = status.name,
    strategy = strategy,
    mistakes = mistakes.joinToString(MISTAKE_DELIMITER),
    emotion = emotion
)
