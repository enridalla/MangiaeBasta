package com.example.mangiaebasta.viewmodels

import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangiaebasta.models.DetailedMenuItemWithImage
import com.example.mangiaebasta.models.Order
import com.example.mangiaebasta.models.OrderModel
import com.example.mangiaebasta.models.PositionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException

class OrderViewModel : ViewModel() {

    companion object {
        private const val TAG = "OrderViewModel"
        private const val REFRESH_INTERVAL_MS = 5000L // 5 secondi
    }

    private val orderModel = OrderModel()
    private val positionManager = PositionManager.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _orderStatus = MutableStateFlow<Order?>(null)
    val orderStatus: StateFlow<Order?> = _orderStatus.asStateFlow()

    private val _lastOrder = MutableStateFlow<DetailedMenuItemWithImage?>(null)
    val lastOrder: StateFlow<DetailedMenuItemWithImage?> = _lastOrder.asStateFlow()

    private val _userLocation = MutableStateFlow<android.location.Location?>(null)
    val userLocation: StateFlow<android.location.Location?> = _userLocation.asStateFlow()

    private val _isRefreshingAutomatically = MutableStateFlow(false)
    val isRefreshingAutomatically: StateFlow<Boolean> = _isRefreshingAutomatically.asStateFlow()

    internal fun loadUserLocation() {
        viewModelScope.launch {
            try {
                val location = positionManager.getLocation()
                _userLocation.value = location
                if (location != null) {
                    Log.d(TAG, "Posizione utente caricata: lat=${location.latitude}, lng=${location.longitude}")
                } else {
                    Log.w(TAG, "Impossibile ottenere la posizione utente")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Errore nel caricamento della posizione utente", e)
            }
        }
    }

    internal fun loadOrderData() {
        viewModelScope.launch {
            _isLoading.value = true

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

                // Avvia il refresh automatico se l'ordine è attivo
                if (orderStatusData?.status == "ON_DELIVERY" && !_isRefreshingAutomatically.value) {
                    startAutomaticRefresh()
                }

                // Se non c'è né ultimo ordine né stato ordine, non è un errore
                // La UI gestirà il caso con EmptyOrderScreen
                if (lastOrderData == null && orderStatusData == null) {
                    Log.d(TAG, "Nessun ordine trovato - stato normale per utenti senza ordini")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Errore nel caricamento dei dati dell'ordine", e)
            } finally {
                _isLoading.value = false
                Log.d(TAG, "Caricamento dati ordine completato")
            }
        }
    }

    private fun startAutomaticRefresh() {
        if (_isRefreshingAutomatically.value) {
            Log.d(TAG, "Refresh automatico già attivo")
            return
        }

        Log.d(TAG, "Avvio refresh automatico ogni ${REFRESH_INTERVAL_MS}ms")
        _isRefreshingAutomatically.value = true

        viewModelScope.launch {
            while (_isRefreshingAutomatically.value) {
                delay(REFRESH_INTERVAL_MS)

                try {
                    Log.d(TAG, "Refresh automatico - controllo stato ordine")
                    val currentOrderStatus = orderModel.getOrderStatus()
                    _orderStatus.value = currentOrderStatus

                    // Aggiorna anche la posizione utente
                    val location = positionManager.getLocation()
                    _userLocation.value = location

                    // Se l'ordine non è più in consegna, ferma il refresh
                    if (currentOrderStatus?.status != "ON_DELIVERY") {
                        Log.d(TAG, "Ordine non più in consegna, fermo refresh automatico")
                        stopAutomaticRefresh()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Errore durante refresh automatico", e)
                }
            }
        }
    }

    private fun stopAutomaticRefresh() {
        Log.d(TAG, "Fermo refresh automatico")
        _isRefreshingAutomatically.value = false
    }

    fun refreshOrderData() {
        Log.d(TAG, "Refresh dati ordine richiesto manualmente")
        loadOrderData()
    }

    fun getEstimatedTime(): String {
        val isoTimestamp = _orderStatus.value?.expectedDeliveryTimestamp ?: return "N/A"

        return try {
            val deliveryInstant = Instant.parse(isoTimestamp)
            val nowInstant = Instant.now()

            val minutes = Duration.between(nowInstant, deliveryInstant).toMinutes()
            if (minutes <= 0) "0 min" else "$minutes min"
        } catch (e: DateTimeParseException) {
            Log.e(TAG, "Errore nel parsing del timestamp", e)
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

    fun getOrderStatusMessage(): String {
        val order = _orderStatus.value ?: return "Stato sconosciuto"

        return when (order.status) {
            "ON_DELIVERY" -> "Il tuo ordine è in viaggio! Il drone sta arrivando alla tua posizione."
            "COMPLETED" -> "Ordine consegnato con successo! Speriamo ti sia piaciuto il tuo pasto."
            else -> "Stato ordine: ${order.status}"
        }
    }

    fun getOrderStatusColor(): androidx.compose.ui.graphics.Color {
        val order = _orderStatus.value ?: return androidx.compose.ui.graphics.Color.Gray

        return when (order.status) {
            "ON_DELIVERY" -> androidx.compose.ui.graphics.Color(0xFF2196F3) // Blu
            "COMPLETED" -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde
            else -> androidx.compose.ui.graphics.Color.Gray
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAutomaticRefresh()
    }
}