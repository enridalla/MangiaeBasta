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
import kotlinx.serialization.Serializable

class OrderModel {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    companion object {
        private const val TAG = "OrderModel"
    }

    suspend fun order(mid: Int): Order? {
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

        return try {
            val response = client.post(completeUrlString) {
                contentType(ContentType.Application.Json)
                setBody(delivery)
            }
            if (!response.status.isSuccess()) {
                Log.e(TAG, "order() → ERRORE ${response.status.value}: ${response.bodyAsText()}")
                null
            } else {
                Json.decodeFromString(Order.serializer(), response.bodyAsText())
            }
        } catch (e: Exception) {
            Log.e(TAG, "order() → exception during POST", e)
            null
        }
    }

    suspend fun getOrderStatus(): Order? {
        val oid = 12345
        val sid = "zJQhOtMu8IDfH7WymQTdtS5C2yHyUMw0gAlplK8v0DEn8xgrqtkIk3r1p0Cb9Zdg"
        val baseUrl = "https://develop.ewlab.di.unimi.it/mc/2425/order/$oid"

        val urlBuilder = Uri.parse(baseUrl).buildUpon()
            .appendQueryParameter("oid", oid.toString())
            .appendQueryParameter("sid", sid)
        val completeUrlString = urlBuilder.build().toString()

        Log.d(TAG, "getOrderStatus() → endpoint: $completeUrlString")

        return try {
            val response = client.get(completeUrlString) {
                contentType(ContentType.Application.Json)
            }
            if (!response.status.isSuccess()) {
                Log.e(TAG, "getOrderStatus() → ERRORE ${response.status.value}: ${response.bodyAsText()}")
                null
            } else {
                Json.decodeFromString(Order.serializer(), response.bodyAsText())
            }
        } catch (e: Exception) {
            Log.e(TAG, "getOrderStatus() → exception during GET", e)
            null
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