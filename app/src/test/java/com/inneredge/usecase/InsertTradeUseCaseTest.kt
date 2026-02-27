package com.inneredge.usecase

import com.inneredge.domain.FakeTradeRepository
import com.inneredge.domain.model.MarketType
import com.inneredge.domain.model.Trade
import com.inneredge.domain.model.TradeDirection
import com.inneredge.domain.model.TradeStatus
import com.inneredge.domain.usecase.InsertTradeUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class InsertTradeUseCaseTest {
    @Test
    fun `inserts trade into repository`() = runTest {
        val repository = FakeTradeRepository()
        val useCase = InsertTradeUseCase(repository)
        val trade = Trade("1", LocalDate.now(), "BTC", 10.0, null, 1, null, null, null, null, MarketType.CRYPTO, TradeDirection.BUY, TradeStatus.OPEN)

        useCase(trade)

        assertEquals("1", repository.getAllTrades().first().first().id)
    }
}
