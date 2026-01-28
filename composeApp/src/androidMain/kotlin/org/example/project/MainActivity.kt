package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import app.cash.sqldelight.driver.android.AndroidSqliteDriver // Import Driver Directly
import org.example.project.db.TradeDataBase
import org.example.project.domain.repository.SqlDelightTradeRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Create the Driver directly (No Factory needed)
        val driver = AndroidSqliteDriver(
            schema = TradeDataBase.Schema,
            context = applicationContext,
            name = "trade.db"
        )

        // 2. Initialize Database & Repository
        val database = TradeDataBase(driver)
        val repository = SqlDelightTradeRepository(database)

        setContent {
            App(repository)
        }
    }
}

@Composable
fun AppAndroidPreview() {
    // App(repository) // Preview usually requires a mock repository, ignoring for now
}