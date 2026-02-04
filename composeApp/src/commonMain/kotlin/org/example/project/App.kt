package org.example.project

import androidx.compose.runtime.*
import org.example.project.domain.repository.TradeRepository
import org.example.project.ui.AddTradeScreen
import org.example.project.ui.TradeListScreen
import org.example.project.ui.theme.AppTheme

@Composable
fun App(tradeRepository: TradeRepository) {
    AppTheme {
        // Simple Navigation State: "list" or "add"
        var currentScreen by remember { mutableStateOf("list") }

        when (currentScreen) {
            "list" -> {
                TradeListScreen(
                    repository = tradeRepository,
                    onAddClick = { currentScreen = "add" } // Navigate to Add Screen
                )
            }
            "add" -> {
                AddTradeScreen(
                    repository = tradeRepository,
                    onTradeSaved = { currentScreen = "list" }, // Go back on Save
                    onCancel = { currentScreen = "list" }      // Go back on Cancel
                )
            }
        }
    }
}
