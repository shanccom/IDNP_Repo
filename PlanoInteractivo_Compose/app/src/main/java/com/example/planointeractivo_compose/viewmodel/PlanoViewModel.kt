package com.example.planointeractivo_compose.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.compose.ui.geometry.Offset
import com.example.planointeractivo_compose.Marcador

// ViewModel del plano: mantiene el estado de los marcadores y la selección
class PlanoViewModel(application: Application) : AndroidViewModel(application) {

    var marcadores by mutableStateOf<List<Marcador>>(emptyList())
        private set

    var marcadorSeleccionado by mutableStateOf<Marcador?>(null)
        private set

    init {
        marcadores = cargarMarcadores(application)
    }

    fun seleccionarMarcador(marcador: Marcador?) {
        marcadorSeleccionado = marcador
    }

    fun limpiarSeleccion() {
        marcadorSeleccionado = null
    }
}

// Carga los marcadores desde assets/marcadores.csv (patrón línea-a-línea del Lab 03)
private fun cargarMarcadores(context: Context): List<Marcador> {
    return try {
        val contenido = context.assets.open("marcadores.csv")
            .bufferedReader()
            .use { it.readText() }

        contenido.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .mapNotNull { linea ->
                val partes = linea.split(",")
                if (partes.size >= 5) {
                    Marcador(
                        offset = Offset(
                            partes[0].trim().toFloat(),
                            partes[1].trim().toFloat()
                        ),
                        radio = partes[2].trim().toFloat(),
                        titulo = partes[3].trim(),
                        descripcion = partes.drop(4).joinToString(",").trim()
                    )
                } else {
                    null
                }
            }
            .toList()
    } catch (e: Exception) {
        emptyList()
    }
}
