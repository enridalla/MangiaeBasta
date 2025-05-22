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
import com.example.mangiaebasta.viewmodels.OrderStatus
import kotlinx.coroutines.launch

@Composable
fun MenuDetailScreen(
    menuId: Int,
    navController: NavHostController,
    menuViewModel: MenuViewModel
) {
    LaunchedEffect(menuId) {
        menuViewModel.loadMenu(menuId)
    }

    val menu by menuViewModel.selectedMenu.collectAsState()
    val isLoading by menuViewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isLoading) {
                LoadingScreen()
            } else if (menu == null) {
                ErrorMessage("Impossibile caricare i dettagli del menu")
            } else {
                MenuDetailContent(
                    menu = menu!!,
                    isOrderLoading = isLoading,
                    onOrder = { id ->
                        scope.launch {
                            menuViewModel.orderMenu(id)
                        }
                    }
                )
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
    isOrderLoading: Boolean,
    onOrder: (Int) -> Unit
) {
    // decodifica Base64 → Bitmap → ImageBitmap
    val imageBitmap = menu.image
        ?.let { Base64.decode(it, Base64.DEFAULT) }
        ?.let { bytes -> BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
        ?.asImageBitmap()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
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
            )
            Spacer(Modifier.height(12.dp))
        }

        Text(menu.name, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(menu.longDescription, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Prezzo:", style = MaterialTheme.typography.titleMedium)
            Text(
                menu.price.toString() + "€",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Consegna:", style = MaterialTheme.typography.titleMedium)
            Text("${menu.deliveryTime} min", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onOrder(menu.mid) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isOrderLoading
        ) {
            if (isOrderLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Ordina ora")
            }
        }
    }
}