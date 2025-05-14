package com.example.mangiaebasta.views

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mangiaebasta.models.MenuModel
import com.example.mangiaebasta.viewmodels.MenuViewModel
import kotlinx.coroutines.launch

@Composable
fun MenuDetailScreen(
    menuId: Int,
    navController: NavHostController,
    menuViewModel: MenuViewModel
) {
    // Carica il menu selezionato
    LaunchedEffect(menuId) {
        menuViewModel.loadMenu(menuId)
    }

    val menu by menuViewModel.selectedMenu.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (menu == null) {
                CircularProgressIndicator()
            } else {
                menu?.let { nonNullMenu ->
                    MenuDetailContent(nonNullMenu) { id ->
                        // Ordina e mostra snackbar
                        scope.launch {
                            snackbarHostState.showSnackbar("Ordine effettuato per il menu #$id")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuDetailContent(
    menu: MenuModel,
    onOrder: (Int) -> Unit
) {
    val imageBitmap = menu.image?.let { base64 ->
        runCatching {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }.onFailure {
            Log.e("MenuDetail", "Errore nella decodifica dell'immagine: ${it.message}", it)
        }.getOrNull()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()) // abilita lo scroll
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        when {
            imageBitmap != null -> {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = menu.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.height(12.dp))
            }
            !menu.image.isNullOrEmpty() -> {
                Text(
                    text = "Errore nel caricamento dell'immagine",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(Modifier.height(12.dp))
            }
            else -> {
                Text(
                    text = "Immagine non disponibile",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        Text(
            text = menu.name,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = menu.longDescription,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Prezzo:", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "€${"%.2f".format(menu.price)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Consegna:", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${menu.deliveryTime} min",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onOrder(menu.mid) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Ordina ora")
        }
    }
}
