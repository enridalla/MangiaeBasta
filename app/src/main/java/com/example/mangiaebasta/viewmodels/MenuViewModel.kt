package com.example.mangiaebasta.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangiaebasta.models.MenuModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MenuViewModel : ViewModel() {

    private val _menus = MutableStateFlow<List<MenuModel>>(emptyList())
    val menus: StateFlow<List<MenuModel>> = _menus.asStateFlow()

    private val _selectedMenu = MutableStateFlow<MenuModel?>(null)
    val selectedMenu: StateFlow<MenuModel?> = _selectedMenu.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init { loadMenus() }

    fun loadMenus() = viewModelScope.launch {
        _isLoading.value = true
        _menus.value = MenuModel.getAll()
        _isLoading.value = false
    }

    fun loadMenu(id: Int) = viewModelScope.launch {
        _isLoading.value = true
        _selectedMenu.value = MenuModel.getById(id)
        _isLoading.value = false
    }
}