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
import com.rinconsur.probadores.data.repository.ProbadorRepository
import com.rinconsur.probadores.data.repository.ProbadorRepositoryImpl
import com.rinconsur.probadores.data.repository.TurnoRepository
import com.rinconsur.probadores.data.repository.TurnoRepositoryImpl
import com.rinconsur.probadores.data.repository.UsuarioRepository
import com.rinconsur.probadores.data.repository.UsuarioRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AsistenteViewModel @JvmOverloads constructor(
    application: Application,
    private val probadorRepository: ProbadorRepository = ProbadorRepositoryImpl(
        AppDatabase.getDatabase(application).probadorDao(),
        AppDatabase.getDatabase(application).turnoDao()
    ),
    turnoRepository: TurnoRepository = TurnoRepositoryImpl(
        AppDatabase.getDatabase(application).turnoDao()
    ),
    private val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl(
        AppDatabase.getDatabase(application).usuarioDao()
    )
) : AndroidViewModel(application) {

    val probadoresLiveData: LiveData<List<ProbadorEntity>> =
        probadorRepository.obtenerTodosProbadores().asLiveData(Dispatchers.IO)

    val turnosEnEsperaLiveData: LiveData<List<TurnoEntity>> =
        turnoRepository.obtenerTurnosPorEstado("EN_ESPERA").asLiveData(Dispatchers.IO)

    fun cambiarEstadoProbador(probador: ProbadorEntity, nuevoEstado: String) {
        viewModelScope.launch {
            probadorRepository.cambiarEstadoProbador(probador, nuevoEstado)
        }
    }

    fun obtenerUsuario(username: String, onResultado: (UsuarioEntity?) -> Unit) {
        viewModelScope.launch {
            val usuario = usuarioRepository.obtenerUsuarioPorUsername(username)
            onResultado(usuario)
        }
    }

    fun validarUsername(username: String): String? {
        val clean = username.trim()
        return when {
            clean.isEmpty() -> "El nombre de usuario es obligatorio"
            clean.length < 3 -> "El usuario debe tener al menos 3 caracteres"
            else -> null
        }
    }

    fun validarNombreCompleto(nombre: String): String? {
        val clean = nombre.trim()
        return when {
            clean.isEmpty() -> "El nombre completo es obligatorio"
            clean.length < 3 -> "El nombre debe tener al menos 3 caracteres"
            else -> null
        }
    }

    fun validarCorreo(correo: String): String? {
        val clean = correo.trim()
        return when {
            clean.isEmpty() -> "El correo electrónico es obligatorio"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(clean).matches() -> "Ingresa un correo válido (ej: usuario@correo.com)"
            else -> null
        }
    }

    fun modificarPerfil(
        currentUsername: String,
        passwordActual: String,
        nuevoNombre: String,
        nuevoCorreo: String,
        nuevoUsername: String,
        nuevaPassword: String,
        onResultado: (exito: Boolean, mensaje: String, nuevoUsername: String?) -> Unit
    ) {
        viewModelScope.launch {
            val (exito, mensaje, updatedUser) = usuarioRepository.modificarPerfil(
                currentUsername = currentUsername,
                passwordActual = passwordActual,
                nuevoNombre = nuevoNombre,
                nuevoCorreo = nuevoCorreo,
                nuevoUsername = nuevoUsername,
                nuevaPassword = nuevaPassword
            )
            onResultado(exito, mensaje, updatedUser)
        }
    }
}
