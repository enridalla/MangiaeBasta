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

    suspend fun updateProfile(updatedProfile: Profile): UpdateResult {
        // Prima valida i dati
        val validationResult = validateProfile(updatedProfile)
        if (validationResult != null) {
            return UpdateResult.ValidationError(validationResult)
        }

        _isLoading.value = true
        return try {
            profileModel.updateUser(updatedProfile)
            _profile.value = updatedProfile
            checkProfileCompleteness(updatedProfile)
            UpdateResult.Success
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("204") == true -> return UpdateResult.Success // 204 è successo
                e.message?.contains("401") == true -> "La tua sessione è scaduta. Riapri l'app per continuare."
                e.message?.contains("404") == true -> "Il tuo profilo non è stato trovato. Riapri l'app per continuare."
                e.message?.contains("422") == true -> "I dati che hai inserito non sono corretti."
                e.message?.contains("network", ignoreCase = true) == true -> "Errore di connessione. Controlla la tua connessione internet e riprova."
                else -> "Si è verificato un errore durante l'aggiornamento. Riprova più tardi."
            }
            UpdateResult.Error(errorMessage)
        } finally {
            _isLoading.value = false
        }
    }

    sealed class UpdateResult {
        object Success : UpdateResult()
        data class Error(val message: String) : UpdateResult()
        data class ValidationError(val message: String) : UpdateResult()
    }

    private fun validateProfile(profile: Profile): String? {
        // Validazione nome e cognome
        if (profile.firstName.isNullOrBlank()) {
            return "Il nome è obbligatorio"
        }
        if (profile.firstName.length < 2) {
            return "Il nome deve contenere almeno 2 caratteri"
        }
        if (profile.firstName.length > 15) {
            return "Il nome non può superare i 15 caratteri"
        }
        if (profile.lastName.isNullOrBlank()) {
            return "Il cognome è obbligatorio"
        }
        if (profile.lastName.length < 2) {
            return "Il cognome deve contenere almeno 2 caratteri"
        }
        if (profile.lastName.length > 15) {
            return "Il nome non può superare i 15 caratteri"
        }

        // Validazione dati carta
        if (profile.cardFullName!!.length > 31) {
            return "L'intestatario della carta non può superare i 31 caratteri"
        }
        if (profile.cardFullName.length < 4) {
            return "L'intestatario della carta deve contenere almeno 4 caratteri"
        }

        // Validazione numero carta
        if (profile.cardNumber == null) {
            return "Il numero della carta è obbligatorio"
        }
        val cardNumberStr = profile.cardNumber.toString()
        if (cardNumberStr.length < 13 || cardNumberStr.length > 19) {
            return "Il numero della carta deve essere tra 13 e 19 cifre"
        }

        // Validazione mese scadenza
        if (profile.cardExpireMonth == null) {
            return "Il mese di scadenza è obbligatorio"
        }
        if (profile.cardExpireMonth!! < 1 || profile.cardExpireMonth!! > 12) {
            return "Il mese di scadenza deve essere compreso tra 1 e 12"
        }

        // Validazione anno scadenza
        if (profile.cardExpireYear == null) {
            return "L'anno di scadenza è obbligatorio"
        }
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        if (profile.cardExpireYear!! < currentYear || profile.cardExpireYear!! > currentYear + 20) {
            return "L'anno di scadenza non è valido"
        }

        // Validazione CVV
        if (profile.cardCVV == null) {
            return "Il CVV è obbligatorio"
        }
        val cvvStr = profile.cardCVV.toString()
        if (cvvStr.length < 3 || cvvStr.length > 4) {
            return "Il CVV deve essere di 3 o 4 cifre"
        }

        return null // Tutto ok
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