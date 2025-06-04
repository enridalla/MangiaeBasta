package com.example.mangiaebasta.models

import android.util.Log
import android.net.Uri
import androidx.datastore.dataStore
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import io.ktor.client.request.put
import io.ktor.client.statement.*
import com.example.mangiaebasta.models.StorageManager

class ProfileModel {
    private val TAG = "ProfileModel"
    private val dataStoreManager = DataStoreManager.getInstance()

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getProfileInfo(): Profile {
        // Prima controlla se c'è un profilo salvato nel DataStore
        Log.d(TAG, "getProfileInfo() → Checking DataStore for cached profile")
        val cachedProfile = dataStoreManager.getProfile()

        if (cachedProfile != null) {
            Log.d(TAG, "getProfileInfo() → Found cached profile in DataStore, returning it: $cachedProfile")
            return cachedProfile
        }

        Log.d(TAG, "getProfileInfo() → No cached profile found, fetching from server")

        val uid = getUid() ?: throw Exception("UID non disponibile")
        val sid = getSid() ?: throw Exception("SID non disponibile")
        val baseUrl = "https://develop.ewlab.di.unimi.it/mc/2425/user/$uid"
        val url = Uri.parse(baseUrl)

        val completeUrlString = url.buildUpon()
            .appendQueryParameter("sid", sid)
            .appendQueryParameter("uid", uid.toString())
            .build()
            .toString()

        Log.d(TAG, "getProfileInfo() → requesting URL: $completeUrlString")

        val response = client.get(completeUrlString)
        if (!response.status.isSuccess()) {
            val errText = response.bodyAsText()
            Log.e(TAG, "getProfileInfo() → HTTP ${response.status.value}: $errText")
            throw Exception("Errore ${response.status.value}: $errText")
        }

        val responseBody = response.bodyAsText()
        Log.d(TAG, "getProfileInfo() → HTTP ${response.status.value}")
        Log.d(TAG, "getProfileInfo() → raw response body: $responseBody")

        val profile: Profile = response.body()
        Log.d(TAG, "getProfileInfo() → parsed Profile from server: $profile")

        // Salva il profilo nel DataStore per uso futuro
        Log.d(TAG, "getProfileInfo() → Saving profile to DataStore for caching")
        dataStoreManager.saveProfile(profile)

        return profile
    }

    suspend fun createUser(): User {
        val baseUrl = "https://develop.ewlab.di.unimi.it/mc/2425/user"
        val completeUrlString = Uri.parse(baseUrl)
            .buildUpon()
            .build()
            .toString()
        Log.d(TAG, "createUser() → Request URL: $completeUrlString")

        val response: HttpResponse = client.post(completeUrlString) {
            contentType(ContentType.Application.Json)
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            Log.e(TAG, "createUser() → Errore HTTP ${response.status}: $errorBody")
            throw Exception("Errore nella creazione utente: ${response.status} - $errorBody")
        }

        val body: User = response.body()

        // Salva SID e UID nel DataStore
        dataStoreManager.setSid(body.sid)
        dataStoreManager.setUid(body.uid)
        Log.d(TAG, "createUser() → SID salvato: ${body.sid}")
        Log.d(TAG, "createUser() → UID salvato: ${body.uid}")

        return body
    }

    suspend fun getSid(): String? {
        val sid = dataStoreManager.getSid()

        if (!sid.isNullOrBlank() && sid.length >= 64) {
            Log.d(TAG, "getSid() → SID valido trovato nel DataStore (length: ${sid.length})")
            return sid
        }

        Log.d(TAG, "getSid() → SID non valido nel DataStore (${sid?.length ?: 0} caratteri), creando un nuovo utente")

        try {
            val user = createUser()
            val newSid = user.sid

            if (newSid.isNullOrBlank() || newSid.length < 64) {
                Log.e(TAG, "getSid() → SID ricevuto dal server non valido: $newSid")
                throw Exception("Server ha restituito un SID non valido")
            }

            Log.d(TAG, "getSid() → Nuovo SID creato con successo (length: ${newSid.length})")
            return newSid
        } catch (e: Exception) {
            Log.e(TAG, "getSid() → Errore nella creazione del nuovo utente: ${e.message}", e)
            throw e
        }
    }

