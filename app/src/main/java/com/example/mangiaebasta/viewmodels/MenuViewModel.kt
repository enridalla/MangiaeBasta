package com.example.mangiaebasta.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangiaebasta.models.MenuModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MenuViewModel : ViewModel() {

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
        val domain = MenuModel.getAll()
        _menusUi.value = domain.map { menu ->
            MenuListItemUiState(
                mid = menu.mid,
                name = menu.name,
                shortDescription = menu.shortDescription,
                priceText = "€${"%.2f".format(menu.price)}",
                deliveryTimeText = "${menu.deliveryTime} min",
                imageBase64 = menu.image
            )
        }
        _isLoading.value = false
    }

    fun loadMenu(id: Int) = viewModelScope.launch {
        _isLoading.value = true
        MenuModel.getById(id)?.let { menu ->
            _selectedMenuUi.value = MenuDetailUiState(
                mid = menu.mid,
                name = menu.name,
                longDescription = menu.longDescription,
                priceText = "€${"%.2f".format(menu.price)}",
                deliveryTimeText = "${menu.deliveryTime} min",
                imageBase64 = menu.image
            )
        }
        _isLoading.value = false
    }
}
