package com.inneredge.data

import com.inneredge.data.local.TradeDao
import com.inneredge.data.local.TradeEntity
import com.inneredge.data.repository.RoomTradeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomTradeRepositoryTest {
    @Test
    fun `maps entity to domain model`() = runTest {
        val dao = FakeDao(
            listOf(
                TradeEntity("1", "2025-01-01", "NIFTY", "FNO", "BUY", 10.0, null, 1, null, null, null, null, "OPEN", null, "", null)
            )
        )
        val repository = RoomTradeRepository(dao)

        val result = repository.getAllTrades().first()

        assertEquals("NIFTY", result.first().instrument)
    }

    private class FakeDao(initial: List<TradeEntity>) : TradeDao {
        private val state = MutableStateFlow(initial)
        override fun getAllTrades(): Flow<List<TradeEntity>> = state
        override suspend fun insertTrade(trade: TradeEntity) { state.value = state.value + trade }
    }
}
