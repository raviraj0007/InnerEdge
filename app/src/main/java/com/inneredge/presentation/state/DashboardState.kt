package com.inneredge.presentation.state

import com.inneredge.domain.model.Trade

data class DashboardState(val trades: List<Trade> = emptyList())
