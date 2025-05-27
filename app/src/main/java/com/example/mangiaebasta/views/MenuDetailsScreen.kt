package com.example.mangiaebasta.views

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mangiaebasta.models.DetailedMenuItemWithImage
import com.example.mangiaebasta.viewmodels.MenuViewModel
import kotlinx.coroutines.launch

@Composable
fun MenuDetailScreen(
    menuId: Int,
    navController: NavHostController,
    menuViewModel: MenuViewModel
) {
    // Stato per tracciare se è il primo caricamento
    var isFirstLoad by remember(menuId) { mutableStateOf(true) }

    /* ---------- trigger del fetch quando cambia l'id ---------- */
    LaunchedEffect(menuId) {
        menuViewModel.loadMenu(menuId)
        isFirstLoad = false
    }

    val menu by menuViewModel.selectedMenu.collectAsState()
    val isLoading by menuViewModel.isLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    errorDialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { errorDialogMessage = null },
            title = { Text("Errore Ordine") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { errorDialogMessage = null }) { Text("OK") }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {

            when {
                // Mostra loading solo se stiamo caricando E non abbiamo ancora dati
                isLoading && menu == null -> {
                    LoadingScreen()
                }
                // Mostra contenuto se abbiamo i dati (anche se stiamo ricaricando)
                menu != null -> {
                    MenuDetailContent(
                        menu = menu!!,
                        isRefreshing = isLoading,
                        onOrder = { id ->
                            scope.launch {
                                val error = menuViewModel.orderMenu(id)
                                if (error != null) {
                                    errorDialogMessage = error
                                } else {
                                    snackbarHostState.showSnackbar("Ordine effettuato con successo!")
                                }
                            }
                        }
                    )
                }
                // Mostra errore solo se non stiamo caricando e non abbiamo dati
                // Ma NON durante il primo caricamento
                !isLoading && !isFirstLoad -> {
                    ErrorMessage("Impossibile caricare i dettagli del menu")
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun MenuDetailContent(
    menu: DetailedMenuItemWithImage,
    isRefreshing: Boolean = false,
    onOrder: (Int) -> Unit
) {
    val imgBytes = menu.image?.let { Base64.decode(it, Base64.DEFAULT) }
    val imgBitmap = imgBytes
        ?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        ?.asImageBitmap()

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Indicatore di refresh in alto
        if (isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }

        imgBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = menu.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
            Spacer(Modifier.height(12.dp))
        }

        Text(menu.name, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(menu.longDescription, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("Prezzo:", style = MaterialTheme.typography.titleMedium)
            Text(
                "${menu.price}€",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("Consegna:", style = MaterialTheme.typography.titleMedium)
            Text("${menu.deliveryTime} min", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onOrder(menu.mid) },
            enabled = !isRefreshing,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Ordina ora")
            }
        }
    }
}
