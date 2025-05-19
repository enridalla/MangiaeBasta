package com.example.mangiaebasta.viewmodels

// Classi di stato UI per la visualizzazione dei menu
data class MenuListItemUiState(
    val mid: Int,
    val name: String,
    val shortDescription: String,
    val priceText: String,
    val deliveryTimeText: String,
    val imageBase64: String?
)

data class MenuDetailUiState(
    val mid: Int,
    val name: String,
    val longDescription: String,
    val priceText: String,
    val deliveryTimeText: String,
    val imageBase64: String?
)

/// Dati del profilo utente
data class ProfileInfoUiState(
    val firstName: String = "",
    val lastName: String = "",
    val cardFullName: String = "",
    val cardNumber: String = "",
    val cardExpireMonth: Int = 0,
    val cardExpireYear: Int = 0,
    val cardCVV: String = ""
)

// Dati dell'ordine
data class OrderInfoUiState(
    val orderStatus: String = "",
    val menuName: String? = null
)

// Stesso tipo per modifica del profilo
typealias ProfileEditUiState = ProfileInfoUiState