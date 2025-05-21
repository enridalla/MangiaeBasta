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
    private val LASTORDER_KEY = stringPreferencesKey("last_order")
    private val UID_KEY = intPreferencesKey("uid")

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

    // Versione sincrona per casi d'uso specifici
    fun getSidBlocking(): String? = runBlocking {
        getSid()
    }

    // LastOrder
    suspend fun saveLastOrder(lastOrder: String) {
        dataStore.edit { preferences ->
            preferences[LASTORDER_KEY] = lastOrder
            val savedLastOrder = preferences[LASTORDER_KEY]
            Log.d(TAG, "New saved LastOrder: $savedLastOrder")
        }
    }

    suspend fun getLastOrder(): String? {
        return withContext(Dispatchers.IO) {
            val preferences = dataStore.data.first()
            preferences[LASTORDER_KEY]
        }
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
}