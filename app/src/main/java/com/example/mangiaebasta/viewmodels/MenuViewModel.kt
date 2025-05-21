package com.example.mangiaebasta.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangiaebasta.models.DetailedMenuItem
import com.example.mangiaebasta.models.DetailedMenuItemWithImage
import com.example.mangiaebasta.models.MenuItemWithImage
import com.example.mangiaebasta.models.MenuModel
import com.example.mangiaebasta.models.Order
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.mangiaebasta.models.OrderModel

class MenuViewModel : ViewModel() {
    private val menuModel = MenuModel()
    private val orderModel = OrderModel()

    // --- STREAM PER LA LISTA ---
    private val _menusUi = MutableStateFlow<List<MenuItemWithImage>>(emptyList())
    val menusUi: StateFlow<List<MenuItemWithImage>> = _menusUi

    // --- STREAM PER IL DETTAGLIO ---
    private val _selectedMenu = MutableStateFlow<DetailedMenuItemWithImage?>(null)
    val selectedMenu: StateFlow<DetailedMenuItemWithImage?> = _selectedMenu

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // --- STREAM PER LO STATO DELL'ORDINE ---
    private val _orderStatus = MutableStateFlow<OrderStatus?>(null)
    val orderStatus: StateFlow<OrderStatus?> = _orderStatus

    init { loadMenus() }

    fun loadMenus() = viewModelScope.launch {
        _isLoading.value = true
        try {
            val menus = menuModel.getMenus()
            _menusUi.value = menus
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
            _selectedMenu.value = menu
        } catch (e: Exception) {
            // Gestione degli errori
            _selectedMenu.value = null
        } finally {
            _isLoading.value = false
        }
    }

    fun orderMenu(menuId: Int) = viewModelScope.launch {
        _isLoading.value = true
        _orderStatus.value = null // Reset dello stato precedente
        try {
            val order = orderModel.order(menuId)
            orderModel.saveLastOrder(_selectedMenu.value)
            if (order != null) {
                _orderStatus.value = OrderStatus.Success(order)
            } else {
                _orderStatus.value = OrderStatus.Error("Non è stato possibile completare l'ordine")
            }
        } catch (e: Exception) {
            // Gestione degli specifici messaggi di errore dall'API
            val errorMsg = e.message?.replace("API Error: ", "") ?: "Si è verificato un errore sconosciuto"
            _orderStatus.value = OrderStatus.Error(errorMsg)
        } finally {
            _isLoading.value = false
        }
    }

    // Resetta lo stato dell'ordine quando necessario
    fun resetOrderStatus() {
        _orderStatus.value = null
    }
}

// Stato dell'ordine
sealed class OrderStatus {
    data class Success(val order: Order) : OrderStatus()
    data class Error(val message: String) : OrderStatus()
}