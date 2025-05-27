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
import com.example.mangiaebasta.viewmodels.OrderViewModel
import com.example.mangiaebasta.models.DataStoreManager
import kotlinx.coroutines.launch
import android.util.Log

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

        fun findScreenByRoute(route: String?): Screen? {
            Log.d("Navigation", "🔍 FIND_SCREEN - Looking for route: '$route'")

            return route?.let { fullRoute ->
                // Prima prova a trovare una corrispondenza esatta
                val exactMatch = getAllScreens().find { it.route == fullRoute }
                Log.d("Navigation", "🔍 FIND_SCREEN - Exact match: ${exactMatch?.javaClass?.simpleName}")

                exactMatch ?: run {
                    // Poi prova con il pattern matching (per route con parametri)
                    val patternMatch = getAllScreens().find { screen ->
                        val routePrefix = fullRoute.substringBefore("/")
                        val screenPrefix = screen.route.substringBefore("/")
                        val match = routePrefix == screenPrefix && screen.routeParams.isNotEmpty()
                        Log.d("Navigation", "🔍 FIND_SCREEN - Testing pattern '$screenPrefix' against '$routePrefix': $match")
                        match
                    }
                    Log.d("Navigation", "🔍 FIND_SCREEN - Pattern match: ${patternMatch?.javaClass?.simpleName}")
                    patternMatch
                }
            }
        }

        fun extractMenuIdFromRoute(route: String) = route.substringAfter("menu_detail/").toIntOrNull()

        /**
         * Valida se una route con parametri è valida
         * @param route La route completa (es. "menu_detail/123")
         * @return true se la route è valida, false altrimenti
         */
        fun isRouteValid(route: String): Boolean {
            Log.d("Navigation", "🔍 VALIDATION - Validating route: '$route'")

            val screen = findScreenByRoute(route)
            Log.d("Navigation", "🔍 VALIDATION - Found screen: ${screen?.javaClass?.simpleName}")

            if (screen == null) {
                Log.d("Navigation", "🔍 VALIDATION - Screen not found, returning false")
                return false
            }

            // Se non ha parametri, è sempre valida
            if (screen.routeParams.isEmpty()) {
                Log.d("Navigation", "🔍 VALIDATION - No params required, returning true")
                return true
            }

            // Controlla ogni parametro
            val isValid = screen.routeParams.all { (paramName, paramType) ->
                val paramValue = extractParamFromRoute(route, paramName)
                Log.d("Navigation", "🔍 VALIDATION - Param '$paramName': value='$paramValue', type=$paramType")

                val paramValid = when (paramType) {
                    NavType.IntType -> {
                        val intValue = paramValue?.toIntOrNull()
                        Log.d("Navigation", "🔍 VALIDATION - IntType validation: '$paramValue' -> $intValue")
                        intValue != null && intValue > 0
                    }
                    NavType.StringType -> {
                        val valid = !paramValue.isNullOrBlank()
                        Log.d("Navigation", "🔍 VALIDATION - StringType validation: '$paramValue' -> $valid")
                        valid
                    }
                    else -> {
                        Log.d("Navigation", "🔍 VALIDATION - Unknown param type, returning false")
                        false
                    }
                }
                Log.d("Navigation", "🔍 VALIDATION - Param '$paramName' is valid: $paramValid")
                paramValid
            }

            Log.d("Navigation", "🔍 VALIDATION - Overall route validity: $isValid")
            return isValid
        }

        /**
         * Estrae il valore di un parametro da una route
         */
        private fun extractParamFromRoute(route: String, paramName: String): String? {
            // Handle menu_detail routes specifically
            if (route.startsWith("menu_detail/") && paramName == "menuId") {
                return route.substringAfter("menu_detail/")
            }

            val screen = findScreenByRoute(route) ?: return null
            val template = screen.route

            // Trova la posizione del parametro nel template
            val paramPlaceholder = "{$paramName}"
            val paramIndex = template.indexOf(paramPlaceholder)
            if (paramIndex == -1) return null

            // Calcola la posizione nella route reale
            val beforeParam = template.substring(0, paramIndex)
            val afterParam = template.substring(paramIndex + paramPlaceholder.length)

            val routeAfterPrefix = route.removePrefix(beforeParam)
            return if (afterParam.isNotEmpty()) {
                routeAfterPrefix.removeSuffix(afterParam)
            } else {
                routeAfterPrefix
            }
        }
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

        return resolvedRoute
    }
}

