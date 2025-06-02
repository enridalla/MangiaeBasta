package com.example.mangiaebasta

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.mangiaebasta.viewmodels.OrderViewModel
import com.example.mangiaebasta.models.DataStoreManager
import kotlinx.coroutines.launch
import android.util.Log

/**
 * Classe che definisce tutte le schermate dell'app
 * Ogni schermata ha:
 * - route: l'URL interno per la navigazione
 * - label: il nome mostrato nell'interfaccia
 * - icon: l'icona mostrata nella bottom bar
 * - parent: la schermata genitore (per la navigazione gerarchica)
 */
sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val parent: Screen? = null
) {
    // Parametri opzionali per route dinamiche (es. /menu/123)
    open val routeParams: Map<String, NavType<*>> = emptyMap()

    // Indica se la schermata deve apparire nella bottom navigation
    open val showInBottomNav: Boolean = false

    // =============== DEFINIZIONE SCHERMATE ===============

    object MenuList : Screen(
        route = "menu",
        label = "Menu",
        icon = Icons.Filled.RestaurantMenu
    ) {
        override val showInBottomNav = true
    }

    object Order : Screen(
        route = "order",
        label = "Ordini",
        icon = Icons.Filled.ShoppingCart
    ) {
        override val showInBottomNav = true
    }

    object ProfileInfo : Screen(
        route = "profile",
        label = "Profilo",
        icon = Icons.Filled.Person
    ) {
        override val showInBottomNav = true
    }

    object ProfileEdit : Screen(
        route = "profile_edit",
        label = "Modifica Profilo",
        icon = Icons.Filled.Person,
        parent = ProfileInfo
    )

    object MenuDetail : Screen(
        route = "menu_detail/{menuId}",
        label = "Dettagli Menu",
        icon = Icons.Filled.RestaurantMenu,
        parent = MenuList
    ) {
        override val routeParams = mapOf("menuId" to NavType.IntType)

        // Funzione helper per creare la route con parametri
        fun createRoute(menuId: Int) = "menu_detail/$menuId"
    }

    /* SETTINGS SCREEN
    object Settings : Screen(
        route = "settings",
        label = "Impostazioni",
        icon = Icons.Filled.Settings
    ) {
        override val showInBottomNav = true
    }
    */

    /* ORDER DETAIL SCREEN
    object OrderDetail : Screen(
    route = "order_detail/{orderId}/{status}",  // Parametri: orderId (Int) e status (String)
    label = "Dettagli Ordine",
    icon = Icons.Filled.ShoppingCart,
    parent = Order  // Schermata genitore
) {
    // Definisci i tipi dei parametri
    override val routeParams = mapOf(
        "orderId" to NavType.IntType,
        "status" to NavType.StringType
    )

    // Funzione helper per creare la route con parametri
    fun createRoute(orderId: Int, status: String) = "order_detail/$orderId/$status"

    // Funzioni helper per estrarre parametri dalla route
    fun extractOrderIdFromRoute(route: String): Int? {
        return route.substringAfter("order_detail/")
                   .substringBefore("/")
                   .toIntOrNull()
    }

    fun extractStatusFromRoute(route: String): String? {
        return route.substringAfterLast("/")
                   .takeIf { it.isNotBlank() }
    }
     */


    // =============== FUNZIONI UTILI ===============

    companion object {
        /**
         * Restituisce tutte le schermate definite
         */
        fun getAllScreens(): List<Screen> = listOf(
            MenuList,
            Order,
            ProfileInfo,
            ProfileEdit,
            MenuDetail,
            //Settings,
            //OrderDetail
        )

        /**
         * Restituisce solo le schermate da mostrare nella bottom navigation
         */
        fun getBottomNavScreens(): List<Screen> =
            getAllScreens().filter { it.showInBottomNav }

        /**
         * Trova una schermata dalla sua route
         * Gestisce sia route esatte che route con parametri
         */
        fun findScreenByRoute(route: String?): Screen? {
            if (route == null) return null

            Log.d("Navigation", "🔍 Cercando schermata per route: '$route'")

            // Prima cerca corrispondenza esatta
            val exactMatch = getAllScreens().find { it.route == route }
            if (exactMatch != null) {
                Log.d("Navigation", "✅ Trovata corrispondenza esatta: ${exactMatch.javaClass.simpleName}")
                return exactMatch
            }

            // Poi cerca tramite pattern per route con parametri
            val patternMatch = getAllScreens().find { screen ->
                if (screen.routeParams.isEmpty()) return@find false

                val routePattern = screen.route.substringBefore("/")
                val currentRoutePattern = route.substringBefore("/")

                routePattern == currentRoutePattern
            }

            if (patternMatch != null) {
                Log.d("Navigation", "✅ Trovata corrispondenza pattern: ${patternMatch.javaClass.simpleName}")
            } else {
                Log.d("Navigation", "❌ Nessuna schermata trovata per route: '$route'")
            }

            return patternMatch
        }

        /**
         * Verifica se una route è valida e i suoi parametri sono corretti
         */
        fun isRouteValid(route: String): Boolean {
            Log.d("Navigation", "🔍 Validando route: '$route'")

            val screen = findScreenByRoute(route)
            if (screen == null) {
                Log.d("Navigation", "❌ Route non trovata")
                return false
            }

            // Se non ha parametri, è sempre valida
            if (screen.routeParams.isEmpty()) {
                Log.d("Navigation", "✅ Route senza parametri, valida")
                return true
            }

            // Valida ogni parametro
            val allParamsValid = screen.routeParams.all { (paramName, paramType) ->
                val paramValue = extractParameterFromRoute(route, paramName, screen)

                val isValid = when (paramType) {
                    NavType.IntType -> paramValue?.toIntOrNull()?.let { it > 0 } ?: false
                    NavType.StringType -> !paramValue.isNullOrBlank()
                    else -> false
                }

                Log.d("Navigation", "📝 Parametro '$paramName': '$paramValue' -> ${if (isValid) "✅" else "❌"}")
                isValid
            }

            Log.d("Navigation", "🎯 Route '$route' è ${if (allParamsValid) "valida" else "non valida"}")
            return allParamsValid
        }

        /**
         * Estrae un parametro specifico da una route
         */
        private fun extractParameterFromRoute(
            route: String,
            paramName: String,
            screen: Screen
        ): String? {
            val template = screen.route
            val paramPlaceholder = "{$paramName}"

            val paramStartIndex = template.indexOf(paramPlaceholder)
            if (paramStartIndex == -1) return null

            val beforeParam = template.substring(0, paramStartIndex)
            val afterParam = template.substring(paramStartIndex + paramPlaceholder.length)

            val valueStart = route.indexOf(beforeParam) + beforeParam.length
            val valueEnd = if (afterParam.isNotEmpty()) {
                route.indexOf(afterParam, valueStart)
            } else {
                route.length
            }

            return if (valueStart >= 0 && valueEnd > valueStart) {
                route.substring(valueStart, valueEnd)
            } else null
        }

        fun extractMenuIdFromRoute(route: String) = route.substringAfter("menu_detail/").toIntOrNull()
    }
}

