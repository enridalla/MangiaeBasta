package com.example.mangiaebasta.views

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mangiaebasta.Screen
import com.example.mangiaebasta.viewmodels.OrderViewModel
import com.example.mangiaebasta.views.EmptyOrderScreen
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage


@Composable
fun OrderScreen(
    navController: NavHostController,
    orderViewModel: OrderViewModel
) {
    val isLoading by orderViewModel.isLoading.collectAsState()
    val orderStatus by orderViewModel.orderStatus.collectAsState()
    val lastOrder by orderViewModel.lastOrder.collectAsState()
    val userLocation by orderViewModel.userLocation.collectAsState()
    val isRefreshingAutomatically by orderViewModel.isRefreshingAutomatically.collectAsState()

    // NUOVA AGGIUNTA: Refresh automatico all'apertura della pagina
    LaunchedEffect(Unit) {
        orderViewModel.refreshOrderData()
    }

    if (isLoading) {
        LoadingScreen()
        return
    }

    if (lastOrder == null || orderStatus == null) {
        EmptyOrderScreen(navController)
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
        OrderHeader(
            orderStatus = orderStatus!!,
            orderViewModel = orderViewModel,
            isRefreshingAutomatically = isRefreshingAutomatically,
            onRefresh = { orderViewModel.refreshOrderData() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Messaggio di stato
        OrderStatusMessage(
            orderStatus = orderStatus!!,
            orderViewModel = orderViewModel
        )

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

        // Bottone per nuovo ordine se completato
        if (orderStatus!!.status == "COMPLETED") {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate(Screen.MenuList.route) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    text = "Ordina di Nuovo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OrderHeader(
    orderStatus: com.example.mangiaebasta.models.Order,
    orderViewModel: OrderViewModel,
    isRefreshingAutomatically: Boolean,
    onRefresh: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Icona di stato
        Icon(
            imageVector = when (orderStatus.status) {
                "ON_DELIVERY" -> Icons.Default.LocationOn
                "COMPLETED" -> Icons.Default.CheckCircle
                else -> Icons.Default.LocationOn
            },
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = orderViewModel.getOrderStatusColor()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (orderStatus.status) {
                "ON_DELIVERY" -> "Ordine in Consegna"
                "COMPLETED" -> "Ordine Completato"
                else -> "Stato Ordine"
            },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Indicatore di refresh automatico
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isRefreshingAutomatically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = orderViewModel.getOrderStatusColor()
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Aggiornamento automatico ogni 5s",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
            } else {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Aggiorna",
                        tint = Color(0xFF666666)
                    )
                }
                Text(
                    text = "Tocca per aggiornare",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .width(100.dp)
                .height(2.dp)
                .background(
                    orderViewModel.getOrderStatusColor(),
                    RoundedCornerShape(4.dp)
                )
        )
    }
}

@Composable
private fun OrderStatusMessage(
    orderStatus: com.example.mangiaebasta.models.Order,
    orderViewModel: OrderViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = orderViewModel.getOrderStatusColor().copy(alpha = 0.1f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (orderStatus.status) {
                        "ON_DELIVERY" -> "🚁 Il tuo ordine è in viaggio!"
                        "COMPLETED" -> "✅ Ordine consegnato con successo!"
                        else -> "📦 ${orderViewModel.getOrderStatusMessage()}"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )

                if (orderStatus.status == "ON_DELIVERY") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Il drone sta arrivando alla tua posizione",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )

                    val estimatedTime = orderViewModel.getEstimatedTime()
                    if (estimatedTime != "N/A") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = orderViewModel.getOrderStatusColor()
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tempo stimato: $estimatedTime",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = orderViewModel.getOrderStatusColor()
                            )
                        }
                    }
                } else if (orderStatus.status == "COMPLETED") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Speriamo ti sia piaciato il tuo pasto! 🍕",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun MapCard(
    orderStatus: com.example.mangiaebasta.models.Order,
    lastOrder: com.example.mangiaebasta.models.DetailedMenuItemWithImage,
    userLocation: android.location.Location?,
    orderViewModel: OrderViewModel
) {
    val mapViewPortState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(0.0, 0.0))
            zoom(14.0)
        }
    }

    // Configurazione iniziale della mappa
    LaunchedEffect(orderStatus) {
        val (center, zoom) = when (orderStatus.status) {
            "ON_DELIVERY" -> {
                val droneLat = orderStatus.currentPosition?.lat ?: 0.0
                val droneLng = orderStatus.currentPosition?.lng ?: 0.0
                Pair(droneLat, droneLng) to 14.0
            }
            "COMPLETED" -> {
                val lat = orderStatus.deliveryLocation?.lat ?: 0.0
                val lng = orderStatus.deliveryLocation?.lng ?: 0.0
                Pair(lat, lng) to 15.0
            }
            else -> {
                val lat = orderStatus.deliveryLocation?.lat ?: 0.0
                val lng = orderStatus.deliveryLocation?.lng ?: 0.0
                Pair(lat, lng) to 14.0
            }
        }

        mapViewPortState.setCameraOptions {
            center(Point.fromLngLat(center.second, center.first))
            zoom(zoom)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewPortState
        ) {
            // Icone diverse per ogni tipo di marker
            val restaurantIcon = rememberIconImage(
                key = "restaurant",
                painter = painterResource(id = android.R.drawable.star_big_on) // Stella per ristorante
            )

            val homeIcon = rememberIconImage(
                key = "home",
                painter = painterResource(id = android.R.drawable.ic_menu_mylocation) // Icona valida
            )

            val droneIcon = rememberIconImage(
                key = "drone",
                painter = painterResource(id = android.R.drawable.ic_menu_compass) // Icona valida
            )

            val userIcon = rememberIconImage(
                key = "user",
                painter = painterResource(id = android.R.drawable.presence_online) // Punto verde per utente
            )

            // 🏪 Ristorante (stella)
            PointAnnotation(
                point = Point.fromLngLat(lastOrder.location.lng, lastOrder.location.lat)
            ) {
                iconImage = restaurantIcon
                iconSize = 1.2
            }

            // 🏠 Destinazione (casa)
            orderStatus.deliveryLocation?.let { destination ->
                PointAnnotation(
                    point = Point.fromLngLat(destination.lng, destination.lat)
                ) {
                    iconImage = homeIcon
                    iconSize = 1.0
                }
            }

            // 🚁 Drone (triangolo)
            if (orderStatus.status == "ON_DELIVERY") {
                val droneLat = orderStatus.currentPosition?.lat ?: 0.0
                val droneLng = orderStatus.currentPosition?.lng ?: 0.0

                PointAnnotation(
                    point = Point.fromLngLat(droneLng, droneLat)
                ) {
                    iconImage = droneIcon
                    iconSize = 1.5
                }

                // Linea dal drone alla destinazione
                orderStatus.deliveryLocation?.let { destination ->
                    PolylineAnnotation(
                        points = listOf(
                            Point.fromLngLat(droneLng, droneLat),
                            Point.fromLngLat(destination.lng, destination.lat)
                        )
                    ) {
                        lineColor = Color(0xFFFF9800)
                        lineWidth = 3.0
                        lineOpacity = 0.8
                    }
                }
            }

            // 📍 Posizione utente (punto verde)
            userLocation?.let { location ->
                PointAnnotation(
                    point = Point.fromLngLat(location.longitude, location.latitude)
                ) {
                    iconImage = userIcon
                    iconSize = 0.8
                }
            }
        }
    }
}

