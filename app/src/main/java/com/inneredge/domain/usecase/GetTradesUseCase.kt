package com.inneredge.domain.usecase

import com.inneredge.domain.model.Trade
import com.inneredge.domain.repository.TradeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTradesUseCase @Inject constructor(
    private val repository: TradeRepository
) {
    operator fun invoke(): Flow<List<Trade>> = repository.getAllTrades()
}
