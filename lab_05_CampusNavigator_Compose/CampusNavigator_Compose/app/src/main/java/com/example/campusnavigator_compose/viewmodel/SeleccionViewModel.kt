package com.example.campusnavigator_compose.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
class SeleccionViewModel : ViewModel() {

    companion object {
        const val SIN_SELECCION = "Ninguno"
    }

    var edificioSeleccionado by mutableStateOf(SIN_SELECCION)
        private set

    private val _edificioSeleccionadoFlow = MutableStateFlow(SIN_SELECCION)

    val edificioSeleccionadoFlow: StateFlow<String> =
        _edificioSeleccionadoFlow.asStateFlow()

    fun seleccionarEdificio(nombre: String) {
        edificioSeleccionado = nombre
        _edificioSeleccionadoFlow.value = nombre
    }

    fun limpiarSeleccion() {
        edificioSeleccionado = SIN_SELECCION
        _edificioSeleccionadoFlow.value = SIN_SELECCION
    }
}
