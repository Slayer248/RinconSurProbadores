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
import kotlinx.coroutines.withContext

class AsistenteViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val probadorDao = db.probadorDao()
    private val turnoDao = db.turnoDao()
    private val usuarioDao = db.usuarioDao()

    val probadoresLiveData: LiveData<List<ProbadorEntity>> =
        probadorDao.obtenerTodosProbadores().asLiveData(Dispatchers.IO)

    val turnosEnEsperaLiveData: LiveData<List<TurnoEntity>> =
        turnoDao.obtenerTurnosPorEstado("EN_ESPERA").asLiveData(Dispatchers.IO)

    val usuariosLiveData: LiveData<List<UsuarioEntity>> =
        usuarioDao.obtenerTodosUsuarios().asLiveData(Dispatchers.IO)

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

    // Al cambiar un probador a OCUPADO, desencolamos automáticamente al cliente en turno
    fun cambiarEstadoProbador(probador: ProbadorEntity, nuevoEstado: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val actualizado = probador.copy(estado = nuevoEstado)
            probadorDao.actualizarProbador(actualizado)

            // Si el probador se marca como OCUPADO, liberamos/atendemos al cliente en la fila
            if (nuevoEstado == "OCUPADO") {
                val siguienteTurno = turnoDao.obtenerSiguienteTurnoEnEspera()
                siguienteTurno?.let { turno ->
                    turnoDao.actualizarEstadoTurno(turno.id, "ATENDIDO")
                }
            }
        }
    }

    fun autenticar(usernameInput: String, passInput: String, onResultado: (String?) -> Unit) {
        viewModelScope.launch {
            val rol = withContext(Dispatchers.IO) {
                val cleanUser = usernameInput.trim()
                val cleanPass = passInput.trim()

                // Credenciales maestras por defecto para administrador
                if (cleanUser.equals("admin", ignoreCase = true) && 
                    (cleanPass == "admin1234" || cleanPass == "admin")
                ) {
                    return@withContext "ADMIN"
                }

                val usuarioBD = usuarioDao.obtenerUsuarioPorUsername(cleanUser)
                if (usuarioBD != null && usuarioBD.contrasena == cleanPass) {
                    usuarioBD.rol
                } else {
                    null
                }
            }
            onResultado(rol)
        }
    }

    fun crearUsuario(nombre: String, username: String, pass: String, rol: String, onCompleto: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val nuevoUsuario = UsuarioEntity(
                    username = username.trim(),
                    nombre = nombre.trim(),
                    contrasena = pass.trim(),
                    rol = rol
                )
                usuarioDao.insertarUsuario(nuevoUsuario)
            }
            onCompleto()
        }
    }
}