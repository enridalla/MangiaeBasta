package com.example.mangiaebasta

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.mangiaebasta.viewmodels.MenuViewModel
import com.example.mangiaebasta.viewmodels.ProfileViewModel
import com.example.mangiaebasta.views.*


private const val PREF_NAME = "navigation_pref"
private const val KEY_LAST_ROUTE = "last_route"

/* ----------------------------- rotte ----------------------------- */
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Menu         : Screen("menu", "Menu", Icons.Filled.RestaurantMenu)
    object Order        : Screen("order", "Ordini", Icons.Filled.ShoppingCart)
    object ProfileInfo      : Screen("profile", "Profilo", Icons.Filled.Person)
    object ProfileEdit  : Screen("profile_edit", "Modifica Profilo", Icons.Filled.Person)
    object MenuDetail   : Screen("menu_detail/{menuId}", "Dettagli Menu", Icons.Filled.RestaurantMenu) {
        fun createRoute(menuId: Int) = "menu_detail/$menuId"
    }
}

/* -------------------- composable principale ---------------------- */
@Composable
fun Navigation(navController: NavHostController, startRoute: String) {
    val context = LocalContext.current
    val prefs = remember<android.content.SharedPreferences> { context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) }

    /* 🔄  salva ogni volta che la destination cambia */
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            entry.destination.route?.let { route ->
                prefs.edit().apply {
                    putString(KEY_LAST_ROUTE, route)
                    apply()
                }
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        topBar = { TopNavigationBar(navController, navBackStackEntry) },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavGraph(navController, innerPadding, startRoute)
    }
}

/* --------------------------- top bar ------------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopNavigationBar(
    navController: NavHostController,
    navBackStackEntry: NavBackStackEntry?
) {
    val currentRoute = navBackStackEntry?.destination?.route

    // Determina il titolo basato sulla rotta corrente
    val screenTitle = when {
        currentRoute == Screen.Menu.route -> Screen.Menu.label
        currentRoute == Screen.Order.route -> Screen.Order.label
        currentRoute == Screen.ProfileInfo.route -> Screen.ProfileInfo.label
        currentRoute == Screen.ProfileEdit.route -> Screen.ProfileEdit.label
        currentRoute?.startsWith("menu_detail") == true -> Screen.MenuDetail.label
        else -> "App Food"
    }

    // Controlla se siamo in una pagina primaria o secondaria
    val isMainRoute = currentRoute == Screen.Menu.route ||
            currentRoute == Screen.Order.route ||
            currentRoute == Screen.ProfileInfo.route

    CenterAlignedTopAppBar(
        title = { Text(screenTitle) },
        navigationIcon = {
            if (!isMainRoute) {
                IconButton(onClick = { navController.popBackStack() }) {
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

/* --------------------------- bottom bar ------------------------- */
@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = Color.White, tonalElevation = 4.dp) {
        listOf(Screen.Menu, Screen.Order, Screen.ProfileInfo).forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true }
                }
            )
        }
    }
}

/* ---------------------------- NavHost --------------------------- */
@Composable
private fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    startRoute: String
) {
    val menuVM: MenuViewModel = viewModel()
    val profileVM: ProfileViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startRoute,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Menu.route) {
            MenuListScreen(menuVM) { selectedId: Int ->
                navController.navigate(Screen.MenuDetail.createRoute(selectedId))
            }
        }

        composable(Screen.Order.route)       { OrderScreen() }
        // In your Navigation.kt file, change the problematic lines to:
        composable(Screen.ProfileInfo.route) { backStackEntry ->
            ProfileInfoScreen(
                navController = navController,
                profileViewModel = profileVM
            )
        }
        composable(Screen.ProfileEdit.route) { backStackEntry ->
            ProfileEditScreen(
                navController = navController,
                profileViewModel = profileVM
            )
        }

        /* ---- NUOVA DESTINAZIONE ---- */
        composable(
            route = Screen.MenuDetail.route,
            arguments = listOf(navArgument("menuId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("menuId") ?: return@composable
            MenuDetailScreen(menuId = id, navController = navController, menuViewModel = menuVM)
        }
    }
}
