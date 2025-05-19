package com.example.mangiaebasta.models

import android.util.Log
import android.net.Uri
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

class ProfileModel {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getProfileInfo(): Profile {
        val uid = 43352
        val sid = "zJQhOtMu8IDfH7WymQTdtS5C2yHyUMw0gAlplK8v0DEn8xgrqtkIk3r1p0Cb9Zdg"
        val baseUrl = "https://develop.ewlab.di.unimi.it/mc/2425/user/$uid"
        val url = Uri.parse(baseUrl)

        val completeUrlString = url.buildUpon()
            .appendQueryParameter("sid", sid)
            .appendQueryParameter("uid", uid.toString())
            .build()
            .toString()

        Log.d("CommController", "getProfileInfo() → requesting URL: $completeUrlString")

        val response = client.get(completeUrlString)
        if (!response.status.isSuccess()) {
            val errText = response.bodyAsText()
            throw Exception("Errore ${response.status.value}: $errText")
        }

        val responseBody = response.bodyAsText()
        Log.d("CommController", "getProfileInfo() → HTTP ${response.status.value}")
        Log.d("CommController", "getProfileInfo() → raw response body: $responseBody")

        val profile: Profile = response.body()
        Log.d("CommController", "getProfileInfo() → parsed Profile: $profile")
        return profile
    }

    suspend fun createUser() {
        val baseUrl = "https://develop.ewlab.di.unimi.it/mc/2425/user"
        val completeUrlString = Uri.parse(baseUrl)
            .buildUpon()
            .build()
            .toString()
        Log.d("CommunicationController", "Request URL: $completeUrlString")

        val response: HttpResponse = client.post(completeUrlString) {
            contentType(ContentType.Application.Json)
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            Log.e("CommunicationController", "Errore HTTP ${response.status}: $errorBody")
        } else {
            val body: User = response.body()
            Log.d("CommunicationController", body.toString())
        }
    }

    suspend fun updateUser(profile: Profile) {
        val uid = 43352
        val baseUrl = "https://develop.ewlab.di.unimi.it/mc/2425/user/$uid"
        val sid = "zJQhOtMu8IDfH7WymQTdtS5C2yHyUMw0gAlplK8v0DEn8xgrqtkIk3r1p0Cb9Zdg"
        val url = Uri.parse(baseUrl)

        val completeUrlString = url.buildUpon()
            .appendQueryParameter("sid", sid)
            .build()
            .toString()

        Log.d("CommController", "updateUser() → PUT to URL: $completeUrlString")

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

        Log.d("CommController", "updateUser() → payload: $profileToUpdate")

        val response = client.put(completeUrlString) {
            contentType(ContentType.Application.Json)
            setBody(profileToUpdate)
        }
        if (!response.status.isSuccess()) {
            val errText = response.bodyAsText()
            Log.e("CommController", "updateUser() → HTTP ${response.status.value}: $errText")
            throw Exception("Errore ${response.status.value}: $errText")
        }

        val responseBody = response.bodyAsText()
        Log.d("CommController", "updateUser() → HTTP ${response.status.value}")
        Log.d("CommController", "updateUser() → raw response body: $responseBody")
        Log.d("CommController", "updateUser() → update successful")
    }
}

@Serializable
data class Profile(
    val firstName: String?,
    val lastName: String?,
    val cardFullName: String?,
    val cardNumber: Long?,
    val cardExpireMonth: Int?,
    val cardExpireYear: Int?,
    val cardCVV: Int?,
    val uid: Int,
)

@Serializable
data class User(
    val uid: Int,
    val sid: String
)

@Serializable
data class ProfileToUpdate(
    val firstName: String,
    val lastName: String,
    val cardFullName: String,
    val cardNumber: String,
    val cardExpireMonth: String,
    val cardExpireYear: String,
    val cardCVV: String,
    val sid: String
)