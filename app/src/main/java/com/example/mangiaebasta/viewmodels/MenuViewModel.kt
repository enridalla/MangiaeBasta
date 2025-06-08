package com.example.mangiaebasta.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangiaebasta.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MenuViewModel : ViewModel() {
    private val menuModel = MenuModel()
    private val orderModel = OrderModel()

    // --- LISTA MENU ---------------------------------------------------------------------------
    private val _menus = MutableStateFlow<List<MenuItemWithImage>>(emptyList())
    val menusUi: StateFlow<List<MenuItemWithImage>> = _menus

    // --- DETTAGLIO ----------------------------------------------------------------------------
    private val _selectedMenu = MutableStateFlow<DetailedMenuItemWithImage?>(null)
    val selectedMenu: StateFlow<DetailedMenuItemWithImage?> = _selectedMenu

    // --- STATO CARICAMENTO --------------------------------------------------------------------
    private val _isLoading = MutableStateFlow(false) // Cambiato da true a false
    val isLoading: StateFlow<Boolean> = _isLoading

    // Rimosso init { loadMenus() }

    /** Carica la lista dei menu */
    fun loadMenus() = viewModelScope.launch {
        _isLoading.value = true
        try {
            // fetch + immagini in parallelo (vedi MenuModel)
            _menus.value = menuModel.getMenus()
        } catch (_: Exception) {
            _menus.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    /** Carica i dettagli di un singolo menu */
    fun loadMenu(id: Int) = viewModelScope.launch {
        _selectedMenu.value = null
        _isLoading.value = true
        try {
            _selectedMenu.value = menuModel.getMenuDetails(id)
        } catch (_: Exception) {
            _selectedMenu.value = null
        } finally {
            _isLoading.value = false
        }
    }

    /** Effettua l'ordine e restituisce un eventuale messaggio d'errore */
    suspend fun orderMenu(menuId: Int): String? {
        _isLoading.value = true
        return try {
            orderModel.order(menuId)
            orderModel.saveLastOrder(_selectedMenu.value)
            null                                        // Successo
        } catch (e: Exception) {
            // Trasforma "API Error: …" in messaggio user-friendly
            e.message?.replace("API Error: ", "")
                ?: "Si è verificato un errore sconosciuto"
        } finally {
            _isLoading.value = false
        }
    }
}