@Composable
private fun OrderDetailsCard(
    orderStatus: com.example.mangiaebasta.models.Order,
    lastOrder: com.example.mangiaebasta.models.DetailedMenuItemWithImage,
    orderViewModel: OrderViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Dettagli Ordine",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Immagine e nome del prodotto
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val productImageBitmap = remember(lastOrder.image) {
                    try {
                        val imageBytes = Base64.decode(lastOrder.image, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap()
                    } catch (e: Exception) {
                        null
                    }
                }

                if (productImageBitmap != null) {
                    Image(
                        bitmap = productImageBitmap,
                        contentDescription = lastOrder.name,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = Color(0xFF666666)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = lastOrder.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "€${String.format("%.2f", lastOrder.price)}",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Informazioni di consegna con coordinate troncate
            orderStatus.deliveryLocation?.let { location ->
                OrderInfoRow(
                    icon = Icons.Default.Home,
                    label = "Destinazione",
                    value = "${String.format("%.4f", location.lat)}, ${String.format("%.4f", location.lng)}"
                )
            }

            if (orderStatus.status == "ON_DELIVERY") {
                orderStatus.currentPosition?.let { position ->
                    OrderInfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "Posizione drone",
                        value = "${String.format("%.4f", position.lat)}, ${String.format("%.4f", position.lng)}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OrderInfoRow(
                icon = Icons.Default.AccessTime,
                label = if (orderStatus.status == "COMPLETED") "Consegnato alle" else "Consegna prevista",
                value = orderViewModel.formatDeliveryTime(orderStatus.expectedDeliveryTimestamp)
            )

            // Distanza drone-destinazione se in consegna
            if (orderStatus.status == "ON_DELIVERY") {
                val distance = orderViewModel.calculateDistance(
                    orderStatus.currentPosition?.lat ?: 0.0,
                    orderStatus.currentPosition?.lng ?: 0.0,
                    orderStatus.deliveryLocation?.lat ?: 0.0,
                    orderStatus.deliveryLocation?.lng ?: 0.0
                )

                Spacer(modifier = Modifier.height(8.dp))
                OrderInfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Distanza rimanente",
                    value = "${String.format("%.2f", distance)} km"
                )
            }
        }
    }
}

@Composable
private fun OrderInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color(0xFF666666)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label:",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333)
        )
    }
}