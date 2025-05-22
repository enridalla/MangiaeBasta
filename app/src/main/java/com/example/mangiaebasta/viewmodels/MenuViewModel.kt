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
        try {
            val order = orderModel.order(menuId)
            orderModel.saveLastOrder(_selectedMenu.value)
        } catch (e: Exception) {
            // Gestione degli specifici messaggi di errore dall'API
            val errorMsg = e.message?.replace("API Error: ", "") ?: "Si è verificato un errore sconosciuto"
        } finally {
            _isLoading.value = false
        }
    }
}