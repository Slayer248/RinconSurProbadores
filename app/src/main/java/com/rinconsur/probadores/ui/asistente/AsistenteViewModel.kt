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
import com.rinconsur.probadores.utils.PasswordHasher
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
                if (usuarioBD != null && PasswordHasher.verifyPassword(cleanPass, usuarioBD.contrasena)) {
                    // Migrar a hash SHA-256 si la clave estaba en texto plano heredado
                    if (usuarioBD.contrasena == cleanPass) {
                        usuarioDao.insertarUsuario(usuarioBD.copy(contrasena = PasswordHasher.hashPassword(cleanPass)))
                    }
                    usuarioBD.rol
                } else {
                    null
                }
            }
            onResultado(rol)
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
            val errorMsg: String? = withContext(Dispatchers.IO) {
                val cleanUser = username.trim()
                val cleanCorreo = correo.trim()

                // Validar que el username no exista ya
                val usuarioPorUsername = usuarioDao.obtenerUsuarioPorUsername(cleanUser)
                if (usuarioPorUsername != null || cleanUser.equals("admin", ignoreCase = true)) {
                    return@withContext "El nombre de usuario '$cleanUser' ya se encuentra registrado"
                }

                // Validar que el correo no esté registrado previamente
                val usuarioPorCorreo = usuarioDao.obtenerUsuarioPorCorreo(cleanCorreo)
                if (usuarioPorCorreo != null) {
                    return@withContext "El correo electrónico '$cleanCorreo' ya se encuentra registrado"
                }

                val nuevoUsuario = UsuarioEntity(
                    username = cleanUser,
                    nombre = nombre.trim(),
                    correo = cleanCorreo,
                    contrasena = PasswordHasher.hashPassword(pass.trim()),
                    rol = rol
                )
                usuarioDao.insertarUsuario(nuevoUsuario)
                null
            }
            onResultado(errorMsg)
        }
    }

    fun recuperarContrasena(
        correo: String,
        nuevaContrasena: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val (exito, mensaje) = withContext(Dispatchers.IO) {
                val cleanCorreo = correo.trim()
                val cleanPass = nuevaContrasena.trim()

                val usuarioBD = usuarioDao.obtenerUsuarioPorCorreo(cleanCorreo)
                if (usuarioBD == null) {
                    Pair(false, "No se encontró ninguna cuenta registrada con el correo: $cleanCorreo")
                } else {
                    val usuarioActualizado = usuarioBD.copy(contrasena = PasswordHasher.hashPassword(cleanPass))
                    usuarioDao.insertarUsuario(usuarioActualizado)
                    Pair(true, "Contraseña actualizada exitosamente para la cuenta de ${usuarioBD.nombre} (${usuarioBD.username})")
                }
            }
            onResultado(exito, mensaje)
        }
    }

    fun obtenerUsuario(username: String, onResultado: (UsuarioEntity?) -> Unit) {
        viewModelScope.launch {
            val usuario = withContext(Dispatchers.IO) {
                usuarioDao.obtenerUsuarioPorUsername(username.trim())
            }
            onResultado(usuario)
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
            val (exito, mensaje, updatedUser) = withContext(Dispatchers.IO) {
                val oldUserClean = currentUsername.trim()
                val passActualClean = passwordActual.trim()
                val nomClean = nuevoNombre.trim()
                val correoClean = nuevoCorreo.trim()
                val userClean = nuevoUsername.trim()
                val passNuevaClean = nuevaPassword.trim()

                // 1. Obtener usuario actual de la BD o verificar admin maestro
                val usuarioBD = usuarioDao.obtenerUsuarioPorUsername(oldUserClean)

                val esAdminMaestro = oldUserClean.equals("admin", ignoreCase = true) &&
                        (passActualClean == "admin1234" || passActualClean == "admin")

                val contrasenaCorrecta = esAdminMaestro || (usuarioBD != null && PasswordHasher.verifyPassword(passActualClean, usuarioBD.contrasena))

                if (!contrasenaCorrecta) {
                    return@withContext Triple(false, "La contraseña actual es incorrecta", null)
                }

                // 2. Validar que el nuevo username no esté repetido por OTRA cuenta
                if (!userClean.equals(oldUserClean, ignoreCase = true)) {
                    val otroUsername = usuarioDao.obtenerUsuarioPorUsername(userClean)
                    if (otroUsername != null || userClean.equals("admin", ignoreCase = true)) {
                        return@withContext Triple(false, "El nombre de usuario '$userClean' ya se encuentra registrado por otra cuenta", null)
                    }
                }

                // 3. Validar que el nuevo correo no esté repetido por OTRA cuenta
                val correoAnterior = usuarioBD?.correo ?: ""
                if (!correoClean.equals(correoAnterior, ignoreCase = true)) {
                    val otroCorreo = usuarioDao.obtenerUsuarioPorCorreo(correoClean)
                    if (otroCorreo != null) {
                        return@withContext Triple(false, "El correo electrónico '$correoClean' ya se encuentra registrado por otra cuenta", null)
                    }
                }

                // 4. Determinar la contraseña final (se guarda hasheada)
                val finalPassword = if (passNuevaClean.isNotEmpty()) {
                    PasswordHasher.hashPassword(passNuevaClean)
                } else {
                    usuarioBD?.contrasena ?: PasswordHasher.hashPassword(passActualClean)
                }
                val rolFinal = usuarioBD?.rol ?: "ASISTENTE"

                // 5. Si cambió el username, eliminamos el registro previo para actualizar la clave primaria
                if (!userClean.equals(oldUserClean, ignoreCase = true)) {
                    usuarioDao.eliminarUsuarioPorUsername(oldUserClean)
                }

                val usuarioModificado = UsuarioEntity(
                    username = userClean,
                    nombre = nomClean,
                    correo = correoClean,
                    contrasena = finalPassword,
                    rol = rolFinal
                )

                usuarioDao.insertarUsuario(usuarioModificado)

                Triple(true, "Perfil actualizado con éxito", userClean)
            }
            onResultado(exito, mensaje, updatedUser)
        }
    }
}