package com.example.mangiaebasta.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import android.Manifest

class AppViewModel : ViewModel() {

    private val _appState = MutableStateFlow(AppState.CHECKING_PERMISSIONS)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    companion object {
        private const val TAG = "AppViewModel"
    }

    fun checkLocationPermission(context: Context) {
        viewModelScope.launch {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            _hasLocationPermission.value = hasPermission

            if (hasPermission) {
                Log.d(TAG, "Location permission already granted")
                _appState.value = AppState.PERMISSION_GRANTED
            } else {
                Log.d(TAG, "Location permission not granted, showing explanation")
                _appState.value = AppState.SHOWING_PERMISSION_EXPLANATION
            }
        }
    }

    fun onPermissionExplanationAccepted() {
        Log.d(TAG, "User accepted permission explanation, requesting permission")
        _appState.value = AppState.REQUESTING_PERMISSION
    }

    fun onPermissionResult(isGranted: Boolean) {
        viewModelScope.launch {
            _hasLocationPermission.value = isGranted

            if (isGranted) {
                Log.d(TAG, "Permission granted by user")
                _appState.value = AppState.PERMISSION_GRANTED
            } else {
                Log.d(TAG, "Permission denied by user")
                _appState.value = AppState.PERMISSION_DENIED
            }
        }
    }

    fun retryPermissionRequest() {
        Log.d(TAG, "User wants to retry permission request")
        _appState.value = AppState.REQUESTING_PERMISSION
    }
}

enum class AppState {
    CHECKING_PERMISSIONS,
    SHOWING_PERMISSION_EXPLANATION,
    REQUESTING_PERMISSION,
    PERMISSION_GRANTED,
    PERMISSION_DENIED
}