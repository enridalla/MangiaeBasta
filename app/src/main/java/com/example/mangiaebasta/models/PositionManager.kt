package com.example.mangiaebasta.models

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await

class PositionManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: PositionManager? = null

        @Volatile
        private var appContext: Context? = null

        fun initialize(context: Context) {
            if (appContext == null) {
                appContext = context.applicationContext
            }
        }

        fun getInstance(): PositionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PositionManager().also { INSTANCE = it }
            }
        }
    }

    private val context: Context
        get() = appContext ?: throw IllegalStateException(
            "PositionManager not initialized. Call PositionManager.initialize(context) first."
        )

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val geocoder by lazy {
        Geocoder(context)
    }

    fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun getLocation(): Location? {
        return try {
            if (!checkLocationPermission()) {
                return null
            }

            val task: Task<Location> = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            )

            task.await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}