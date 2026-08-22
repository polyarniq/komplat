package ru.komplat.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.komplat.presentation.screens.company.CompanyDetailScreen
import ru.komplat.presentation.screens.company.CompanyListScreen
import ru.komplat.presentation.screens.expense.ExpenseDetailScreen
import ru.komplat.presentation.screens.expense.ExpenseListScreen
import ru.komplat.presentation.screens.home.HomeScreen
import ru.komplat.presentation.screens.settings.SettingsScreen
import ru.komplat.presentation.screens.settings.ServiceTypesScreen
import ru.komplat.presentation.screens.statistics.StatisticsScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Home : Screen("home", "Главная", Icons.Default.Home)
    object Statistics : Screen("statistics", "Статистика", Icons.Default.PieChart)
    object Companies : Screen("companies", "Компании", Icons.Default.Business)
    object Settings : Screen("settings", "Настройки", Icons.Default.Settings)
    object ExpenseList : Screen("expense_list/{period}", "Расходы", null) {
        fun createRoute(period: String) = "expense_list/$period"
    }
    object ExpenseDetail : Screen("expense_detail/{expenseId}", "Расход", null) {
        fun createRoute(expenseId: Long) = "expense_detail/$expenseId"
        fun createNew() = "expense_detail/-1"
    }
    object CompanyDetail : Screen("company_detail/{companyId}", "Компания", null) {
        fun createRoute(companyId: Long) = "company_detail/$companyId"
        fun createNew() = "company_detail/-1"
    }
    object ServiceTypes : Screen("service_types", "Типы услуг", null)
}

val bottomNavItems = listOf(Screen.Home, Screen.Statistics, Screen.Companies, Screen.Settings)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToExpenseDetail = { expenseId ->
                        navController.navigate(Screen.ExpenseDetail.createRoute(expenseId))
                    },
                    onAddExpense = {
                        navController.navigate(Screen.ExpenseDetail.createNew())
                    }
                )
            }

            composable(Screen.Statistics.route) {
                StatisticsScreen()
            }

            composable(Screen.Companies.route) {
                CompanyListScreen(
                    onNavigateToCompanyDetail = { companyId ->
                        navController.navigate(Screen.CompanyDetail.createRoute(companyId))
                    },
                    onAddCompany = {
                        navController.navigate(Screen.CompanyDetail.createNew())
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToServiceTypes = {
                        navController.navigate(Screen.ServiceTypes.route)
                    }
                )
            }

            composable(Screen.ServiceTypes.route) {
                ServiceTypesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ExpenseList.route,
                arguments = listOf(navArgument("period") { type = NavType.StringType })
            ) { backStackEntry ->
                val period = backStackEntry.arguments?.getString("period") ?: ""
                ExpenseListScreen(
                    period = period,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToExpenseDetail = { expenseId ->
                        navController.navigate(Screen.ExpenseDetail.createRoute(expenseId))
                    },
                    onAddExpense = {
                        navController.navigate(Screen.ExpenseDetail.createNew())
                    }
                )
            }

            composable(
                route = Screen.ExpenseDetail.route,
                arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
            ) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getLong("expenseId") ?: -1L
                ExpenseDetailScreen(
                    expenseId = expenseId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.CompanyDetail.route,
                arguments = listOf(navArgument("companyId") { type = NavType.LongType })
            ) { backStackEntry ->
                val companyId = backStackEntry.arguments?.getLong("companyId") ?: -1L
                CompanyDetailScreen(
                    companyId = companyId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
