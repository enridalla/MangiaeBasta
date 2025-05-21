package com.example.mangiaebasta.models

import android.util.Log
import android.net.Uri
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.json.JSONObject

class OrderModel {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // Utilizziamo il singleton DataStoreManager
    private val dataStoreManager = DataStoreManager.getInstance()

    companion object {
        private const val TAG = "OrderModel"
    }

    /**
     * Effettua un ordine e gestisce specifici messaggi di errore dall'API
     * Salva l'OID e l'UID nel DataStore
     */
    suspend fun order(mid: Int): Order {
        val sid = "zJQhOtMu8IDfH7WymQTdtS5C2yHyUMw0gAlplK8v0DEn8xgrqtkIk3r1p0Cb9Zdg"
        val location = Location(45.4654, 9.1866)
        val baseUrl = "https://develop.ewlab.di.unimi.it/mc/2425/menu/$mid/buy"
        val urlBuilder = Uri.parse(baseUrl).buildUpon()
            .appendQueryParameter("mid", mid.toString())
        val completeUrlString = urlBuilder.build().toString()

        Log.d(TAG, "order() → endpoint: $completeUrlString")

        val delivery = DeliveryLocationWithSid(
            sid = sid,
            deliveryLocation = location
        )
        Log.d(TAG, "order() → payload: $delivery")

        try {
            val response = client.post(completeUrlString) {
                contentType(ContentType.Application.Json)
                setBody(delivery)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                Log.e(TAG, "order() → ERRORE ${response.status.value}: $errorBody")

                // Estrai ed elabora il messaggio di errore
                val errorMessage = parseErrorMessage(errorBody)
                throw Exception(errorMessage)
            }
            val order: Order = Json.decodeFromString(Order.serializer(), response.bodyAsText())


            dataStoreManager.saveLastOrder(order.oid.toString())
            Log.d(TAG, "order() → saved OID ${order.oid} to DataStore")

            return order
        } catch (e: Exception) {
            // Se l'eccezione è già stata gestita con parseErrorMessage, la ripropago
            if (e.message?.startsWith("API Error:") == true) {
                throw e
            }

            Log.e(TAG, "order() → exception during POST", e)
            throw Exception("Errore durante l'invio dell'ordine: ${e.message}")
        }
    }

    /**
     * Analizza il messaggio di errore JSON e lo trasforma in un messaggio user-friendly
     */
    private fun parseErrorMessage(errorBody: String): String {
        return try {
            // Tenta di analizzare il corpo dell'errore come JSON
            val jsonObject = JSONObject(errorBody)
            val message = jsonObject.optString("message", errorBody)

            // Traduci i messaggi di errore comuni in messaggi user-friendly
            val userFriendlyMessage = when {
                message.contains("User already has an active order") ->
                    "Hai già un ordine attivo. Completa il tuo ordine attuale prima di ordinarne uno nuovo."
                message.contains("Invalid card") ->
                    "Prima di ordinare devi completare il tuo profilo."
                else -> "Errore: $message"
            }

            "API Error: $userFriendlyMessage"
        } catch (e: Exception) {
            // In caso non sia un JSON valido, restituisci il messaggio grezzo
            "API Error: $errorBody"
        }
    }

    suspend fun getOrderStatus(): Order? {
        val sid = "zJQhOtMu8IDfH7WymQTdtS5C2yHyUMw0gAlplK8v0DEn8xgrqtkIk3r1p0Cb9Zdg"
        var oid: Int? = null

        // Recupera l'OID dal DataStore
        val lastOrderStr = dataStoreManager.getLastOrder()
        oid = lastOrderStr?.toIntOrNull()
        Log.d(TAG, "getOrderStatus() → retrieved OID $oid from DataStore")

        // Se non è disponibile un OID nel DataStore, usa un valore di default
        if (oid == null) {
            oid = 12345
            Log.d(TAG, "getOrderStatus() → using default OID $oid")
        }

        // Recupera e logga anche l'UID dal DataStore
        val storedUid = dataStoreManager.getUid()
        Log.d(TAG, "getOrderStatus() → retrieved UID $storedUid from DataStore")

        val baseUrl = "https://develop.ewlab.di.unimi.it/mc/2425/order/$oid"

        val urlBuilder = Uri.parse(baseUrl).buildUpon()
            .appendQueryParameter("oid", oid.toString())
            .appendQueryParameter("sid", sid)
        val completeUrlString = urlBuilder.build().toString()

        Log.d(TAG, "getOrderStatus() → endpoint: $completeUrlString")
        try {
            val response = client.get(completeUrlString) {
                contentType(ContentType.Application.Json)
            }
            if (!response.status.isSuccess()) {
                Log.e(TAG, "getOrderStatus() → ERRORE ${response.status.value}: ${response.bodyAsText()}")
                return null
            } else {
                val order: Order = Json.decodeFromString(Order.serializer(), response.bodyAsText())
                return order
            }
        } catch (e: Exception) {
            Log.e(TAG, "getOrderStatus() → exception during GET", e)
            return null
        }
    }
}

@Serializable
data class Location(
    val lat: Double,
    val lng: Double
)

@Serializable
data class Order(
    val oid: Int,
    val mid: Int,
    val uid: Int,
    val creationTimestamp: String,
    val status: String,
    val deliveryLocation: Location,
    val expectedDeliveryTimestamp: String?,
    val currentPosition: Location,
)

@Serializable
data class DeliveryLocationWithSid(
    val sid: String,
    val deliveryLocation: Location
)