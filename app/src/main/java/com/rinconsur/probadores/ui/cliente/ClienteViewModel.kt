package com.rinconsur.probadores.ui.cliente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rinconsur.probadores.data.local.AppDatabase
import com.rinconsur.probadores.data.local.entities.ProbadorEntity
import com.rinconsur.probadores.data.local.entities.TurnoEntity
import com.rinconsur.probadores.data.repository.ProbadorRepository
import com.rinconsur.probadores.data.repository.ProbadorRepositoryImpl
import com.rinconsur.probadores.data.repository.TurnoRepository
import com.rinconsur.probadores.data.repository.TurnoRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClienteViewModel @JvmOverloads constructor(
    application: Application,
    private val turnoRepository: TurnoRepository = TurnoRepositoryImpl(
        AppDatabase.getDatabase(application).turnoDao()
    ),
    probadorRepository: ProbadorRepository = ProbadorRepositoryImpl(
        AppDatabase.getDatabase(application).probadorDao(),
        AppDatabase.getDatabase(application).turnoDao()
    )
) : AndroidViewModel(application) {

    val probadoresLiveData: LiveData<List<ProbadorEntity>> =
        probadorRepository.obtenerTodosProbadores().asLiveData(Dispatchers.IO)

    val turnosEnEsperaLiveData: LiveData<List<TurnoEntity>> =
        turnoRepository.obtenerTurnosPorEstado("EN_ESPERA").asLiveData(Dispatchers.IO)

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
            onResultado(exito, mensaje)
        }
    }
}
