package com.example.mangiaebasta.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    var firstName    by mutableStateOf("Mario")
    var lastName     by mutableStateOf("Rossi")
    var cardFullName by mutableStateOf("Mario Rossi")
    var cardNumber   by mutableStateOf("1234 5678 9012 3456")
    var cardExpire   by mutableStateOf("08/27")
    var cardCVV      by mutableStateOf("123")

    fun update(
        firstName:    String,
        lastName:     String,
        cardNumber:   String,
        expireMonth:  String,
        expireYear:   String,
        cvv:          String
    ) {
        this.firstName     = firstName
        this.lastName      = lastName
        this.cardFullName  = "$firstName $lastName"
        this.cardNumber    = cardNumber
        this.cardExpire    = "$expireMonth/$expireYear"
        this.cardCVV       = cvv
    }
}
