package com.inneredge.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inneredge.domain.usecase.GetTradesUseCase
import com.inneredge.presentation.state.DisciplineState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DisciplineViewModel @Inject constructor(
    getTradesUseCase: GetTradesUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(DisciplineState())
    val state: StateFlow<DisciplineState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getTradesUseCase().collect { _state.update { state -> state.copy(trades = it) } }
        }
    }
}
