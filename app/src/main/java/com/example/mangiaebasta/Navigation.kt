package com.example.mangiaebasta

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.mangiaebasta.viewmodels.MenuViewModel
import com.example.mangiaebasta.viewmodels.ProfileViewModel
import com.example.mangiaebasta.views.*
import com.example.mangiaebasta.components.TopNavigationBar
import com.example.mangiaebasta.components.BottomNavigationBar

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val parent: Screen? = null
) {
    open val routeParams: Map<String, NavType<*>> = emptyMap()

    object MenuList : Screen("menu", "Menu", Icons.Filled.RestaurantMenu)
    object Order : Screen("order", "Ordini", Icons.Filled.ShoppingCart)
    object ProfileInfo : Screen("profile", "Profilo", Icons.Filled.Person)
    object ProfileEdit : Screen("profile_edit", "Modifica Profilo", Icons.Filled.Person, ProfileInfo)

    object MenuDetail : Screen("menu_detail/{menuId}", "Dettagli Menu", Icons.Filled.RestaurantMenu, MenuList) {
        override val routeParams = mapOf("menuId" to NavType.IntType)
        fun createRoute(menuId: Int) = "menu_detail/$menuId"
    }

    companion object {
        fun getAllScreens() = listOf(MenuList, Order, ProfileInfo, ProfileEdit, MenuDetail)

        fun findScreenByRoute(route: String?): Screen? = route?.let { fullRoute ->
            getAllScreens().find { it.route == fullRoute }
                ?: getAllScreens().find { screen ->
                    fullRoute.substringBefore("/") == screen.route.substringBefore("/")
                }
        }

        fun extractMenuIdFromRoute(route: String) = route.substringAfter("menu_detail/").toIntOrNull()
    }

    fun resolveRoute(arguments: Bundle?): String {
        if (routeParams.isEmpty()) return route
        var resolvedRoute = route
        routeParams.forEach { (paramName, paramType) ->
            val placeholder = "{$paramName}"
            if (resolvedRoute.contains(placeholder)) {
                when (paramType) {
                    NavType.IntType -> {
                        val value = arguments?.getInt(paramName, -1) ?: -1
                        if (value >= 0) {
                            resolvedRoute = resolvedRoute.replace(placeholder, value.toString())
                        }
                    }
                    NavType.StringType -> {
                        val value = arguments?.getString(paramName)
                        if (!value.isNullOrEmpty()) {
                            resolvedRoute = resolvedRoute.replace(placeholder, value)
                        }
                    }
                    else -> { /* Altri tipi non gestiti */ }
                }
            }
        }
        return if (resolvedRoute.contains("{")) parent?.route ?: MenuList.route else resolvedRoute
    }
}

@Composable
fun Navigation(navController: NavHostController = rememberNavController()) {
    val menuVM: MenuViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        topBar = { TopNavigationBar(navController, navBackStackEntry) },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        NavGraph(navController, padding)
    }
}

@Composable
private fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    val menuVM: MenuViewModel = viewModel()
    val profileVM: ProfileViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.MenuList.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.MenuList.route) {
            MenuListScreen(menuVM) { selectedId ->
                navController.navigate(Screen.MenuDetail.createRoute(selectedId))
            }
        }
        composable(Screen.Order.route) { OrderScreen() }
        composable(Screen.ProfileInfo.route) { ProfileInfoScreen(navController, profileVM) }
        composable(Screen.ProfileEdit.route) { ProfileEditScreen(navController, profileVM) }
        composable(
            route = Screen.MenuDetail.route,
            arguments = listOf(navArgument("menuId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("menuId") ?: return@composable
            MenuDetailScreen(menuId = id, navController = navController, menuViewModel = menuVM)
        }
    }
}
