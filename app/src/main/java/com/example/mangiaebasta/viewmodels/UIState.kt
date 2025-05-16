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

data class ProfileInfoUiState(
    val firstName: String,
    val lastName: String,
    val cardFullName: String,
    val cardNumber: String,
    val cardExpireMonth: Int,
    val cardExpireYear: Int,
    val cardCVV: String,
    val orderStatus: String,
    val menuName: String?
)

data class ProfileEditUiState(
    val firstName: String,
    val lastName: String,
    val cardFullName: String,
    val cardNumber: String,
    val cardExpireMonth: Int,
    val cardExpireYear: Int,
    val cardCVV: String,
    val orderStatus: String,
    val menuName: String?
)