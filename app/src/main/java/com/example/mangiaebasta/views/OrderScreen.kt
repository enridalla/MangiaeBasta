package com.example.mangiaebasta.views

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.mangiaebasta.Screen
import com.example.mangiaebasta.viewmodels.OrderViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location

@Composable
fun OrderScreen(
    navController: NavHostController,
    orderViewModel: OrderViewModel
) {
    val isLoading by orderViewModel.isLoading.collectAsState()
    val error by orderViewModel.error.collectAsState()
    val orderStatus by orderViewModel.orderStatus.collectAsState()
    val lastOrder by orderViewModel.lastOrder.collectAsState()
    val userLocation by orderViewModel.userLocation.collectAsState()

    if (isLoading) {
        LoadingScreen()
        return
    }

    if (error != null) {
        ErrorScreen(error = error!!)
        return
    }

    if (lastOrder == null) {
        EmptyOrderScreen(navController)
        return
    }

    if (orderStatus == null) {
        LoadingScreen()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header con stato ordine
        OrderHeader(orderStatus = orderStatus!!)

        Spacer(modifier = Modifier.height(16.dp))

        // Mappa
        MapCard(
            orderStatus = orderStatus!!,
            lastOrder = lastOrder!!,
            userLocation = userLocation,
            orderViewModel = orderViewModel
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dettagli ordine
        OrderDetailsCard(
            orderStatus = orderStatus!!,
            lastOrder = lastOrder!!,
            orderViewModel = orderViewModel
        )
    }
}

@Composable
private fun OrderHeader(orderStatus: com.example.mangiaebasta.models.Order) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = when (orderStatus.status) {
                "ON_DELIVERY" -> "Ordine in Consegna"
                "DELIVERED" -> "Ordine Consegnato"
                else -> "Stato Ordine"
            },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .width(100.dp)
                .height(2.dp)
                .background(
                    Color(0xFF6554A4),
                    RoundedCornerShape(4.dp)
                )
        )
    }
}

@Composable
private fun MapCard(
    orderStatus: com.example.mangiaebasta.models.Order,
    lastOrder: com.example.mangiaebasta.models.DetailedMenuItemWithImage,
    userLocation: android.location.Location?,
    orderViewModel: OrderViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Text(
                text = "Tracciamento",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                MapboxMapView(
                    orderStatus = orderStatus,
                    lastOrder = lastOrder,
                    userLocation = userLocation,
                    orderViewModel = orderViewModel,
                    modifier = Modifier.fillMaxSize()
                )

                // Bottone per centrare la mappa
                FloatingActionButton(
                    onClick = {
                        // Implementare il centramento della mappa se necessario
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(48.dp),
                    containerColor = Color.White,
                    contentColor = Color(0xFF333333)
                ) {
                    Text(
                        text = "⊙",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MapboxMapView(
    orderStatus: com.example.mangiaebasta.models.Order,
    lastOrder: com.example.mangiaebasta.models.DetailedMenuItemWithImage,
    userLocation: android.location.Location?,
    orderViewModel: OrderViewModel,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            MapView(context).apply {
                getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
                    // Configura le annotazioni
                    val annotationApi = annotations
                    val pointManager = annotationApi.createPointAnnotationManager()

                    // Pin per la destinazione (rosso)
                    val deliveryPoint = Point.fromLngLat(
                        orderStatus.deliveryLocation.lng,
                        orderStatus.deliveryLocation.lat
                    )
                    pointManager.create(
                        PointAnnotationOptions()
                            .withPoint(deliveryPoint)
                            .withIconColor("#FF0000")
                            .withTextField("Consegna")
                    )

                    // Pin per il drone (blu) - solo se in consegna
                    if (orderStatus.status == "ON_DELIVERY") {
                        val dronePoint = Point.fromLngLat(
                            orderStatus.currentPosition.lng,
                            orderStatus.currentPosition.lat
                        )
                        pointManager.create(
                            PointAnnotationOptions()
                                .withPoint(dronePoint)
                                .withIconColor("#0000FF")
                                .withTextField("Drone")
                        )

                        // Pin per il ristorante (verde)
                        val restaurantPoint = Point.fromLngLat(
                            lastOrder.location.lng,
                            lastOrder.location.lat
                        )
                        pointManager.create(
                            PointAnnotationOptions()
                                .withPoint(restaurantPoint)
                                .withIconColor("#00FF00")
                                .withTextField("Ristorante")
                        )

                        // Linea tra drone e destinazione
                        val lineManager = annotationApi.createPolylineAnnotationManager()
                        val pathCoordinates = orderViewModel.getPathCoordinates()
                        if (pathCoordinates.isNotEmpty()) {
                            val points = pathCoordinates.map { (lat, lng) ->
                                Point.fromLngLat(lng, lat)
                            }
                            lineManager.create(
                                PolylineAnnotationOptions()
                                    .withPoints(points)
                                    .withLineColor("#0000FF")
                                    .withLineWidth(3.0)
                            )
                        }
                    }

                    // Pin per la posizione utente se disponibile
                    userLocation?.let { location ->
                        val userPoint = Point.fromLngLat(location.longitude, location.latitude)
                        pointManager.create(
                            PointAnnotationOptions()
                                .withPoint(userPoint)
                                .withIconColor("#FF6600")
                                .withTextField("Tu")
                        )
                    }

                    // Centra la mappa
                    val centerCoordinates = orderViewModel.getCenterCoordinates()
                    if (centerCoordinates != null) {
                        val cameraOptions = CameraOptions.Builder()
                            .center(Point.fromLngLat(centerCoordinates.second, centerCoordinates.first))
                            .zoom(orderViewModel.getMapZoomLevel())
                            .build()
                        getMapboxMap().setCamera(cameraOptions)
                    }
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun OrderDetailsCard(
    orderStatus: com.example.mangiaebasta.models.Order,
    lastOrder: com.example.mangiaebasta.models.DetailedMenuItemWithImage,
    orderViewModel: OrderViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Dettagli Ordine",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hai ordinato
            DetailRow(
                label = "Hai ordinato:",
                value = "${lastOrder.name} (${lastOrder.price}€)"
            )

            // Stato
            DetailRow(
                label = "Stato:",
                value = when (orderStatus.status) {
                    "ON_DELIVERY" -> "In Consegna"
                    "DELIVERED" -> "Consegnato"
                    else -> orderStatus.status
                }
            )

            // Tempo stimato o ora di consegna
            if (orderStatus.status == "ON_DELIVERY") {
                DetailRow(
                    label = "Tempo stimato:",
                    value = orderViewModel.getEstimatedTime()
                )
            } else {
                DetailRow(
                    label = "Ora di consegna:",
                    value = orderViewModel.formatDeliveryTime(orderStatus.expectedDeliveryTimestamp)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color(0xFF555555)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
private fun EmptyOrderScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Nessun Ordine Effettuato",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Al momento non hai ancora effettuato nessun ordine. Esplora il nostro menu e inizia a ordinare i tuoi piatti preferiti!",
            fontSize = 16.sp,
            color = Color(0xFF555555),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate(Screen.MenuList.route) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Vai al Menu")
        }
    }
}

@Composable
private fun ErrorScreen(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = error,
            color = Color.Red,
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}