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
    /* ---------- trigger del fetch quando cambia l’id ---------- */
    LaunchedEffect(menuId) {
        menuViewModel.loadMenu(menuId)
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

            /* -- CONTENUTO: mostrato SOLO se non si sta caricando -- */
            if (!isLoading) {
                menu?.let {
                    MenuDetailContent(
                        menu = it,
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
                } ?: ErrorMessage("Impossibile caricare i dettagli del menu")
            }

            if (isLoading) {
                LoadingScreen()
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
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) { Text("Ordina ora") }
    }
}
