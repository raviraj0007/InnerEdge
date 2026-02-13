package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.example.project.domain.repository.TradeRepository
import org.example.project.ui.AddTradeScreen
import org.example.project.ui.AppNavigation
import org.example.project.ui.theme.AppTheme

@Composable
fun App(tradeRepository: TradeRepository) {
    AppTheme {
        var showAddTrade by remember { mutableStateOf(false) }

        if (showAddTrade) {
            AddTradeScreen(
                repository = tradeRepository,
                onTradeSaved = { showAddTrade = false },
                onCancel = { showAddTrade = false }
            )
        } else {
            AppNavigation(
                repository = tradeRepository,
                onAddTradeClick = { showAddTrade = true }
            )
        }
    }
}
