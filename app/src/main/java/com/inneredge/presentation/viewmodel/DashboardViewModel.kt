package com.inneredge.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inneredge.domain.model.TradeStatus
import com.inneredge.domain.usecase.GetTradesUseCase
import com.inneredge.presentation.state.DashboardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getTradesUseCase: GetTradesUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getTradesUseCase().collect { trades ->
                val closedTrades = trades.filter { it.status == TradeStatus.CLOSED }
                val totalPnl = closedTrades.sumOf { it.pnl ?: 0.0 }
                val winCount = closedTrades.count { (it.pnl ?: 0.0) > 0.0 }
                val winRate = if (closedTrades.isNotEmpty()) {
                    (winCount * 100) / closedTrades.size
                } else {
                    0
                }

                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val todayPnl = closedTrades
                    .filter { it.date.toString() == today }
                    .sumOf { it.pnl ?: 0.0 }

                _state.update {
                    it.copy(
                        trades = trades,
                        totalPnl = totalPnl,
                        winRate = winRate,
                        todayPnl = todayPnl,
                        totalTrades = trades.size
                    )
                }
            }
        }
    }
}
