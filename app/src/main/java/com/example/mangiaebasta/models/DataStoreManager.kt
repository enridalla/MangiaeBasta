package com.example.mangiaebasta.models

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property to create DataStore instance tied to Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Manager object for persisting and retrieving the last navigation route.
 */
object DataStoreManager {
    private val LAST_ROUTE_KEY = stringPreferencesKey("last_route")

    /**
     * Returns a Flow emitting the last saved route, falling back to "menu" if none is present.
     */
    fun getLastRoute(context: Context): Flow<String> =
        context.dataStore.data
            .map { prefs -> prefs[LAST_ROUTE_KEY] ?: "menu" }

    /**
     * Saves the provided route string to DataStore.
     */
    suspend fun saveLastRoute(context: Context, route: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_ROUTE_KEY] = route
        }
    }
}