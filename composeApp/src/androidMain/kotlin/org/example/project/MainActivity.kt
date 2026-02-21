package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.example.project.db.TradeDataBase
import org.example.project.domain.repository.SqlDelightTradeRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val driver = AndroidSqliteDriver(
            schema = TradeDataBase.Schema,
            context = applicationContext,
            name = "trade.db"
        )
        val database = TradeDataBase(driver)
        val repository = SqlDelightTradeRepository(database)

        setContent {
            App(repository)
        }
    }
}
