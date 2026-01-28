package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.example.project.domain.repository.TradeRepository
import org.example.project.ui.TradeListScreen

// ✅ We added 'tradeRepository' as a parameter here
@Composable
fun App(tradeRepository: TradeRepository) {
    MaterialTheme {
        // Pass the repository to the screen we created in Step 1
        TradeListScreen(tradeRepository)
    }
}




