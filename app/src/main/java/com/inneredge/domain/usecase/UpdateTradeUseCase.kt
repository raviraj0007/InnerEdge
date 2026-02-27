package com.inneredge.domain.usecase

import com.inneredge.domain.model.Trade
import com.inneredge.domain.repository.TradeRepository
import javax.inject.Inject

class UpdateTradeUseCase @Inject constructor(
    private val repository: TradeRepository
) {
    suspend operator fun invoke(trade: Trade) = repository.insertTrade(trade)
}
