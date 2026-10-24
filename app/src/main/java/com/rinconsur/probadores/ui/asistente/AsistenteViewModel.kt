package com.rinconsur.probadores.ui.asistente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rinconsur.probadores.data.local.AppDatabase
import com.rinconsur.probadores.data.local.entities.ProbadorEntity
import com.rinconsur.probadores.data.local.entities.TurnoEntity
import com.rinconsur.probadores.data.local.entities.UsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AsistenteViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val probadorDao = db.probadorDao()
    private val turnoDao = db.turnoDao()
    private val usuarioDao = db.usuarioDao()

    // --- Flujos y LiveData existentes ---
    val probadoresLiveData: LiveData<List<ProbadorEntity>> =
        probadorDao.obtenerTodosProbadores().asLiveData(Dispatchers.IO)

    val turnosEnEsperaLiveData: LiveData<List<TurnoEntity>> =
        turnoDao.obtenerTurnosEnEspera().asLiveData(Dispatchers.IO)
    fun registrarTurno(nombreCliente: String, cantidadPrendas: Int, esPreferente: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val ultimoNumero = turnoDao.obtenerUltimoNumero() ?: 1000
            val nuevoNumeroTurno = "T-${ultimoNumero + 1}"

            val nuevoTurno = TurnoEntity(
                numeroTurno = nuevoNumeroTurno,
                nombreCliente = nombreCliente,
                cantidadPrendas = cantidadPrendas,
                esPreferente = esPreferente,
                estado = "EN_ESPERA"
            )
            turnoDao.insertarTurno(nuevoTurno)
        }
    }

    fun cambiarEstadoProbador(probador: ProbadorEntity, nuevoEstado: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val actualizado = probador.copy(estado = nuevoEstado)
            probadorDao.actualizarProbador(actualizado)
        }
    }

    // --- NUEVO: Funciones para Autenticación de Usuarios ---
    fun autenticar(rut: String, pass: String, onResultado: (UsuarioEntity?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val usuario = usuarioDao.obtenerUsuarioPorRut(rut)
            if (usuario != null && usuario.contrasena == pass) {
                onResultado(usuario)
            } else {
                onResultado(null)
            }
        }
    }
}