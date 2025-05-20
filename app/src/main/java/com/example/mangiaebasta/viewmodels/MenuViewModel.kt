package com.example.mangiaebasta.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangiaebasta.models.MenuModel
import com.example.mangiaebasta.models.Order
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.mangiaebasta.models.OrderModel

class MenuViewModel : ViewModel() {
    private val menuModel = MenuModel()
    private val orderModel = OrderModel()

    // --- STREAM PER LA LISTA ---
    private val _menusUi = MutableStateFlow<List<MenuListItemUiState>>(emptyList())
    val menusUi: StateFlow<List<MenuListItemUiState>> = _menusUi

    // --- STREAM PER IL DETTAGLIO ---
    private val _selectedMenuUi = MutableStateFlow<MenuDetailUiState?>(null)
    val selectedMenuUi: StateFlow<MenuDetailUiState?> = _selectedMenuUi

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init { loadMenus() }

    fun loadMenus() = viewModelScope.launch {
        _isLoading.value = true
        try {
            val menus = menuModel.getMenus()
            _menusUi.value = menus.map { menu ->
                MenuListItemUiState(
                    mid = menu.mid,
                    name = menu.name,
                    shortDescription = menu.shortDescription,
                    priceText = "€${"%.2f".format(menu.price)}",
                    deliveryTimeText = "${menu.deliveryTime} min",
                    imageBase64 = menu.image
                )
            }
        } catch (e: Exception) {
            // Gestione degli errori
            _menusUi.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    fun loadMenu(id: Int) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val menu = menuModel.getMenuDetails(id)
            _selectedMenuUi.value = MenuDetailUiState(
                mid = menu.mid,
                name = menu.name,
                longDescription = menu.longDescription,
                priceText = "€${"%.2f".format(menu.price)}",
                deliveryTimeText = "${menu.deliveryTime} min",
                imageBase64 = menu.image
            )
        } catch (e: Exception) {
            // Gestione degli errori
            _selectedMenuUi.value = null
        } finally {
            _isLoading.value = false
        }
    }

    fun orderMenu(menuId: Int) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val order = orderModel.order(menuId)
        } catch (e: Exception) {
            // TODO
        } finally {
            _isLoading.value = false
        }
    }
}

// Stato dell'ordine
sealed class OrderStatus {
    data class Success(val order: Order) : OrderStatus()
    data class Error(val message: String) : OrderStatus()
}