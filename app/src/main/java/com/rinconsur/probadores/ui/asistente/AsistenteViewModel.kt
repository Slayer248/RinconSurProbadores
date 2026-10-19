package com.rinconsur.probadores.ui.asistente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rinconsur.probadores.data.local.AppDatabase
import com.rinconsur.probadores.data.local.entities.ProbadorEntity
import com.rinconsur.probadores.data.local.entities.TurnoEntity
import com.rinconsur.probadores.utils.DataInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AsistenteViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val probadorDao = db.probadorDao()
    private val turnoDao = db.turnoDao()

    // Conversión segura de Flow a LiveData especificada en el hilo IO
    val probadoresLiveData: LiveData<List<ProbadorEntity>> =
        probadorDao.obtenerTodosProbadores().asLiveData(Dispatchers.IO)

    val turnosEnEsperaLiveData: LiveData<List<TurnoEntity>> =
        turnoDao.obtenerTurnosPorEstado("EN_ESPERA").asLiveData(Dispatchers.IO)

    init {
        // Inicialización diferida fuera del hilo principal
        viewModelScope.launch(Dispatchers.IO) {
            DataInitializer.inicializarProbadoresSiEstaVacio(probadorDao)
        }
    }

    fun registrarTurno(prendas: Int, esPreferente: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val nuevoTurno = TurnoEntity(
                numeroTurno = "T-${System.currentTimeMillis().toString().takeLast(3)}",
                fechaHoraIngreso = System.currentTimeMillis(),
                esPreferente = esPreferente,
                cantidadPrendas = prendas,
                estado = "EN_ESPERA",
                rutColaborador = "16.842.311-K"
            )
            turnoDao.insertarTurno(nuevoTurno)
        }
    }

    fun cambiarEstadoProbador(probador: ProbadorEntity, nuevoEstado: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val actualizado = probador.copy(
                estado = nuevoEstado,
                desdeHora = System.currentTimeMillis().toString()
            )
            probadorDao.actualizarProbador(actualizado)

            if (nuevoEstado == "LIBRE") {
                atenderSiguienteFila()
            }
        }
    }

    private suspend fun atenderSiguienteFila() {
        val siguienteTurno = turnoDao.obtenerSiguienteTurnoEnEspera()
        siguienteTurno?.let { turno ->
            val turnoAtendido = turno.copy(estado = "EN_PROBADOR")
            turnoDao.actualizarTurno(turnoAtendido)
        }
    }
}