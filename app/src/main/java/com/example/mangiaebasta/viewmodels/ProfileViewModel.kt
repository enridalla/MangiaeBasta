package com.example.mangiaebasta.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangiaebasta.models.Profile
import com.example.mangiaebasta.models.ProfileModel
import com.example.mangiaebasta.models.DetailedMenuItemWithImage
import com.example.mangiaebasta.models.OrderModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val profileModel = ProfileModel()
    private val orderModel = OrderModel()

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile

    private val _orderInfo = MutableStateFlow<DetailedMenuItemWithImage?>(null)
    val orderInfo: StateFlow<DetailedMenuItemWithImage?> = _orderInfo

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profile = profileModel.getProfileInfo()
                _profile.value = profile
                checkProfileCompleteness(profile)
            } catch (e: Exception) {
                _profile.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadOrderInfo() {
        viewModelScope.launch {
            try {
                _orderInfo.value = orderModel.loadLastOrder()
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