    suspend fun getUid(): Int? {
        val uid = dataStoreManager.getUid()

        if (uid != null && uid > 0) {
            Log.d(TAG, "getUid() → UID trovato nel DataStore: $uid")
            return uid
        }

        Log.d(TAG, "getUid() → UID non trovato nel DataStore, creando un nuovo utente")

        try {
            val user = createUser()
            val newUid = user.uid

            if (newUid <= 0) {
                Log.e(TAG, "getUid() → UID ricevuto dal server non valido: $newUid")
                throw Exception("Server ha restituito un UID non valido")
            }

            Log.d(TAG, "getUid() → Nuovo UID creato con successo: $newUid")
            return newUid
        } catch (e: Exception) {
            Log.e(TAG, "getUid() → Errore nella creazione del nuovo utente: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateUser(profile: Profile) {
        val uid = getUid() ?: throw Exception("UID non disponibile per l'aggiornamento")
        val sid = getSid() ?: throw Exception("SID non disponibile per l'aggiornamento")

        val baseUrl = "https://develop.ewlab.di.unimi.it/mc/2425/user/$uid"
        val url = Uri.parse(baseUrl)

        val completeUrlString = url.buildUpon()
            .appendQueryParameter("sid", sid)
            .build()
            .toString()

        Log.d(TAG, "updateUser() → PUT to URL: $completeUrlString")

        val profileToUpdate = ProfileToUpdate(
            firstName = profile.firstName ?: "DefaultFirstName",
            lastName = profile.lastName ?: "DefaultLastName",
            cardFullName = profile.cardFullName ?: "DefaultCardFullName",
            cardNumber = profile.cardNumber?.toString() ?: "0",
            cardExpireMonth = profile.cardExpireMonth?.toString() ?: "1",
            cardExpireYear = profile.cardExpireYear?.toString() ?: "2000",
            cardCVV = profile.cardCVV?.toString() ?: "0",
            sid = sid
        )

        Log.d(TAG, "updateUser() → payload: $profileToUpdate")

        val response = client.put(completeUrlString) {
            contentType(ContentType.Application.Json)
            setBody(profileToUpdate)
        }

        if (!response.status.isSuccess()) {
            val errText = response.bodyAsText()
            Log.e(TAG, "updateUser() → HTTP ${response.status.value}: $errText")
            throw Exception("Errore ${response.status.value}: $errText")
        }

        val responseBody = response.bodyAsText()
        Log.d(TAG, "updateUser() → HTTP ${response.status.value}")
        Log.d(TAG, "updateUser() → raw response body: $responseBody")
        Log.d(TAG, "updateUser() → update successful")

        // Aggiorna il profilo nel DataStore dopo l'aggiornamento riuscito
        Log.d(TAG, "updateUser() → Updating cached profile in DataStore")
        dataStoreManager.saveProfile(profile)
        Log.d(TAG, "updateUser() → Profile cache updated successfully")
    }

    /**
     * Forza il refresh del profilo dal server, bypassa la cache
     */
    suspend fun refreshProfileFromServer(): Profile {
        Log.d(TAG, "refreshProfileFromServer() → Clearing cached profile and fetching fresh data")

        // Pulisci la cache del profilo
        dataStoreManager.clearProfile()

        val uid = getUid() ?: throw Exception("UID non disponibile")
        val sid = getSid() ?: throw Exception("SID non disponibile")
        val baseUrl = "https://develop.ewlab.di.unimi.it/mc/2425/user/$uid"
        val url = Uri.parse(baseUrl)

        val completeUrlString = url.buildUpon()
            .appendQueryParameter("sid", sid)
            .appendQueryParameter("uid", uid.toString())
            .build()
            .toString()

        Log.d(TAG, "refreshProfileFromServer() → requesting URL: $completeUrlString")

        val response = client.get(completeUrlString)
        if (!response.status.isSuccess()) {
            val errText = response.bodyAsText()
            Log.e(TAG, "refreshProfileFromServer() → HTTP ${response.status.value}: $errText")
            throw Exception("Errore ${response.status.value}: $errText")
        }

        val responseBody = response.bodyAsText()
        Log.d(TAG, "refreshProfileFromServer() → HTTP ${response.status.value}")
        Log.d(TAG, "refreshProfileFromServer() → raw response body: $responseBody")

        val profile: Profile = response.body()
        Log.d(TAG, "refreshProfileFromServer() → parsed fresh Profile from server: $profile")

        // Salva il nuovo profilo nel DataStore
        Log.d(TAG, "refreshProfileFromServer() → Saving fresh profile to DataStore")
        dataStoreManager.saveProfile(profile)

        return profile
    }

    /**
     * Ottiene il profilo dalla cache locale senza fare richieste al server
     */
    suspend fun getCachedProfile(): Profile? {
        Log.d(TAG, "getCachedProfile() → Getting profile from DataStore cache only")
        return dataStoreManager.getProfile()
    }
}