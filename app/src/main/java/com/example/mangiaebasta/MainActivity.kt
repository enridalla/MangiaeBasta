package com.example.mangiaebasta

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.mangiaebasta.ui.theme.MangiaEBastaTheme

private const val PREF_NAME = "navigation_pref"
private const val KEY_LAST_ROUTE = "last_route"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // recupera l'ultima rotta salvata (default = "menu")
        val lastRoute = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LAST_ROUTE, "menu") ?: "menu"

        enableEdgeToEdge()
        setContent {
            MangiaEBastaTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation(navController, startRoute = lastRoute)
                }
            }
        }
    }
}
