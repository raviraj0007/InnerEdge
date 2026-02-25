package com.inneredge.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inneredge.domain.usecase.GetTradesUseCase
import com.inneredge.presentation.state.TradeFilter
import com.inneredge.presentation.state.TradeListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TradeListViewModel @Inject constructor(
    getTradesUseCase: GetTradesUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(TradeListState())
    val state: StateFlow<TradeListState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getTradesUseCase().collect { trades -> _state.update { it.copy(trades = trades) } }
        }
    }

    fun setFilter(filter: TradeFilter) = _state.update { it.copy(selectedFilter = filter) }
    fun toggleFab() = _state.update { it.copy(isFabExpanded = !it.isFabExpanded) }
    fun collapseFab() = _state.update { it.copy(isFabExpanded = false) }
}
