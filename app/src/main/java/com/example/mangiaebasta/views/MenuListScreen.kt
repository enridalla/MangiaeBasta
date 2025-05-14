package com.example.mangiaebasta.views

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.mangiaebasta.models.MenuModel
import com.example.mangiaebasta.viewmodels.MenuViewModel
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun MenuListScreen(
    menuViewModel: MenuViewModel,
    onMenuSelected: (Int) -> Unit
) {
    val menus by menuViewModel.menus.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
    ) {
        items(menus, key = { it.mid }) { menu ->
            MenuCard(menu, onMenuSelected)
        }
    }
}

@Composable
private fun MenuCard(menu: MenuModel, onMenuSelected: (Int) -> Unit) {
    // Decodifica l'immagine direttamente come nel codice di esempio funzionante
    val imageBitmap = menu.image?.let { base64String ->
        try {
            // Importante: Non usiamo substringAfter qui, prendiamo direttamente la stringa base64
            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap()

            // Log per debug
            if (bitmap == null) {
                Log.e("MenuCard", "Bitmap decodificato è null. Lunghezza base64: ${base64String.length}")
            } else {
                Log.d("MenuCard", "Decodifica immagine riuscita")
            }

            bitmap
        } catch (e: Exception) {
            Log.e("MenuCard", "Errore nella decodifica dell'immagine: ${e.message}", e)
            null
        }
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onMenuSelected(menu.mid) }
    ) {
        Column {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = menu.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                )
                Spacer(Modifier.height(8.dp))
            } else if (!menu.image.isNullOrEmpty()) {
                Text(
                    "Errore nel caricamento dell'immagine",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Log.e("MenuCard", "Immagine non visualizzata. Primi 100 caratteri: ${menu.image.take(100)}")
            } else {
                Text(
                    "Immagine non disponibile",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Column(Modifier.padding(16.dp)) {
                Text(menu.name, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(menu.shortDescription, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("€${"%.2f".format(menu.price)}", style = MaterialTheme.typography.titleMedium)
                    Text("${menu.deliveryTime} min", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
