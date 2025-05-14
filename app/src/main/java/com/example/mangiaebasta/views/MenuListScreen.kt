package com.example.mangiaebasta.views

import android.graphics.Bitmap
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.mangiaebasta.models.MenuModel
import com.example.mangiaebasta.viewmodels.MenuViewModel

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
    val imageBitmap = menu.image?.let { raw ->
        val base64 = raw.substringAfter(",", raw)
        try {
            val imageBytes = Base64.decode(base64, Base64.NO_WRAP)
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inMutable = true
            }
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
                ?.asImageBitmap()
        } catch (e: Exception) {
            Log.e("MenuCard", "Errore decodifica immagine: ${e.message}")
            null
        }
    }

    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onMenuSelected(menu.mid) }
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            imageBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = menu.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
                Spacer(Modifier.height(8.dp))
            } ?: run {
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