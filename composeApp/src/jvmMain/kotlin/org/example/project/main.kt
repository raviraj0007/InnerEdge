package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver // Import Driver Directly
import org.example.project.db.TradeDataBase
import org.example.project.domain.repository.SqlDelightTradeRepository

fun main() = application {

    // 1. Create the Driver directly (No Factory needed)
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    // On Desktop, we must create the schema manually
    TradeDataBase.Schema.create(driver)

    // 2. Initialize Database & Repository
    val database = TradeDataBase(driver)
    val repository = SqlDelightTradeRepository(database)

    Window(
        onCloseRequest = ::exitApplication,
        title = "InnerEdge",
    ) {
        App(repository)
    }
}