/**
 * =============== GESTIONE DELLA NAVIGAZIONE ===============
 */

/**
 * Manager per la persistenza dell'ultima route visitata
 */
class NavigationStateManager {
    suspend fun saveCurrentRoute(route: String) {
        try {
            Log.d("Navigation", "💾 Salvando route: '$route'")
            DataStoreManager.getInstance().saveLastRoute(route)
        } catch (e: Exception) {
            Log.e("Navigation", "❌ Errore nel salvare la route: ${e.message}")
        }
    }

    suspend fun getLastSavedRoute(): String {
        return try {
            val savedRoute = DataStoreManager.getInstance().getLastRoute()
            Log.d("Navigation", "📂 Route salvata caricata: '$savedRoute'")

            if (Screen.isRouteValid(savedRoute)) {
                savedRoute
            } else {
                Log.d("Navigation", "⚠️ Route salvata non valida, usando default")
                Screen.MenuList.route
            }
        } catch (e: Exception) {
            Log.e("Navigation", "❌ Errore nel caricare la route: ${e.message}")
            Screen.MenuList.route
        }
    }
}

/**
 * =============== COMPONENTI UI PRINCIPALI ===============
 */

@Composable
fun Navigation(navController: NavHostController = rememberNavController()) {
    val scope = rememberCoroutineScope()
    val navigationManager = remember { NavigationStateManager() }

    // Stati per la gestione dell'inizializzazione
    var isInitialized by remember { mutableStateOf(false) }
    var startDestination by remember { mutableStateOf(Screen.MenuList.route) }
    var initialRoute by remember { mutableStateOf<String?>(null) }

    // =============== INIZIALIZZAZIONE ALL'AVVIO ===============
    LaunchedEffect(Unit) {
        scope.launch {
            Log.d("Navigation", "🚀 Inizializzando navigazione...")

            val lastRoute = navigationManager.getLastSavedRoute()

            // Per route con parametri, usa il template come startDestination
            if (lastRoute.contains("/") && lastRoute != Screen.ProfileEdit.route) {
                val screen = Screen.findScreenByRoute(lastRoute)
                if (screen != null && screen.routeParams.isNotEmpty()) {
                    startDestination = screen.route
                    initialRoute = lastRoute
                    Log.d("Navigation", "🎯 Route con parametri: template='$startDestination', concrete='$initialRoute'")
                } else {
                    startDestination = lastRoute
                }
            } else {
                startDestination = lastRoute
            }

            isInitialized = true
            Log.d("Navigation", "✅ Navigazione inizializzata con destinazione: '$startDestination'")
        }
    }

    // Naviga alla route iniziale dopo l'inizializzazione
    LaunchedEffect(isInitialized, initialRoute) {
        if (isInitialized && initialRoute != null) {
            Log.d("Navigation", "🎯 Navigando alla route iniziale: '$initialRoute'")
            navController.navigate(initialRoute!!) {
                popUpTo(startDestination) { inclusive = true }
            }
        }
    }

    // =============== SALVATAGGIO AUTOMATICO DELLA ROUTE ===============
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentBackStackEntry?.destination?.route) {
        if (!isInitialized) return@LaunchedEffect

        val currentRoute = currentBackStackEntry?.destination?.route
        if (currentRoute == null) return@LaunchedEffect

        scope.launch {
            // Costruisce la route completa includendo i parametri
            val fullRoute = buildCompleteRoute(currentBackStackEntry)
            navigationManager.saveCurrentRoute(fullRoute)
        }
    }

    // =============== UI PRINCIPALE ===============
    if (!isInitialized) {
        LoadingScreen()
        return
    }

    Scaffold(
        topBar = { TopNavigationBar(navController, currentBackStackEntry) },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        NavGraph(navController, padding, startDestination)
    }
}

