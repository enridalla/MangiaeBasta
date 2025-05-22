package com.example.mangiaebasta.models

import android.net.Uri
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import com.example.mangiaebasta.models.StorageManager

class MenuModel {
    val baseUrl = "https://develop.ewlab.di.unimi.it/mc/2425/menu"
    private val sid = "Qx4f16AFHgPUFe2RG4gXVnVPbkf95zJ8Ih7TIifkKK7a73yn99rJ48kxVDi04qyJ"
    private val defaultLocation = Location(45.4654, 9.1866)
    private val TAG = "MenuModel"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getMenus(): List<MenuItemWithImage> {
        Log.d(TAG, "Iniziando getMenus()")
        val url = Uri.parse(baseUrl)

        val completeUrlString = url.buildUpon()
            .appendQueryParameter("sid", sid)
            .appendQueryParameter("lat", defaultLocation.lat.toString())
            .appendQueryParameter("lng", defaultLocation.lng.toString())
            .build()
            .toString()

        Log.d(TAG, "Fetching menus from: $completeUrlString")

        try {
            val response = client.get(completeUrlString)
            Log.d(TAG, "Risposta ricevuta con stato: ${response.status.value}")

            if (!response.status.isSuccess()) {
                val errText = response.bodyAsText()
                Log.e(TAG, "ERROR ${response.status.value}: $errText")
                throw Exception("Failed to fetch menus: $errText")
            }

            val menuItems: List<MenuItem> = response.body()
            Log.d(TAG, "Received ${menuItems.size} menu items")
            Log.d(TAG, "Menu ricevuti: ${menuItems.map { it.name }}")

            val result = menuItems.map { menuItem ->
                Log.d(TAG, "Elaborazione menu item: ${menuItem.mid} - ${menuItem.name}")
                try {
                    Log.d(TAG, "Caricamento immagine per menu ${menuItem.mid}")
                    val imageBase64 = getMenuImage(menuItem.mid, menuItem.imageVersion)

                    MenuItemWithImage(
                        mid = menuItem.mid,
                        name = menuItem.name,
                        price = menuItem.price,
                        location = menuItem.location,
                        imageVersion = menuItem.imageVersion,
                        shortDescription = menuItem.shortDescription,
                        deliveryTime = menuItem.deliveryTime,
                        image = imageBase64
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to fetch image for menu item ${menuItem.mid}: ${e.message}", e)
                    MenuItemWithImage(
                        mid = menuItem.mid,
                        name = menuItem.name,
                        price = menuItem.price,
                        location = menuItem.location,
                        imageVersion = menuItem.imageVersion,
                        shortDescription = menuItem.shortDescription,
                        deliveryTime = menuItem.deliveryTime,
                        image = null
                    )
                }
            }

            Log.d(TAG, "getMenus() completato con successo, ritorno ${result.size} elementi")
            return result
        } catch (e: Exception) {
            Log.e(TAG, "Errore in getMenus(): ${e.message}", e)
            throw e
        }
    }

    suspend fun getMenuImage(mid: Int, imageVersion: Int): String? {
        // Controlla se l'immagine è già presente nello storage
        StorageManager.getMenuImageBase64(mid, imageVersion)?.let { it: String ->
            if (it.isNotEmpty()) {
                Log.d(TAG, "Immagine per menu $mid trovata nello storage")
                return it
            }
        }

        // Se l'immagine non è presente nello storage, la recuperiamo dal server
        val url = Uri.parse("$baseUrl/$mid/image").buildUpon()
            .appendQueryParameter("sid", sid)
            .appendQueryParameter("mid", mid.toString())
            .build()
            .toString()

        try {
            val response: HttpResponse = client.get(url)
            if (!response.status.isSuccess()) {
                val err = response.bodyAsText()
                Log.e(TAG, "Failed to fetch menu image: $err")
                return null
            }

            // Deserialize as Base64Response object instead of String
            val base64Response: Base64Response = response.body()

            // Save the image to storage
            StorageManager.saveMenuImage(mid, base64Response.base64, imageVersion)

            Log.d(TAG, "Successfully fetched and saved image for menu $mid")
            return base64Response.base64

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching image for menu $mid: ${e.message}", e)
            return null
        }
    }

    suspend fun getMenuDetails(mid: Int): DetailedMenuItemWithImage {
        Log.d(TAG, "Iniziando getMenuDetails() per menu $mid")
        val url = Uri.parse("$baseUrl/$mid")

        val completeUrlString = url.buildUpon()
            .appendQueryParameter("sid", sid)
            .appendQueryParameter("lat", defaultLocation.lat.toString())
            .appendQueryParameter("lng", defaultLocation.lng.toString())
            .build()
            .toString()

        Log.d(TAG, "Fetching menu details from: $completeUrlString")

        try {
            val response = client.get(completeUrlString)
            Log.d(TAG, "Dettagli menu ricevuti per $mid con stato: ${response.status.value}")

            if (!response.status.isSuccess()) {
                val errText = response.bodyAsText()
                Log.e(TAG, "ERROR ${response.status.value}: $errText")
                throw Exception("Failed to fetch menu details: $errText")
            }

            val menuItem: DetailedMenuItem = response.body()
            Log.d(TAG, "Dettagli menu deserializzati: ${menuItem.name}, descrizione: ${menuItem.shortDescription}")

            try {
                Log.d(TAG, "Caricamento immagine per dettaglio menu $mid")
                val imageBase64 = getMenuImage(menuItem.mid, menuItem.imageVersion)

                return DetailedMenuItemWithImage(
                    mid = menuItem.mid,
                    name = menuItem.name,
                    price = menuItem.price,
                    location = menuItem.location,
                    imageVersion = menuItem.imageVersion,
                    shortDescription = menuItem.shortDescription,
                    deliveryTime = menuItem.deliveryTime,
                    longDescription = menuItem.longDescription,
                    image = imageBase64
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch image for menu item ${menuItem.mid}: ${e.message}", e)
                return DetailedMenuItemWithImage(
                    mid = menuItem.mid,
                    name = menuItem.name,
                    price = menuItem.price,
                    location = menuItem.location,
                    imageVersion = menuItem.imageVersion,
                    shortDescription = menuItem.shortDescription,
                    deliveryTime = menuItem.deliveryTime,
                    longDescription = menuItem.longDescription,
                    image = null
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Errore in getMenuDetails() per menu $mid: ${e.message}", e)
            throw e
        }
    }
}