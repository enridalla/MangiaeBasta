package com.example.mangiaebasta.views

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.mangiaebasta.viewmodels.MenuListItemUiState
import com.example.mangiaebasta.viewmodels.MenuViewModel

@Composable
fun MenuListScreen(
    menuViewModel: MenuViewModel,
    onMenuSelected: (Int) -> Unit
) {
    val menus by menuViewModel.menusUi.collectAsState()
    val isLoading by menuViewModel.isLoading.collectAsState()

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(menus, key = { it.mid }) { menu ->
            MenuCard(menu, onMenuSelected)
        }
    }
}

@Composable
private fun MenuCard(
    menu: MenuListItemUiState,
    onMenuSelected: (Int) -> Unit
) {
    val imageBitmap = menu.imageBase64
        ?.let { Base64.decode(it, Base64.NO_WRAP) }
        ?.let { bytes -> BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
        ?.asImageBitmap()

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onMenuSelected(menu.mid) }
    ) {
        Column(Modifier.padding(8.dp)) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = menu.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("Immagine non disponibile", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(8.dp))
            Text(menu.name, style = MaterialTheme.typography.titleLarge)
            Text(menu.shortDescription, style = MaterialTheme.typography.bodyMedium)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(menu.priceText, style = MaterialTheme.typography.titleMedium)
                Text(menu.deliveryTimeText, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}