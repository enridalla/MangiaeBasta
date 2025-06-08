package com.example.mangiaebasta.models

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager private constructor(private val applicationContext: Context) {

    companion object {
        private const val TAG = "DataStoreManager"

        @Volatile
        private var INSTANCE: DataStoreManager? = null

        fun initialize(applicationContext: Context) {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = DataStoreManager(applicationContext.applicationContext)
                    }
                }
            }
        }

        fun getInstance(): DataStoreManager {
            return INSTANCE ?: throw IllegalStateException(
                "DataStoreManager non è stato inizializzato. Chiama DataStoreManager.initialize(context) nell'Application class."
            )
        }
    }

    private val dataStore = applicationContext.dataStore

    private val LAST_ROUTE_KEY = stringPreferencesKey("last_route")
    private val SID_KEY = stringPreferencesKey("sid")
    private val UID_KEY = intPreferencesKey("uid")
    private val LAST_ORDER_KEY = stringPreferencesKey("last_order")
    private val OID_KEY = intPreferencesKey("oid")

    // Profile keys
    private val PROFILE_FIRST_NAME_KEY = stringPreferencesKey("profile_first_name")
    private val PROFILE_LAST_NAME_KEY = stringPreferencesKey("profile_last_name")
    private val PROFILE_CARD_FULL_NAME_KEY = stringPreferencesKey("profile_card_full_name")
    private val PROFILE_CARD_NUMBER_KEY = stringPreferencesKey("profile_card_number")
    private val PROFILE_CARD_EXPIRE_MONTH_KEY = intPreferencesKey("profile_card_expire_month")
    private val PROFILE_CARD_EXPIRE_YEAR_KEY = intPreferencesKey("profile_card_expire_year")
    private val PROFILE_CARD_CVV_KEY = intPreferencesKey("profile_card_cvv")

    // SID
    fun setSid(sid: String?) {
        if (sid == null) return
        CoroutineScope(Dispatchers.Main).launch {
            dataStore.edit { preferences ->
                preferences[SID_KEY] = sid
                val savedSid = preferences[SID_KEY]
                Log.d(TAG, "New saved sid: $savedSid")
            }
        }
    }

    suspend fun getSid(): String? {
        Log.d(TAG, "Getting sid from data store")
        return withContext(Dispatchers.IO) {
            val preferences = dataStore.data.first()
            preferences[SID_KEY]
        }
    }

    // LastOrder
    suspend fun setLastOrder(menuItem: DetailedMenuItemWithImage) {
        val jsonString = Json.encodeToString(DetailedMenuItemWithImage.serializer(), menuItem)
        applicationContext.dataStore.edit { preferences ->
            preferences[LAST_ORDER_KEY] = jsonString
        }
    }

    suspend fun getLastOrder(): DetailedMenuItemWithImage? {
        val preferences = applicationContext.dataStore.data.first()
        val jsonString = preferences[LAST_ORDER_KEY]
        return jsonString?.let { Json.decodeFromString(DetailedMenuItemWithImage.serializer(), it) }
    }

    // OID
    suspend fun setOID(oid: Int) {
        applicationContext.dataStore.edit { preferences ->
            preferences[OID_KEY] = oid
        }
    }

    suspend fun getOID(): Int? {
        val preferences = applicationContext.dataStore.data.first()
        return preferences[OID_KEY]
    }

    // UID
    fun setUid(uid: Int?) {
        if (uid == null) return
        CoroutineScope(Dispatchers.Main).launch {
            dataStore.edit { preferences ->
                preferences[UID_KEY] = uid
                val savedUid = preferences[UID_KEY]
                Log.d(TAG, "New saved uid: $savedUid")
            }
        }
    }

    suspend fun getUid(): Int? {
        Log.d(TAG, "Getting uid from data store")
        return withContext(Dispatchers.IO) {
            val preferences = dataStore.data.first()
            preferences[UID_KEY]
        }
    }

    // Versione sincrona per casi d'uso specifici
    fun getUidBlocking(): Int? = runBlocking {
        getUid()
    }

    // LastRoute
    fun getLastRouteFlow(): Flow<String> =
        dataStore.data
            .map { prefs ->
                val route = prefs[LAST_ROUTE_KEY] ?: "menu"
                route
            }
            .onEach { route ->
                Log.d(TAG, "Loaded last route: $route")
            }

    suspend fun getLastRoute(): String {
        return dataStore.data.first()[LAST_ROUTE_KEY] ?: "menu"
    }

    suspend fun saveLastRoute(route: String) {
        Log.d(TAG, "Saving last route: $route")
        dataStore.edit { prefs ->
            prefs[LAST_ROUTE_KEY] = route
        }
        Log.d(TAG, "Last route saved successfully.")
    }

    // Profile methods
    suspend fun saveProfile(profile: Profile) {
        Log.d(TAG, "Saving profile to DataStore: $profile")
        dataStore.edit { preferences ->
            profile.firstName?.let { preferences[PROFILE_FIRST_NAME_KEY] = it }
            profile.lastName?.let { preferences[PROFILE_LAST_NAME_KEY] = it }
            profile.cardFullName?.let { preferences[PROFILE_CARD_FULL_NAME_KEY] = it }
            profile.cardNumber?.let { preferences[PROFILE_CARD_NUMBER_KEY] = it.toString() }
            profile.cardExpireMonth?.let { preferences[PROFILE_CARD_EXPIRE_MONTH_KEY] = it }
            profile.cardExpireYear?.let { preferences[PROFILE_CARD_EXPIRE_YEAR_KEY] = it }
            profile.cardCVV?.let { preferences[PROFILE_CARD_CVV_KEY] = it }
        }
        Log.d(TAG, "Profile saved to DataStore successfully")
    }

    suspend fun getProfile(): Profile? {
        Log.d(TAG, "Getting profile from DataStore")
        val preferences = dataStore.data.first()

        val firstName = preferences[PROFILE_FIRST_NAME_KEY]
        val lastName = preferences[PROFILE_LAST_NAME_KEY]
        val cardFullName = preferences[PROFILE_CARD_FULL_NAME_KEY]
        val cardNumber = preferences[PROFILE_CARD_NUMBER_KEY]?.toLongOrNull()
        val cardExpireMonth = preferences[PROFILE_CARD_EXPIRE_MONTH_KEY]
        val cardExpireYear = preferences[PROFILE_CARD_EXPIRE_YEAR_KEY]
        val cardCVV = preferences[PROFILE_CARD_CVV_KEY]

        // Se almeno un campo del profilo è presente, restituisce il profilo
        if (firstName != null || lastName != null || cardFullName != null ||
            cardNumber != null || cardExpireMonth != null || cardExpireYear != null || cardCVV != null) {

            val uid = getUid()

            val profile = Profile(
                firstName = firstName,
                lastName = lastName,
                cardFullName = cardFullName,
                cardNumber = cardNumber,
                cardExpireMonth = cardExpireMonth,
                cardExpireYear = cardExpireYear,
                cardCVV = cardCVV,
                uid = uid!!
            )

            Log.d(TAG, "Profile retrieved from DataStore: $profile")
            return profile
        } else {
            Log.d(TAG, "No profile found in DataStore")
            return null
        }
    }

    suspend fun clearProfile() {
        Log.d(TAG, "Clearing profile from DataStore")
        dataStore.edit { preferences ->
            preferences.remove(PROFILE_FIRST_NAME_KEY)
            preferences.remove(PROFILE_LAST_NAME_KEY)
            preferences.remove(PROFILE_CARD_FULL_NAME_KEY)
            preferences.remove(PROFILE_CARD_NUMBER_KEY)
            preferences.remove(PROFILE_CARD_EXPIRE_MONTH_KEY)
            preferences.remove(PROFILE_CARD_EXPIRE_YEAR_KEY)
            preferences.remove(PROFILE_CARD_CVV_KEY)
        }
        Log.d(TAG, "Profile cleared from DataStore successfully")
    }
}