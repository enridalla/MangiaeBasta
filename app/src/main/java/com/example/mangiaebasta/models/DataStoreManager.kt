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

/**
 * Manager object for persisting and retrieving the last navigation route.
 */
object DataStoreManager {
    private const val TAG = "DataStoreManager"
    private val LAST_ROUTE_KEY = stringPreferencesKey("last_route")

    /**
     * Returns a Flow emitting the last saved route, falling back to "menu" if none is present.
     * Logs each emitted value for debugging.
     */
    fun getLastRoute(context: Context): Flow<String> =
        context.dataStore.data
            .map { prefs ->
                val route = prefs[LAST_ROUTE_KEY] ?: "menu"
                route
            }
            .onEach { route ->
                Log.d(TAG, "Loaded last route: $route")
            }

    /**
     * Saves the provided route string to DataStore and logs the action.
     */
    suspend fun saveLastRoute(context: Context, route: String) {
        Log.d(TAG, "Saving last route: $route")
        context.dataStore.edit { prefs ->
            prefs[LAST_ROUTE_KEY] = route
        }
        Log.d(TAG, "Last route saved successfully.")
    }
}
