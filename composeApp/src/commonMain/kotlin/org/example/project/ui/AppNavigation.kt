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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.example.project.domain.repository.TradeRepository

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Trades : Screen("trades")
    object Discipline : Screen("discipline")
    object AddTrade : Screen("add_trade")
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
                                Screen.AddTrade -> Unit
                            }
                        },
                        label = {
                            Text(
                                text = when (screen) {
                                    Screen.Dashboard -> "Dashboard"
                                    Screen.Trades -> "Trades"
                                    Screen.Discipline -> "Discipline"
                                    Screen.AddTrade -> ""
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
                    onAddClick = { navController.navigate(Screen.AddTrade.route) }
                )
            }
            composable(Screen.Discipline.route) {
                DisciplineScreen(repository = repository, contentPadding = innerPadding)
            }
            composable(Screen.AddTrade.route) {
                AddTradeScreen(
                    repository = repository,
                    onTradeSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}
