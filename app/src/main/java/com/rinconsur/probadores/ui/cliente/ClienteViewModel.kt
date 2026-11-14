package com.rinconsur.probadores.ui.cliente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rinconsur.probadores.data.local.entities.ProbadorEntity
import com.rinconsur.probadores.data.local.entities.TurnoEntity
import com.rinconsur.probadores.data.repository.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ClienteViewModel @JvmOverloads constructor(
    application: Application,
    private val turnoRepository: TurnoRepository = TurnoRepositoryImpl(),
    private val probadorRepository: ProbadorRepository = ProbadorRepositoryImpl()
) : AndroidViewModel(application) {

    private val _probadores = MutableLiveData<List<ProbadorEntity>>(emptyList())
    val probadoresLiveData: LiveData<List<ProbadorEntity>> = _probadores

    private val _turnosEnEspera = MutableLiveData<List<TurnoEntity>>(emptyList())
    val turnosEnEsperaLiveData: LiveData<List<TurnoEntity>> = _turnosEnEspera

    init {
        iniciarAutoRefresco()
    }

    private fun iniciarAutoRefresco() {
        viewModelScope.launch {
            while (isActive) {
                refrescarDatos()
                delay(10_000L) // Refresco cada 10 segundos
            }
        }
    }

    fun refrescarDatos() {
        viewModelScope.launch {
            _probadores.value = probadorRepository.obtenerTodosProbadores()
            _turnosEnEspera.value = turnoRepository.obtenerTurnosPorEstado("EN_ESPERA")
        }
    }

    fun validarCantidadPrendas(prendasText: String): String? {
        val clean = prendasText.trim()
        val prendas = clean.toIntOrNull()
        return when {
            clean.isEmpty() -> "Por favor ingresa la cantidad de prendas"
            prendas == null || prendas < 1 || prendas > 6 -> "La cantidad de prendas debe ser entre 1 y 6"
            else -> null
        }
    }

    fun registrarTurno(
        nombreCliente: String,
        cantidadPrendas: Int,
        esPreferente: Boolean,
        onResultado: (exito: Boolean, mensaje: String) -> Unit
    ) {
        viewModelScope.launch {
            val (exito, mensaje) = turnoRepository.registrarTurno(
                nombreCliente = nombreCliente,
                cantidadPrendas = cantidadPrendas,
                esPreferente = esPreferente
            )
            if (exito) refrescarDatos()
            onResultado(exito, mensaje)
        }
    }
}