package com.inneredge.ui.navigation
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.inneredge.presentation.viewmodel.AddTradeViewModel
import com.inneredge.presentation.viewmodel.DashboardViewModel
import com.inneredge.presentation.viewmodel.DisciplineViewModel
import com.inneredge.presentation.viewmodel.TradeListViewModel
import com.inneredge.ui.screens.AddTradeScreen
import com.inneredge.ui.screens.DashboardScreen
import com.inneredge.ui.screens.DisciplineScreen
import com.inneredge.ui.screens.TradeListScreen
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*


sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Trades : Screen("trades")
    data object Discipline : Screen("discipline")
    data object AddTrade : Screen("add_trade")
    data object EditTrade : Screen("add_trade/{tradeId}")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val bottomScreens = listOf(Screen.Dashboard, Screen.Trades, Screen.Discipline)

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            if (currentRoute in bottomScreens.map { it.route }) {
                BottomAppBar(containerColor = MaterialTheme.colorScheme.surface) {
                    bottomScreens.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                when (screen) {
                                    Screen.Dashboard -> Icon(Icons.Default.Dashboard, contentDescription = "Dashboard")
                                    Screen.Trades -> Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Trades")
                                    Screen.Discipline -> Icon(Icons.Default.School, contentDescription = "Discipline")
                                    Screen.AddTrade -> Unit
                                    Screen.EditTrade -> Unit
                                }
                            },
                            label = {
                                Text(
                                    when (screen) {
                                        Screen.Dashboard -> "Dashboard"
                                        Screen.Trades -> "Trades"
                                        Screen.Discipline -> "Discipline"
                                        Screen.AddTrade -> ""
                                        Screen.EditTrade -> ""
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    contentPadding = innerPadding,
                    viewModel = hiltViewModel<DashboardViewModel>()
                )
            }
            composable(Screen.Trades.route) {
                TradeListScreen(
                    viewModel = hiltViewModel<TradeListViewModel>(),
                    onAddClick = { navController.navigate(Screen.AddTrade.route) },
                    onTradeClick = { tradeId -> navController.navigate("add_trade/$tradeId") }
                )
            }
            composable(Screen.Discipline.route) {
                DisciplineScreen(
                    contentPadding = innerPadding,
                    viewModel = hiltViewModel<DisciplineViewModel>()
                )
            }
            composable(Screen.AddTrade.route) {
                AddTradeScreen(
                    viewModel = hiltViewModel<AddTradeViewModel>(),
                    onTradeSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.EditTrade.route,
                arguments = listOf(navArgument("tradeId") { type = NavType.StringType })
            ) {
                val tradeId = it.arguments?.getString("tradeId") ?: return@composable
                val viewModel = hiltViewModel<AddTradeViewModel>()

                androidx.compose.runtime.LaunchedEffect(tradeId) {
                    viewModel.loadTrade(tradeId)
                }

                AddTradeScreen(
                    viewModel = viewModel,
                    onTradeSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}
