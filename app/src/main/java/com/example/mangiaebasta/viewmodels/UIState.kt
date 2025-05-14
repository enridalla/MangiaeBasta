package com.example.mangiaebasta.viewmodels

data class MenuListItemUiState(
    val mid: Int,
    val name: String,
    val shortDescription: String,
    val priceText: String,        // es. "€8.50"
    val deliveryTimeText: String, // es. "30 min"
    val imageBase64: String?      // raw Base64, UI decodifica
)

data class MenuDetailUiState(
    val mid: Int,
    val name: String,
    val longDescription: String,
    val priceText: String,
    val deliveryTimeText: String,
    val imageBase64: String?
)
