package com.example.mangiaebasta

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.mangiaebasta.models.DataStoreManager
import com.example.mangiaebasta.models.StorageManager
import com.example.mangiaebasta.ui.theme.MangiaEBastaTheme
import com.example.mangiaebasta.viewmodels.AppState
import com.example.mangiaebasta.viewmodels.AppViewModel
import com.example.mangiaebasta.views.LoadingScreen
import com.example.mangiaebasta.views.PermissionDeniedScreen
import com.example.mangiaebasta.views.PermissionExplanationScreen

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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
fun AppContent(viewModel: AppViewModel = viewModel()) {
    val context = LocalContext.current
    val appState by viewModel.appState.collectAsState()

    // Launcher per la richiesta dei permessi
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("MainActivity", "Permission result: $isGranted")
        viewModel.onPermissionResult(isGranted)
    }

    // Effetto per controllare i permessi all'avvio
    LaunchedEffect(Unit) {
        Log.d("MainActivity", "Checking initial permissions")
        viewModel.checkLocationPermission(context)
    }

    // Effetto per gestire la richiesta dei permessi
    LaunchedEffect(appState) {
        if (appState == AppState.REQUESTING_PERMISSION) {
            Log.d("MainActivity", "Launching permission request")
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Rendering basato sullo stato
    when (appState) {
        AppState.CHECKING_PERMISSIONS -> {
            // Schermata di caricamento mentre controlliamo i permessi
            LoadingScreen()
        }

        AppState.SHOWING_PERMISSION_EXPLANATION -> {
            // Schermata che spiega perché servono i permessi
            PermissionExplanationScreen(
                onAccept = {
                    Log.d("MainActivity", "User accepted permission explanation")
                    viewModel.onPermissionExplanationAccepted()
                }
            )
        }

        AppState.REQUESTING_PERMISSION -> {
            // Manteniamo la schermata di spiegazione mentre aspettiamo la risposta dell'utente
            PermissionExplanationScreen(
                onAccept = {
                    // Non fare nulla, la richiesta è già in corso
                }
            )
        }

        AppState.PERMISSION_GRANTED -> {
            // I permessi sono stati concessi, mostra l'app normale
            Log.d("MainActivity", "Permission granted, showing main content")
            val navController = rememberNavController()
            Navigation(
                navController = navController,
            )
        }

        AppState.PERMISSION_DENIED -> {
            // I permessi sono stati negati, mostra la schermata di errore
            PermissionDeniedScreen(
                onRetry = {
                    Log.d("MainActivity", "User wants to retry permission")
                    viewModel.retryPermissionRequest()
                }
            )
        }
    }
}