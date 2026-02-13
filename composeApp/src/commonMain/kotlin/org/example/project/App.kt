package org.example.project

import androidx.compose.runtime.Composable
import org.example.project.domain.repository.TradeRepository
import org.example.project.ui.AppNavigation
import org.example.project.ui.theme.AppTheme

@Composable
fun App(tradeRepository: TradeRepository) {
    AppTheme {
        AppNavigation(repository = tradeRepository)
    }
}
