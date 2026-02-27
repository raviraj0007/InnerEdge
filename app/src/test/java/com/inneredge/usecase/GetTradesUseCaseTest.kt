package com.inneredge.usecase

import com.inneredge.domain.FakeTradeRepository
import com.inneredge.domain.model.MarketType
import com.inneredge.domain.model.Trade
import com.inneredge.domain.model.TradeDirection
import com.inneredge.domain.model.TradeStatus
import com.inneredge.domain.usecase.GetTradesUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GetTradesUseCaseTest {
    @Test
    fun `returns trades from repository`() = runTest {
        val trades = listOf(
            Trade("1", LocalDate.now(), "NIFTY", 100.0, null, 50, null, null, null, null, MarketType.FNO, TradeDirection.BUY, TradeStatus.OPEN)
        )
        val useCase = GetTradesUseCase(FakeTradeRepository(trades))
        assertEquals(1, useCase().first().size)
    }
}
