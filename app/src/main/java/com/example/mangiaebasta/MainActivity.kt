package com.example.mangiaebasta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.mangiaebasta.models.DataStoreManager
import com.example.mangiaebasta.models.StorageManager
import com.example.mangiaebasta.ui.theme.MangiaEBastaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializza DataStoreManager
        DataStoreManager.initialize(applicationContext)
        // Inizializza StorageManager
        StorageManager.initialize(applicationContext)

        val startParams = intent.extras

        enableEdgeToEdge()

        setContent {
            MangiaEBastaTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // non serve più startRoute: lo legge Navigation da DataStore
                    Navigation(
                        navController = navController,
                    )
                }
            }
        }
    }
}