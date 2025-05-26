package com.example.mangiaebasta.models

import kotlinx.serialization.Serializable

@Serializable
data class MenuItem(
    val mid: Int,
    val name: String,
    val price: Double,
    val location: Location,
    val imageVersion: Int,
    val shortDescription: String,
    val deliveryTime: Int
)

@Serializable
data class DetailedMenuItem(
    val mid: Int,
    val name: String,
    val price: Double,
    val location: Location,
    val imageVersion: Int,
    val shortDescription: String,
    val deliveryTime: Int,
    val longDescription: String
)

@Serializable
data class MenuItemWithImage(
    val mid: Int,
    val name: String,
    val price: Double,
    val location: Location,
    val imageVersion: Int,
    val shortDescription: String,
    val deliveryTime: Int,
    val image: String?
)

@Serializable
data class DetailedMenuItemWithImage(
    val mid: Int,
    val name: String,
    val price: Double,
    val location: Location,
    val imageVersion: Int,
    val shortDescription: String,
    val deliveryTime: Int,
    val longDescription: String,
    val image: String?
)

@Serializable
data class Base64Response(
    val base64: String
)

@Serializable
data class Location(
    val lat: Double,
    val lng: Double,
    val deliveryTimestamp: String? = null // Add this optional field
)

@Serializable
data class Order(
    val oid: Int,
    val mid: Int,
    val uid: Int,
    val creationTimestamp: String,
    val status: String,
    val deliveryLocation: Location,
    val expectedDeliveryTimestamp: String? = null,
    val currentPosition: Location,
    val deliveryTimestamp: String? = null
)

@Serializable
data class DeliveryLocationWithSid(
    val sid: String,
    val deliveryLocation: Location
)

@Serializable
data class Profile(
    val firstName: String?,
    val lastName: String?,
    val cardFullName: String?,
    val cardNumber: Long?,
    val cardExpireMonth: Int?,
    val cardExpireYear: Int?,
    val cardCVV: Int?,
    val uid: Int,
)

@Serializable
data class User(
    val uid: Int,
    val sid: String
)

@Serializable
data class ProfileToUpdate(
    val firstName: String,
    val lastName: String,
    val cardFullName: String,
    val cardNumber: String,
    val cardExpireMonth: String,
    val cardExpireYear: String,
    val cardCVV: String,
    val sid: String
)