/**
 * Costruisce la route completa includendo i parametri attuali
 */
private fun buildCompleteRoute(backStackEntry: NavBackStackEntry?): String {
    val route = backStackEntry?.destination?.route ?: return Screen.MenuList.route
    val arguments = backStackEntry.arguments

    return when {
        route.contains("menu_detail/{menuId}") -> {
            val menuId = arguments?.getInt("menuId", -1) ?: -1
            if (menuId > 0) "menu_detail/$menuId" else Screen.MenuList.route
        }
        /* ORDER DETAIL SCREEN
        route.contains("order_detail/{orderId}/{status}") -> {
            val orderId = arguments?.getInt("orderId", -1) ?: -1
            val status = arguments?.getString("status") ?: "unknown"
            if (orderId > 0) "order_detail/$orderId/$status" else Screen.Order.route
        }
         */
        else -> route
    }
}

/**
 * =============== GRAFO DI NAVIGAZIONE ===============
 */

@Composable
private fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    startDestination: String
) {
    // ViewModels condivisi
    val menuViewModel: MenuViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding)
    ) {

        composable(Screen.MenuList.route) {
            MenuListScreen(menuViewModel) { selectedMenuId ->
                navController.navigate(Screen.MenuDetail.createRoute(selectedMenuId))
            }
        }

        composable(Screen.Order.route) {
            OrderScreen(navController, orderViewModel)
        }

        composable(Screen.ProfileInfo.route) {
            ProfileInfoScreen(navController, profileViewModel)
        }

        /*
        SETTING SCREEN
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
        */

        composable(Screen.ProfileEdit.route) {
            ProfileEditScreen(navController, profileViewModel)
        }

        composable(
            route = Screen.MenuDetail.route,
            arguments = listOf(
                navArgument("menuId") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )

        ) { backStackEntry ->
            val menuId = backStackEntry.arguments?.getInt("menuId") ?: 0

            // Validazione parametri
            if (menuId <= 0) {
                Log.e("Navigation", "❌ ID menu non valido: $menuId")
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.MenuList.route) {
                        popUpTo(navController.graph.findStartDestination().id)
                    }
                }
                return@composable
            }

            MenuDetailScreen(
                menuId = menuId,
                navController = navController,
                menuViewModel = menuViewModel
            )
        }

        /*  ORDERS DETAIL SCREEN
        composable(
            route = OrderDetail.route,
            arguments = listOf(
                navArgument("orderId") {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument("status") {
                    type = NavType.StringType
                    defaultValue = "unknown"
                }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
            val status = backStackEntry.arguments?.getString("status") ?: "unknown"

            // Validazione parametri
            if (orderId <= 0) {
                Log.e("Navigation", "❌ ID ordine non valido: $orderId")
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Order.route) {
                        popUpTo(navController.graph.findStartDestination().id)
                    }
                }
                return@composable
            }

            // Renderizza la schermata
            OrderDetailScreen(
                orderId = orderId,
                status = status,
                navController = navController,
                orderViewModel = orderViewModel
            )
        }
         */
    }
}
