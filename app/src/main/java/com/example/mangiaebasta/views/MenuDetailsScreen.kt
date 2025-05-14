package com.example.mangiaebasta.views

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mangiaebasta.viewmodels.MenuDetailUiState
import com.example.mangiaebasta.viewmodels.MenuViewModel
import kotlinx.coroutines.launch

@Composable
fun MenuDetailScreen(
    menuId: Int,
    navController: NavHostController,
    menuViewModel: MenuViewModel
) {
    LaunchedEffect(menuId) { menuViewModel.loadMenu(menuId) }

    val menu by menuViewModel.selectedMenuUi.collectAsState()
    val isLoading by menuViewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isLoading || menu == null) {
                CircularProgressIndicator()
            } else {
                MenuDetailContent(menu!!) { id ->
                    scope.launch {
                        snackbarHostState.showSnackbar("Ordine effettuato per il menu #$id")
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuDetailContent(
    menu: MenuDetailUiState,
    onOrder: (Int) -> Unit
) {
    val imageBitmap = menu.imageBase64
        ?.let { Base64.decode(it, Base64.DEFAULT) }
        ?.let { bytes -> BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
        ?.asImageBitmap()

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        imageBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = menu.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.height(12.dp))
        }

        Text(menu.name, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(menu.longDescription, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Prezzo:", style = MaterialTheme.typography.titleMedium)
            Text(menu.priceText, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Consegna:", style = MaterialTheme.typography.titleMedium)
            Text(menu.deliveryTimeText, style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onOrder(menu.mid) },
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Text("Ordina ora")
        }
    }
}