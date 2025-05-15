package com.example.mangiaebasta.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.mangiaebasta.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
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