package com.example.mangiaebasta.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangiaebasta.models.ProfileModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _infoUi = MutableStateFlow<ProfileInfoUiState?>(null)
    val infoUi: StateFlow<ProfileInfoUiState?> = _infoUi.asStateFlow()

    private val _editUi = MutableStateFlow<ProfileEditUiState?>(null)
    val editUi: StateFlow<ProfileEditUiState?> = _editUi.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init { loadProfile() }

    fun loadProfile() = viewModelScope.launch {
        _isLoading.value = true
        val p = ProfileModel.getCurrent()
        val info = ProfileInfoUiState(
            firstName       = p.firstName,
            lastName        = p.lastName,
            cardFullName    = p.cardFullName,
            cardNumber      = p.cardNumber,
            cardExpireMonth = p.cardExpireMonth,
            cardExpireYear  = p.cardExpireYear,
            cardCVV         = p.cardCVV,
            orderStatus     = p.orderStatus,
            menuName        = p.menuName
        )
        _infoUi.value = info
        _editUi.value = info.let {
            ProfileEditUiState(
                firstName       = it.firstName,
                lastName        = it.lastName,
                cardFullName    = it.cardFullName,
                cardNumber      = it.cardNumber,
                cardExpireMonth = it.cardExpireMonth,
                cardExpireYear  = it.cardExpireYear,
                cardCVV         = it.cardCVV,
                orderStatus     = it.orderStatus,
                menuName        = it.menuName
            )
        }
        _isLoading.value = false
    }

    fun updateProfile(changes: ProfileEditUiState) = viewModelScope.launch {
        _isLoading.value = true
        // Qui potresti fare chiamata a server/DB.
        _infoUi.value = ProfileInfoUiState(
            firstName       = changes.firstName,
            lastName        = changes.lastName,
            cardFullName    = changes.cardFullName,
            cardNumber      = changes.cardNumber,
            cardExpireMonth = changes.cardExpireMonth,
            cardExpireYear  = changes.cardExpireYear,
            cardCVV         = changes.cardCVV,
            orderStatus     = changes.orderStatus,
            menuName        = changes.menuName
        )
        _isLoading.value = false
    }
}