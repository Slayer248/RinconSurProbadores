package com.rinconsur.probadores.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rinconsur.probadores.data.local.entities.ProbadorEntity
import com.rinconsur.probadores.data.local.entities.UsuarioEntity
import com.rinconsur.probadores.data.repository.*
import com.rinconsur.probadores.utils.SessionManager
import kotlinx.coroutines.launch

class AdminViewModel @JvmOverloads constructor(
    application: Application,
    private val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl(),
    private val probadorRepository: ProbadorRepository = ProbadorRepositoryImpl()
) : AndroidViewModel(application) {

    private val _usuarios = MutableLiveData<List<UsuarioEntity>>(emptyList())
    val usuariosLiveData: LiveData<List<UsuarioEntity>> = _usuarios

    private val _probadores = MutableLiveData<List<ProbadorEntity>>(emptyList())
    val probadoresLiveData: LiveData<List<ProbadorEntity>> = _probadores

    init {
        refrescarTodo()
    }

    fun refrescarTodo() {
        viewModelScope.launch {
            _usuarios.value = usuarioRepository.obtenerTodosUsuarios()
            _probadores.value = probadorRepository.obtenerTodosProbadores()
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
            if (exito) refrescarTodo()
            onResultado(exito, mensaje, updatedUser)
        }
    }

    fun crearUsuario(
        nombre: String,
        correo: String,
        username: String,
        pass: String,
        rol: String,
        onResultado: (String?) -> Unit
    ) {
        viewModelScope.launch {
            val error = usuarioRepository.crearUsuario(nombre, correo, username, pass, rol)
            if (error == null) refrescarTodo()
            onResultado(error)
        }
    }

    fun editarCuenta(
        username: String,
        nuevoNombre: String,
        nuevoCorreo: String,
        nuevoRol: String,
        nuevaPassword: String,
        onResultado: (String?) -> Unit
    ) {
        viewModelScope.launch {
            val error = usuarioRepository.editarCuenta(
                username, nuevoNombre, nuevoCorreo, nuevoRol, nuevaPassword
            )
            if (error == null) refrescarTodo()
            onResultado(error)
        }
    }

    fun eliminarCuenta(username: String, @Suppress("UNUSED_PARAMETER") adminActual: String, onResultado: (String?) -> Unit) {
        viewModelScope.launch {
            val adminId = SessionManager.obtenerEmpleadoId(getApplication()).takeIf { it > 0 }
            val error = usuarioRepository.eliminarCuenta(username, adminId)
            if (error == null) refrescarTodo()
            onResultado(error)
        }
    }

    fun cambiarBloqueoCuenta(username: String, bloquear: Boolean, @Suppress("UNUSED_PARAMETER") adminActual: String, onResultado: (String?) -> Unit) {
        viewModelScope.launch {
            val adminId = SessionManager.obtenerEmpleadoId(getApplication()).takeIf { it > 0 }
            val error = usuarioRepository.cambiarBloqueoCuenta(username, bloquear, adminId)
            if (error == null) refrescarTodo()
            onResultado(error)
        }
    }

    fun crearProbador(codigo: String, esAccesible: Boolean, onResultado: (String?) -> Unit) {
        viewModelScope.launch {
            val error = probadorRepository.crearProbador(codigo, esAccesible)
            if (error == null) refrescarTodo()
            onResultado(error)
        }
    }

    fun cambiarBloqueoProbador(
        probador: ProbadorEntity,
        bloquear: Boolean,
        motivo: String,
        onResultado: (String?) -> Unit
    ) {
        viewModelScope.launch {
            val error = probadorRepository.cambiarBloqueoProbador(probador, bloquear, motivo)
            if (error == null) refrescarTodo()
            onResultado(error)
        }
    }

    fun eliminarProbador(probador: ProbadorEntity, onResultado: (String?) -> Unit) {
        viewModelScope.launch {
            val error = probadorRepository.eliminarProbador(probador)
            if (error == null) refrescarTodo()
            onResultado(error)
        }
    }

    companion object {
        const val ESTADO_FUERA_SERVICIO = ProbadorRepositoryImpl.ESTADO_FUERA_SERVICIO
    }
}