package com.example.mangiaebasta.views

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.mangiaebasta.models.MenuModel
import com.example.mangiaebasta.viewmodels.MenuViewModel
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun MenuDetailScreen(
    menuId: Int,
    navController: androidx.navigation.NavHostController,
    menuViewModel: MenuViewModel
) {
    LaunchedEffect(menuId) { menuViewModel.loadMenu(menuId) }

    val menu by menuViewModel.selectedMenu.collectAsState()

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (menu == null) {
                CircularProgressIndicator()
            } else {
                MenuDetailContent(menu!!)
            }
        }
    }
}

@Composable
private fun MenuDetailContent(menu: MenuModel) {
    val imageBitmap = menu.image?.let { base64 ->
        runCatching {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }.onFailure {
            Log.e("MenuDetail", "Errore nella decodifica dell'immagine: ${it.message}", it)
        }.getOrNull()
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = menu.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                )
                Spacer(Modifier.height(12.dp))
            } else if (!menu.image.isNullOrEmpty()) {
                Text(
                    "Errore nel caricamento dell'immagine",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(Modifier.height(12.dp))
            } else {
                Text(
                    "Immagine non disponibile",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(Modifier.height(12.dp))
            }

            Text(menu.name, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(menu.longDescription, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Prezzo:", style = MaterialTheme.typography.titleMedium)
                Text("€${"%.2f".format(menu.price)}", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Consegna:", style = MaterialTheme.typography.titleMedium)
                Text("${menu.deliveryTime} min", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
