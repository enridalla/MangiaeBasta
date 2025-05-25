package com.example.mangiaebasta.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangiaebasta.models.DetailedMenuItemWithImage
import com.example.mangiaebasta.models.Order
import com.example.mangiaebasta.models.OrderModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class OrderViewModel : ViewModel() {

    companion object {
        private const val TAG = "OrderViewModel"
    }

    private val orderModel = OrderModel()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _orderStatus = MutableStateFlow<Order?>(null)
    val orderStatus: StateFlow<Order?> = _orderStatus.asStateFlow()

    private val _lastOrder = MutableStateFlow<DetailedMenuItemWithImage?>(null)
    val lastOrder: StateFlow<DetailedMenuItemWithImage?> = _lastOrder.asStateFlow()

    private val _userLocation = MutableStateFlow<android.location.Location?>(null)
    val userLocation: StateFlow<android.location.Location?> = _userLocation.asStateFlow()

    init {
        loadOrderData()
        loadUserLocation()
    }

    private fun loadOrderData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                Log.d(TAG, "Inizio caricamento dati ordine...")

                // Carica l'ultimo ordine
                val lastOrderData = orderModel.loadLastOrder()
                Log.d(TAG, "Ultimo ordine caricato: ${lastOrderData?.name ?: "null"}")
                _lastOrder.value = lastOrderData

                // Carica sempre lo stato dell'ordine se c'è un OID salvato
                val orderStatusData = orderModel.getOrderStatus()
                Log.d(TAG, "Stato ordine caricato: ${orderStatusData?.status ?: "null"}")
                _orderStatus.value = orderStatusData

                // Se non c'è né ultimo ordine né stato ordine, non è un errore
                // La UI gestirà il caso con EmptyOrderScreen
                if (lastOrderData == null && orderStatusData == null) {
                    Log.d(TAG, "Nessun ordine trovato - stato normale per utenti senza ordini")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Errore nel caricamento dei dati dell'ordine", e)
                _error.value = "Errore nel caricamento dell'ordine: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d(TAG, "Caricamento dati ordine completato")
            }
        }
    }

    private fun loadUserLocation() {
        viewModelScope.launch {
            try {
                // For now, we'll skip location loading since it requires Context
                // If you need user location, you can implement it differently
                // or pass it from the UI layer when needed
                Log.d(TAG, "User location loading skipped")
            } catch (e: Exception) {
                Log.e(TAG, "Errore nel caricamento della posizione utente", e)
            }
        }
    }

    fun refreshOrderData() {
        Log.d(TAG, "Refresh dati ordine richiesto")
        loadOrderData()
    }

    fun getEstimatedTime(): String {
        val order = _orderStatus.value ?: return "N/A"

        return try {
            val deliveryTime = order.expectedDeliveryTimestamp
            if (deliveryTime != null) {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val deliveryDate = format.parse(deliveryTime)
                val currentTime = Date()

                if (deliveryDate != null && deliveryDate.after(currentTime)) {
                    val diffInMillis = deliveryDate.time - currentTime.time
                    val diffInMinutes = diffInMillis / (1000 * 60)
                    "${diffInMinutes.toInt()} min"
                } else {
                    "In arrivo"
                }
            } else {
                "Tempo non disponibile"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Errore nel calcolo del tempo stimato", e)
            "N/A"
        }
    }

    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371.0 // Raggio della Terra in km

        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    fun getPathCoordinates(): List<Pair<Double, Double>> {
        val order = _orderStatus.value

        return if (order?.status == "ON_DELIVERY") {
            listOf(
                Pair(order.currentPosition.lat, order.currentPosition.lng),
                Pair(order.deliveryLocation.lat, order.deliveryLocation.lng)
            )
        } else {
            emptyList()
        }
    }

    fun getCenterCoordinates(): Pair<Double, Double>? {
        val order = _orderStatus.value ?: return null
        val lastOrderData = _lastOrder.value ?: return null

        return when (order.status) {
            "ON_DELIVERY" -> {
                // Calcola il centro tra le tre posizioni: ristorante, drone e destinazione
                val restaurantLat = lastOrderData.location.lat
                val restaurantLng = lastOrderData.location.lng
                val droneLat = order.currentPosition.lat
                val droneLng = order.currentPosition.lng
                val deliveryLat = order.deliveryLocation.lat
                val deliveryLng = order.deliveryLocation.lng

                val centerLat = (restaurantLat + droneLat + deliveryLat) / 3
                val centerLng = (restaurantLng + droneLng + deliveryLng) / 3

                Pair(centerLat, centerLng)
            }
            else -> {
                // Per ordini consegnati, centra sulla destinazione
                Pair(order.deliveryLocation.lat, order.deliveryLocation.lng)
            }
        }
    }

    fun getMapZoomLevel(): Double {
        val order = _orderStatus.value ?: return 12.0
        val lastOrderData = _lastOrder.value ?: return 12.0

        return when (order.status) {
            "ON_DELIVERY" -> {
                // Calcola la distanza massima tra i punti per determinare il zoom
                val restaurantLat = lastOrderData.location.lat
                val restaurantLng = lastOrderData.location.lng
                val droneLat = order.currentPosition.lat
                val droneLng = order.currentPosition.lng
                val deliveryLat = order.deliveryLocation.lat
                val deliveryLng = order.deliveryLocation.lng

                val dist1 = calculateDistance(restaurantLat, restaurantLng, droneLat, droneLng)
                val dist2 = calculateDistance(droneLat, droneLng, deliveryLat, deliveryLng)
                val dist3 = calculateDistance(restaurantLat, restaurantLng, deliveryLat, deliveryLng)

                val maxDistance = maxOf(dist1, dist2, dist3)

                // Converte la distanza in livello di zoom appropriato
                when {
                    maxDistance > 50 -> 8.0
                    maxDistance > 20 -> 10.0
                    maxDistance > 5 -> 12.0
                    maxDistance > 1 -> 14.0
                    else -> 16.0
                }
            }
            else -> 14.0
        }
    }

    fun formatDeliveryTime(timestamp: String?): String {
        return try {
            if (timestamp != null) {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = format.parse(timestamp)
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                timeFormat.format(date ?: Date())
            } else {
                "N/A"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Errore nel formato dell'ora di consegna", e)
            "N/A"
        }
    }
}