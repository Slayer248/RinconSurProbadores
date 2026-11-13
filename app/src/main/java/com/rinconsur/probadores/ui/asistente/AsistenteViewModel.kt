package com.rinconsur.probadores.ui.asistente

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
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

    // ---------------------------------------------------------------------
    // Lógica de Validación de Entradas
    // ---------------------------------------------------------------------

    fun validarNombreInvitado(nombre: String): String? {
        val clean = nombre.trim()
        return when {
            clean.isEmpty() -> "El nombre no puede estar vacío"
            clean.length < 3 -> "El nombre de invitado debe tener al menos 3 letras"
            else -> null
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

    fun validarCantidadPrendas(prendasText: String): String? {
        val clean = prendasText.trim()
        val prendas = clean.toIntOrNull()
        return when {
            clean.isEmpty() -> "Por favor ingresa la cantidad de prendas"
            prendas == null || prendas < 1 || prendas > 6 -> "La cantidad de prendas debe ser entre 1 y 6"
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

    fun registrarTurno(
        nombreCliente: String,
        cantidadPrendas: Int,
        esPreferente: Boolean,
        onResultado: (exito: Boolean, mensaje: String) -> Unit
    ) {
        viewModelScope.launch {
            val (exito, mensaje) = withContext(Dispatchers.IO) {
                val cleanNombre = nombreCliente.trim()
                val turnoActivo = turnoDao.obtenerTurnoActivoCliente(cleanNombre)
                if (turnoActivo != null) {
                    Pair(
                        false,
                        "Ya tienes un turno activo en espera (${turnoActivo.numeroTurno}). Debes ser atendido antes de solicitar otro."
                    )
                } else {
                    val ultimoNumero = turnoDao.obtenerUltimoNumero() ?: 1000
                    val nuevoNumeroTurno = "T-${ultimoNumero + 1}"

                    val nuevoTurno = TurnoEntity(
                        numeroTurno = nuevoNumeroTurno,
                        nombreCliente = cleanNombre,
                        cantidadPrendas = cantidadPrendas,
                        esPreferente = esPreferente,
                        estado = "EN_ESPERA"
                    )
                    turnoDao.insertarTurno(nuevoTurno)
                    Pair(true, "¡Turno $nuevoNumeroTurno registrado con éxito!")
                }
            }
            onResultado(exito, mensaje)
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
                    if (usuarioBD.bloqueado) {
                        return@withContext ROL_BLOQUEADO
                    }
                    // Migrar a hash SHA-256 si la clave estaba en texto plano heredado
                    if (usuarioBD.contrasena == cleanPass) {
                        usuarioDao.actualizarUsuario(usuarioBD.copy(contrasena = PasswordHasher.hashPassword(cleanPass)))
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
                try {
                    usuarioDao.insertarUsuario(nuevoUsuario)
                    null
                } catch (_: SQLiteConstraintException) {
                    "El nombre de usuario '$cleanUser' ya se encuentra registrado"
                }
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
                    usuarioDao.actualizarUsuario(usuarioActualizado)
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

                val usuarioModificado = UsuarioEntity(
                    username = userClean,
                    nombre = nomClean,
                    correo = correoClean,
                    contrasena = finalPassword,
                    rol = rolFinal,
                    bloqueado = usuarioBD?.bloqueado ?: false
                )

                // 5. Guardar: update si es la misma clave, reemplazo atómico si cambió el username
                when {
                    usuarioBD == null -> usuarioDao.insertarUsuario(usuarioModificado)
                    usuarioBD.username == userClean -> usuarioDao.actualizarUsuario(usuarioModificado)
                    else -> usuarioDao.reemplazarUsername(usuarioBD.username, usuarioModificado)
                }

                Triple(true, "Perfil actualizado con éxito", userClean)
            }
            onResultado(exito, mensaje, updatedUser)
        }
    }

    // ---------------------------------------------------------------------
    // Administración de cuentas
    // ---------------------------------------------------------------------

    /** Edita los datos de otra cuenta. El username es la clave y no se modifica aquí. */
    fun editarCuenta(
        username: String,
        nuevoNombre: String,
        nuevoCorreo: String,
        nuevoRol: String,
        nuevaPassword: String,
        onResultado: (String?) -> Unit
    ) {
        viewModelScope.launch {
            val error = withContext(Dispatchers.IO) {
                val usuarioBD = usuarioDao.obtenerUsuarioPorUsername(username.trim())
                    ?: return@withContext "La cuenta '$username' ya no existe"

                val correoClean = nuevoCorreo.trim()
                val otroCorreo = usuarioDao.obtenerUsuarioPorCorreo(correoClean)
                if (otroCorreo != null && otroCorreo.username != usuarioBD.username) {
                    return@withContext "El correo electrónico '$correoClean' ya se encuentra registrado por otra cuenta"
                }

                val passClean = nuevaPassword.trim()
                usuarioDao.actualizarUsuario(
                    usuarioBD.copy(
                        nombre = nuevoNombre.trim(),
                        correo = correoClean,
                        rol = nuevoRol,
                        contrasena = if (passClean.isNotEmpty()) PasswordHasher.hashPassword(passClean) else usuarioBD.contrasena
                    )
                )
                null
            }
            onResultado(error)
        }
    }

    fun eliminarCuenta(username: String, adminActual: String, onResultado: (String?) -> Unit) {
        viewModelScope.launch {
            val error = withContext(Dispatchers.IO) {
                if (username.equals(adminActual, ignoreCase = true)) {
                    return@withContext "No puedes eliminar tu propia cuenta"
                }
                usuarioDao.eliminarUsuarioPorUsername(username)
                null
            }
            onResultado(error)
        }
    }

    fun cambiarBloqueoCuenta(username: String, bloquear: Boolean, adminActual: String, onResultado: (String?) -> Unit) {
        viewModelScope.launch {
            val error = withContext(Dispatchers.IO) {
                if (username.equals(adminActual, ignoreCase = true)) {
                    return@withContext "No puedes bloquear tu propia cuenta"
                }
                usuarioDao.cambiarBloqueo(username, bloquear)
                null
            }
            onResultado(error)
        }
    }

    // ---------------------------------------------------------------------
    // Administración de probadores
    // ---------------------------------------------------------------------

    fun crearProbador(codigo: String, esAccesible: Boolean, onResultado: (String?) -> Unit) {
        viewModelScope.launch {
            val error = withContext(Dispatchers.IO) {
                val codigoClean = codigo.trim().uppercase()
                if (probadorDao.obtenerProbadorPorCodigo(codigoClean) != null) {
                    return@withContext "Ya existe un probador con el código '$codigoClean'"
                }
                try {
                    probadorDao.insertarProbador(
                        ProbadorEntity(
                            codigo = codigoClean,
                            esAccesible = esAccesible,
                            estado = "LIBRE",
                            desdeHora = "-"
                        )
                    )
                    null
                } catch (e: SQLiteConstraintException) {
                    "Ya existe un probador con el código '$codigoClean'"
                }
            }
            onResultado(error)
        }
    }

    /** Bloquear deja el probador FUERA_SERVICIO (no se puede usar); desbloquear lo devuelve a LIBRE. */
    fun cambiarBloqueoProbador(
        probador: ProbadorEntity,
        bloquear: Boolean,
        motivo: String,
        onResultado: (String?) -> Unit
    ) {
        viewModelScope.launch {
            val error = withContext(Dispatchers.IO) {
                if (bloquear && probador.estado == "OCUPADO") {
                    return@withContext "No se puede bloquear ${probador.codigo} mientras está ocupado"
                }
                val actualizado = if (bloquear) {
                    probador.copy(
                        estado = ESTADO_FUERA_SERVICIO,
                        motivoFueraServicio = motivo.trim().ifEmpty { null }
                    )
                } else {
                    probador.copy(estado = "LIBRE", desdeHora = "-", motivoFueraServicio = null)
                }
                probadorDao.actualizarProbador(actualizado)
                null
            }
            onResultado(error)
        }
    }

    fun eliminarProbador(probador: ProbadorEntity, onResultado: (String?) -> Unit) {
        viewModelScope.launch {
            val error = withContext(Dispatchers.IO) {
                if (probador.estado == "OCUPADO") {
                    return@withContext "No se puede eliminar ${probador.codigo} mientras está ocupado"
                }
                probadorDao.eliminarProbador(probador)
                null
            }
            onResultado(error)
        }
    }

    companion object {
        const val ROL_BLOQUEADO = "BLOQUEADO"
        const val ESTADO_FUERA_SERVICIO = "FUERA_SERVICIO"
    }
}
