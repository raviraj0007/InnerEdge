package com.inneredge.presentation

import com.inneredge.domain.FakeTradeRepository
import com.inneredge.domain.model.MarketType
import com.inneredge.domain.model.Trade
import com.inneredge.domain.model.TradeDirection
import com.inneredge.domain.model.TradeStatus
import com.inneredge.domain.usecase.GetTradesUseCase
import com.inneredge.presentation.viewmodel.TradeListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TradeListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loads trades into state`() = runTest {
        val repo = FakeTradeRepository(
            listOf(
                Trade("1", LocalDate.now(), "NIFTY", 100.0, null, 1, null, null, null, null, MarketType.FNO, TradeDirection.BUY, TradeStatus.OPEN)
            )
        )

        val viewModel = TradeListViewModel(GetTradesUseCase(repo))

        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.trades.size)
    }
}
