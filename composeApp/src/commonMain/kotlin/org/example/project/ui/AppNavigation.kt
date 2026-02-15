package org.example.project.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.example.project.domain.repository.TradeRepository

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Trades : Screen("trades")
    object Discipline : Screen("discipline")
    object AddTrade : Screen("add_trade?tradeId={tradeId}") {
        fun createRoute(tradeId: String? = null): String =
            if (tradeId == null) "add_trade" else "add_trade?tradeId=$tradeId"
    }

    object TradeDetail : Screen("trade_detail/{tradeId}") {
        fun createRoute(tradeId: String): String = "trade_detail/$tradeId"
    }
}

@Composable
fun AppNavigation(repository: TradeRepository) {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomScreens = listOf(Screen.Dashboard, Screen.Trades, Screen.Discipline)

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            if (currentRoute in bottomScreens.map { it.route }) {
                BottomAppBar(containerColor = MaterialTheme.colorScheme.surface) {
                    bottomScreens.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                when (screen) {
                                    Screen.Dashboard -> Icon(Icons.Filled.Assessment, contentDescription = "Dashboard")
                                    Screen.Trades -> Icon(Icons.Filled.ShowChart, contentDescription = "Trades")
                                    Screen.Discipline -> Icon(Icons.Filled.Psychology, contentDescription = "Discipline")
                                    else -> Unit
                                }
                            },
                            label = {
                                Text(
                                    text = when (screen) {
                                        Screen.Dashboard -> "Dashboard"
                                        Screen.Trades -> "Trades"
                                        Screen.Discipline -> "Discipline"
                                        else -> ""
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(repository = repository, contentPadding = innerPadding)
            }
            composable(Screen.Trades.route) {
                TradeListScreen(
                    repository = repository,
                    onAddClick = { navController.navigate(Screen.AddTrade.createRoute()) },
                    onTradeClick = { navController.navigate(Screen.TradeDetail.createRoute(it)) }
                )
            }
            composable(Screen.Discipline.route) {
                DisciplineScreen(repository = repository, contentPadding = innerPadding)
            }
            composable(
                route = Screen.AddTrade.route,
                arguments = listOf(navArgument("tradeId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                AddTradeScreen(
                    repository = repository,
                    tradeId = backStackEntry.arguments?.getString("tradeId"),
                    onTradeSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.TradeDetail.route,
                arguments = listOf(navArgument("tradeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tradeId = backStackEntry.arguments?.getString("tradeId") ?: return@composable
                TradeDetailScreen(
                    repository = repository,
                    tradeId = tradeId,
                    onBack = { navController.popBackStack() },
                    onEditTrade = { navController.navigate(Screen.AddTrade.createRoute(it)) }
                )
            }
        }
    }
}