@Composable
fun Navigation(navController: NavHostController = rememberNavController()) {
    val scope = rememberCoroutineScope()
    var startDestination by remember { mutableStateOf(Screen.MenuList.route) }
    var initialRoute by remember { mutableStateOf<String?>(null) }
    var isInitialized by remember { mutableStateOf(false) }

    // Carica l'ultima route salvata all'avvio
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val lastRoute = DataStoreManager.getInstance().getLastRoute()
                Log.d("Navigation", "🚀 STARTUP - lastRoute caricata: '$lastRoute'")

                val isValid = Screen.isRouteValid(lastRoute)
                Log.d("Navigation", "🚀 STARTUP - route valida: $isValid")

                if (isValid) {
                    // For parameterized routes, use the template as startDestination
                    // but save the concrete route for navigation
                    if (lastRoute.startsWith("menu_detail/")) {
                        startDestination = Screen.MenuDetail.route // Use template
                        initialRoute = lastRoute // Save concrete route for navigation
                        Log.d("Navigation", "🚀 STARTUP - usando template route con parametri")
                    } else {
                        startDestination = lastRoute
                    }
                    Log.d("Navigation", "🚀 STARTUP - startDestination impostata a: '$startDestination'")
                    Log.d("Navigation", "🚀 STARTUP - initialRoute impostata a: '$initialRoute'")
                } else {
                    Log.d("Navigation", "🚀 STARTUP - route non valida, usando fallback")
                    startDestination = Screen.MenuList.route
                }
            } catch (e: Exception) {
                Log.e("Navigation", "🚀 STARTUP - Errore caricamento route: ${e.message}")
                startDestination = Screen.MenuList.route
            } finally {
                isInitialized = true
                Log.d("Navigation", "🚀 STARTUP - Inizializzazione completata")
            }
        }
    }

    // Navigate to the initial route after NavHost is ready
    LaunchedEffect(isInitialized, initialRoute) {
        if (isInitialized && initialRoute != null) {
            Log.d("Navigation", "🚀 STARTUP - Navigating to initial route: $initialRoute")
            navController.navigate(initialRoute!!) {
                popUpTo(startDestination) { inclusive = true }
            }
        }
    }

    // Salva la route corrente quando cambia
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry?.destination?.route) {
        val currentRoute = navBackStackEntry?.destination?.route
        Log.d("Navigation", "📍 ROUTE CHANGE - currentRoute: '$currentRoute', isInitialized: $isInitialized")

        if (currentRoute != null && isInitialized) {
            scope.launch {
                try {
                    val fullRoute = navBackStackEntry?.destination?.route
                    val arguments = navBackStackEntry?.arguments

                    Log.d("Navigation", "📍 SAVING - fullRoute: '$fullRoute'")
                    Log.d("Navigation", "📍 SAVING - arguments: $arguments")

                    // Per le route con parametri, costruisci la route completa
                    val routeToSave = when {
                        fullRoute?.contains("menu_detail") == true -> {
                            val menuId = arguments?.getInt("menuId", -1) ?: -1
                            Log.d("Navigation", "📍 SAVING - menuId estratto: $menuId")

                            if (menuId > 0) {
                                val finalRoute = "menu_detail/$menuId"
                                Log.d("Navigation", "📍 SAVING - route costruita: '$finalRoute'")
                                finalRoute
                            } else {
                                Log.d("Navigation", "📍 SAVING - menuId non valido, usando fallback MenuList")
                                Screen.MenuList.route
                            }
                        }
                        else -> {
                            Log.d("Navigation", "📍 SAVING - route senza parametri: '$fullRoute'")
                            fullRoute ?: Screen.MenuList.route
                        }
                    }

                    Log.d("Navigation", "📍 SAVING - route finale da salvare: '$routeToSave'")
                    DataStoreManager.getInstance().saveLastRoute(routeToSave)
                    Log.d("Navigation", "📍 SAVING - route salvata con successo")

                } catch (e: Exception) {
                    Log.e("Navigation", "📍 SAVING - Errore salvataggio route: ${e.message}")
                }
            }
        }
    }

    // Aspetta che l'inizializzazione sia completata prima di mostrare la UI
    if (!isInitialized) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = { TopNavigationBar(navController, navBackStackEntry) },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        NavGraph(navController, padding, startDestination)
    }
}

@Composable
private fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    startDestination: String
) {
    val menuVM: MenuViewModel = viewModel()
    val profileVM: ProfileViewModel = viewModel()
    val orderVM: OrderViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.MenuList.route) {
            MenuListScreen(menuVM) { selectedId ->
                navController.navigate(Screen.MenuDetail.createRoute(selectedId))
            }
        }
        composable(Screen.Order.route) { OrderScreen(navController, orderVM) }
        composable(Screen.ProfileInfo.route) { ProfileInfoScreen(navController, profileVM) }
        composable(Screen.ProfileEdit.route) { ProfileEditScreen(navController, profileVM) }
        composable(
            route = Screen.MenuDetail.route,
            arguments = listOf(navArgument("menuId") {
                type = NavType.IntType
                defaultValue = 0  // Change default to 0 to distinguish from invalid
            })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("menuId") ?: 0
            Log.d("Navigation", "🎯 COMPOSABLE - MenuDetail with menuId: $id")

            // Add validation to prevent invalid IDs
            if (id <= 0) {
                Log.e("Navigation", "🎯 COMPOSABLE - Invalid menuId: $id, navigating to MenuList")
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.MenuList.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                    }
                }
                return@composable
            }

            MenuDetailScreen(menuId = id, navController = navController, menuViewModel = menuVM)
        }
    }
}