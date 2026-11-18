package com.rinconsur.probadores.ui.asistente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rinconsur.probadores.data.local.entities.ProbadorEntity
import com.rinconsur.probadores.data.local.entities.TurnoEntity
import com.rinconsur.probadores.data.local.entities.UsuarioEntity
import com.rinconsur.probadores.data.repository.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AsistenteViewModel @JvmOverloads constructor(
    application: Application,
    private val probadorRepository: ProbadorRepository = ProbadorRepositoryImpl(),
    private val turnoRepository: TurnoRepository = TurnoRepositoryImpl(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl()
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

    fun cambiarEstadoProbador(probador: ProbadorEntity, nuevoEstado: String) {
        viewModelScope.launch {
            probadorRepository.cambiarEstadoProbador(probador, nuevoEstado)
            refrescarDatos()
        }
    }

    fun obtenerUsuario(username: String, onResultado: (UsuarioEntity?) -> Unit) {
        viewModelScope.launch {
            onResultado(usuarioRepository.obtenerUsuarioPorUsername(username))
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
        onResultado: (exito: Boolean, mensaje: String, nuevoUsername: String?) -> Unit
    ) {
        viewModelScope.launch {
            val (exito, mensaje, updatedUser) = usuarioRepository.modificarPerfil(
                currentUsername = currentUsername,
                passwordActual = passwordActual,
                nuevoNombre = nuevoNombre,
                nuevoCorreo = nuevoCorreo,
                nuevoUsername = nuevoUsername
            )
            onResultado(exito, mensaje, updatedUser)
        }
    }

    fun cambiarContrasena(
        username: String,
        passActual: String,
        passNueva: String,
        onResultado: (exito: Boolean, mensaje: String) -> Unit
    ) {
        viewModelScope.launch {
            val (exito, mensaje) = usuarioRepository.cambiarContrasena(
                username = username,
                passActual = passActual,
                passNueva = passNueva
            )
            onResultado(exito, mensaje)
        }
    }
}