package com.example.mangiaebasta.models

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

// Extension property to create DataStore instance tied to Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object DataStoreManager {
    private const val TAG = "DataStoreManager"
    private val LAST_ROUTE_KEY = stringPreferencesKey("last_route")

    fun getLastRoute(context: Context): Flow<String> =
        context.dataStore.data
            .map { prefs ->
                val route = prefs[LAST_ROUTE_KEY] ?: "menu"
                route
            }
            .onEach { route ->
                Log.d(TAG, "Loaded last route: $route")
            }

    suspend fun saveLastRoute(context: Context, route: String) {
        Log.d(TAG, "Saving last route: $route")
        context.dataStore.edit { prefs ->
            prefs[LAST_ROUTE_KEY] = route
        }
        Log.d(TAG, "Last route saved successfully.")
    }
}
