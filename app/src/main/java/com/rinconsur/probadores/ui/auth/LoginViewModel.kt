package com.rinconsur.probadores.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rinconsur.probadores.data.local.AppDatabase
import com.rinconsur.probadores.data.local.entities.TurnoEntity
import com.rinconsur.probadores.data.repository.TurnoRepository
import com.rinconsur.probadores.data.repository.TurnoRepositoryImpl
import com.rinconsur.probadores.data.repository.UsuarioRepository
import com.rinconsur.probadores.data.repository.UsuarioRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel @JvmOverloads constructor(
    application: Application,
    private val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl(
        AppDatabase.getDatabase(application).usuarioDao()
    ),
    private val turnoRepository: TurnoRepository = TurnoRepositoryImpl(
        AppDatabase.getDatabase(application).turnoDao()
    )
) : AndroidViewModel(application) {

    val turnosEnEsperaLiveData: LiveData<List<TurnoEntity>> =
        turnoRepository.obtenerTurnosPorEstado("EN_ESPERA").asLiveData(Dispatchers.IO)

    fun validarNombreInvitado(nombre: String): String? {
        val clean = nombre.trim()
        return when {
            clean.isEmpty() -> "El nombre no puede estar vacío"
            clean.length < 3 -> "El nombre de invitado debe tener al menos 3 letras"
            else -> null
        }
    }

    fun validarInvitadoDisponible(nombre: String, onResultado: (String?) -> Unit) {
        viewModelScope.launch {
            val clean = nombre.trim()
            val basicError = validarNombreInvitado(clean)
            if (basicError != null) {
                onResultado(basicError)
                return@launch
            }

            val turnoActivo = turnoRepository.obtenerTurnoActivoCliente(clean)
            if (turnoActivo != null) {
                onResultado("Ya existe un cliente activo en la fila con el nombre '$clean'. Ingresa tu apellido o un nombre distinto.")
                return@launch
            }

            val usuarioBD = usuarioRepository.obtenerUsuarioPorUsername(clean)
            if (usuarioBD != null || clean.equals("admin", ignoreCase = true)) {
                onResultado("El nombre '$clean' pertenece a una cuenta registrada. Inicia sesión en la sección de empleados.")
                return@launch
            }

            onResultado(null)
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

    fun validarContrasenaLogin(password: String): String? {
        val clean = password.trim()
        return when {
            clean.isEmpty() -> "La contraseña es obligatoria"
            clean.length < 4 -> "La contraseña debe tener al menos 4 caracteres"
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

    fun autenticar(usernameInput: String, passInput: String, onResultado: (String?) -> Unit) {
        viewModelScope.launch {
            val rol = usuarioRepository.autenticar(usernameInput, passInput)
            onResultado(rol)
        }
    }

    fun recuperarContrasena(
        correo: String,
        nuevaContrasena: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val (exito, mensaje) = usuarioRepository.recuperarContrasena(correo, nuevaContrasena)
            onResultado(exito, mensaje)
        }
    }

    companion object {
        const val ROL_BLOQUEADO = UsuarioRepositoryImpl.ROL_BLOQUEADO
    }
}
