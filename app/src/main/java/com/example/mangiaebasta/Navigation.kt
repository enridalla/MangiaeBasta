package com.example.mangiaebasta

import android.os.Bundle
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mangiaebasta.models.DataStoreManager
import com.example.mangiaebasta.viewmodels.MenuViewModel
import com.example.mangiaebasta.viewmodels.ProfileViewModel
import com.example.mangiaebasta.views.*
import kotlinx.coroutines.flow.collect

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val parent: Screen? = null
) {
    object MenuList : Screen("menu", "Menu", Icons.Filled.RestaurantMenu)
    object Order : Screen("order", "Ordini", Icons.Filled.ShoppingCart)
    object ProfileInfo : Screen("profile", "Profilo", Icons.Filled.Person)
    object ProfileEdit : Screen("profile_edit", "Modifica Profilo", Icons.Filled.Person, ProfileInfo)
    object MenuDetail : Screen("menu_detail/{menuId}", "Dettagli Menu", Icons.Filled.RestaurantMenu, MenuList) {
        fun createRoute(menuId: Int) = "menu_detail/$menuId"
    }

    companion object {
        fun getAllScreens() = listOf(MenuList, Order, ProfileInfo, ProfileEdit, MenuDetail)
        fun findScreenByRoute(route: String?) = route?.let {
            if (it.startsWith("menu_detail/")) MenuDetail
            else getAllScreens().find { screen -> screen.route == it }
        }
        fun extractMenuIdFromRoute(route: String) =
            route.substringAfter("menu_detail/").toIntOrNull()
    }
}

@Composable
fun Navigation(
    navController: NavHostController,
    startParams: Bundle? = null
) {
    val context = LocalContext.current

    val lastRoute by DataStoreManager
        .getLastRoute(context)
        .collectAsState(initial = Screen.MenuList.route)

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            val routeTemplate = entry.destination.route ?: return@collect
            val currentRoute = entry.arguments
                ?.getString("menuId")
                ?.let { id ->
                    if (routeTemplate.contains("{menuId}")) "menu_detail/$id"
                    else routeTemplate
                } ?: routeTemplate

            DataStoreManager.saveLastRoute(context, currentRoute)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        topBar = { TopNavigationBar(navController, navBackStackEntry) },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            innerPadding = innerPadding,
            startRoute = lastRoute,
            startParams = startParams
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopNavigationBar(
    navController: NavHostController,
    navBackStackEntry: NavBackStackEntry?
) {
    val currentRoute = navBackStackEntry?.destination?.route
    val currentScreen = Screen.findScreenByRoute(currentRoute)
    val screenTitle = currentScreen?.label ?: "App Food"
    val isMainRoute = currentScreen?.parent == null

    CenterAlignedTopAppBar(
        title = { Text(screenTitle) },
        navigationIcon = {
            if (!isMainRoute) {
                IconButton(onClick = {
                    currentScreen?.parent?.let { parent ->
                        val target = parent.route.substringBefore("{")
                        navController.navigate(target) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    } ?: navController.popBackStack()
                }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Torna indietro")
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = Color.White, tonalElevation = 4.dp) {
        listOf(Screen.MenuList, Screen.Order, Screen.ProfileInfo).forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
private fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    startRoute: String,
    startParams: Bundle? = null
) {
    val menuVM: MenuViewModel = viewModel()
    val profileVM: ProfileViewModel = viewModel()

    LaunchedEffect(startParams) {
        startParams?.getInt("menuId", -1)?.takeIf { it > 0 && startRoute == Screen.MenuDetail.route }
            ?.let { menuVM.loadMenu(it) }
    }

    NavHost(
        navController = navController,
        startDestination = startRoute,
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
            val id = backStackEntry.arguments?.getInt("menuId")
                ?: startParams?.getInt("menuId", -1)
                ?: return@composable
            MenuDetailScreen(menuId = id, navController = navController, menuViewModel = menuVM)
        }
    }
}