package com.example.mangiaebasta.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangiaebasta.models.Profile
import com.example.mangiaebasta.models.ProfileModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.mangiaebasta.models.OrderModel

class ProfileViewModel : ViewModel() {
    private val profileModel = ProfileModel()
    private val orderModel = OrderModel()

    // Utilizziamo direttamente i tipi del modello senza mapping
    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile

    private val _orderInfo = MutableStateFlow<OrderInfoUiState?>(null)
    val orderInfo: StateFlow<OrderInfoUiState?> = _orderInfo

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Stato per indicare se il profilo è completo o meno
    private val _isProfileComplete = MutableStateFlow(false)
    val isProfileComplete: StateFlow<Boolean> = _isProfileComplete

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profile = profileModel.getProfileInfo()
                _profile.value = profile

                // Verifica se il profilo è completo
                checkProfileCompleteness(profile)
            } catch (e: Exception) {
                _profile.value = null
                _isProfileComplete.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadOrderInfo() {
        viewModelScope.launch {
            try {
                //val orderInfo = TODO

                _orderInfo.value = OrderInfoUiState(
                    price = 10,
                    menuName = "Menu Italiano"
                )
            } catch (e: Exception) {
                _orderInfo.value = null
            }
        }
    }

    fun updateProfile(updatedProfile: Profile) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                profileModel.updateUser(updatedProfile)
                _profile.value = updatedProfile
                checkProfileCompleteness(updatedProfile)
            } catch (e: Exception) {
                // Gestione errori
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkProfileCompleteness(profile: Profile?): Boolean {
        // Un profilo è considerato completo se i campi essenziali non sono nulli
        return !(
                profile?.firstName.isNullOrBlank() &&
                        profile?.lastName.isNullOrBlank() &&
                        profile?.cardFullName.isNullOrBlank() &&
                        profile?.cardNumber == null &&
                        profile?.cardExpireMonth == null &&
                        profile?.cardExpireYear == null &&
                        profile?.cardCVV == null
                )
    }
}