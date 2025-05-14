package com.example.mangiaebasta.models

data class ProfileModel(
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val cardFullName: String,
    val cardNumber: String,
    val cardExpireMonth: Int,
    val cardExpireYear: Int,
    val cardCVV: String,
    val orderStatus: String,
    val menuName: String?
) {
    companion object {
        fun getCurrent(): ProfileModel = ProfileModel(
            userId           = 42,
            firstName        = "Mario",
            lastName         = "Rossi",
            cardFullName     = "Mario Rossi",
            cardNumber       = "1234 5678 9012 3456",
            cardExpireMonth  = 12,
            cardExpireYear   = 2025,
            cardCVV          = "123",
            orderStatus      = "Consegnato",
            menuName         = "Pizza Margherita"
        )
    }
}