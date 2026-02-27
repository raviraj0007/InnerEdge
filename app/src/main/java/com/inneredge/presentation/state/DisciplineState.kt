package com.inneredge.presentation.state

import com.inneredge.domain.model.Trade

data class DisciplineState(val trades: List<Trade> = emptyList())
