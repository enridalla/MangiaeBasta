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
import android.os.Bundle
import androidx.navigation.compose.rememberNavController
import com.example.mangiaebasta.viewmodels.MenuViewModel
import com.example.mangiaebasta.viewmodels.ProfileViewModel
import com.example.mangiaebasta.views.*


private const val PREF_NAME = "navigation_pref"
private const val KEY_LAST_ROUTE = "last_route"

/* ----------------------------- rotte con gerarchia ----------------------------- */
sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val parent: Screen? = null
) {
    // Schermate principali (root level)
    object Menu : Screen("menu", "Menu", Icons.Filled.RestaurantMenu)
    object Order : Screen("order", "Ordini", Icons.Filled.ShoppingCart)
    object ProfileInfo : Screen("profile", "Profilo", Icons.Filled.Person)

    // Schermate secondarie con gerarchia definita
    object ProfileEdit : Screen("profile_edit", "Modifica Profilo", Icons.Filled.Person, ProfileInfo)
    object MenuDetail : Screen("menu_detail/{menuId}", "Dettagli Menu", Icons.Filled.RestaurantMenu, Menu) {
        fun createRoute(menuId: Int) = "menu_detail/$menuId"
    }

    // Funzione di supporto per ottenere tutte le schermate
    companion object {
        fun getAllScreens(): List<Screen> = listOf(Menu, Order, ProfileInfo, ProfileEdit, MenuDetail)

        // Trova uno schermo dalla sua rotta
        fun findScreenByRoute(route: String?): Screen? {
            if (route == null) return null

            // Per MenuDetail abbiamo bisogno di gestire il parametro
            if (route.startsWith("menu_detail/")) {
                return MenuDetail
            }

            return getAllScreens().find { it.route == route }
        }

        // Estrae il parametro menuId dalla rotta
        fun extractMenuIdFromRoute(route: String): Int? {
            return if (route.startsWith("menu_detail/")) {
                try {
                    route.substringAfter("menu_detail/").toInt()
                } catch (e: Exception) {
                    null
                }
            } else null
        }
    }
}

/* -------------------- composable principale ---------------------- */
@Composable
fun Navigation(
    navController: NavHostController,
    startRoute: String,
    startParams: Bundle? = null
) {
    val context = LocalContext.current
    val prefs = remember<android.content.SharedPreferences> { context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) }

    /* 🔄  salva ogni volta che la destination cambia */
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            entry.destination.route?.let { route ->
                val currentRoute = entry.arguments?.getString("menuId")?.let { menuId ->
                    if (route.contains("{menuId}")) {
                        "menu_detail/$menuId"
                    } else route
                } ?: route

                prefs.edit().apply {
                    putString(KEY_LAST_ROUTE, currentRoute)
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
        NavGraph(navController, innerPadding, startRoute, startParams)
    }
}

/* --------------------------- top bar con navigazione gerarchica ------------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopNavigationBar(
    navController: NavHostController,
    navBackStackEntry: NavBackStackEntry?
) {
    val currentRoute = navBackStackEntry?.destination?.route
    val currentScreen = Screen.findScreenByRoute(currentRoute)

    // Determina il titolo basato sulla rotta corrente
    val screenTitle = currentScreen?.label ?: "App Food"

    // Controlla se siamo in una pagina primaria (senza parent)
    val isMainRoute = currentScreen?.parent == null

    CenterAlignedTopAppBar(
        title = { Text(screenTitle) },
        navigationIcon = {
            if (!isMainRoute) {
                IconButton(onClick = {
                    // Se lo schermo ha un parent definito, naviga a quello invece di semplicemente popBackStack
                    currentScreen?.parent?.let { parentScreen ->
                        // Per MenuDetail, dobbiamo gestire il caso speciale in cui potrebbe avere parametri
                        if (parentScreen.route.contains("{")) {
                            navController.navigate(parentScreen.route.substringBefore("{")) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate(parentScreen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                            }
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
    startRoute: String,
    startParams: Bundle? = null
) {
    val menuVM: MenuViewModel = viewModel()
    val profileVM: ProfileViewModel = viewModel()

    // Se abbiamo parametri di avvio e stiamo caricando la schermata dei dettagli del menu
    LaunchedEffect(startParams) {
        startParams?.getInt("menuId", -1)?.let { menuId ->
            if (menuId > 0 && startRoute == Screen.MenuDetail.route) {
                menuVM.loadMenu(menuId)
            }
        }
    }

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

        composable(Screen.Order.route) { OrderScreen() }

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

        composable(
            route = Screen.MenuDetail.route,
            arguments = listOf(navArgument("menuId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("menuId") ?:
            startParams?.getInt("menuId", -1) ?:
            return@composable

            MenuDetailScreen(menuId = id, navController = navController, menuViewModel = menuVM)
        }
    }
}