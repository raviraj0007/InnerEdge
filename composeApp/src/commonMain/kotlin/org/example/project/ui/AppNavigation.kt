package org.example.project.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.example.project.domain.repository.TradeRepository

sealed class Screen(val route: String) {
    data object DashboardScreen : Screen("dashboard")
    data object TradesScreen : Screen("trades")
    data object DisciplineScreen : Screen("discipline")
}

@Composable
fun AppNavigation(
    repository: TradeRepository,
    onAddTradeClick: () -> Unit
) {
    val navController = rememberNavController()
    val tabs = listOf(Screen.DashboardScreen, Screen.TradesScreen, Screen.DisciplineScreen)
    val backStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { screen ->
                    val selected = backStackEntry?.destination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (screen) {
                                    Screen.DashboardScreen -> Icons.Default.Dashboard
                                    Screen.TradesScreen -> Icons.Default.ShowChart
                                    Screen.DisciplineScreen -> Icons.Default.Assessment
                                },
                                contentDescription = screen.route
                            )
                        },
                        label = {
                            Text(
                                text = when (screen) {
                                    Screen.DashboardScreen -> "Dashboard"
                                    Screen.TradesScreen -> "Trades"
                                    Screen.DisciplineScreen -> "Discipline"
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.DashboardScreen.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.DashboardScreen.route) {
                DashboardScreen(repository = repository)
            }
            composable(Screen.TradesScreen.route) {
                TradeListScreen(repository = repository, onAddClick = onAddTradeClick)
            }
            composable(Screen.DisciplineScreen.route) {
                DisciplineScreen(repository = repository)
            }
        }
    }
}
