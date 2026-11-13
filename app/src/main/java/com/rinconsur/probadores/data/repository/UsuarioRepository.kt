package com.rinconsur.probadores.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.rinconsur.probadores.data.local.dao.UsuarioDao
import com.rinconsur.probadores.data.local.entities.UsuarioEntity
import com.rinconsur.probadores.utils.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface UsuarioRepository {
    fun obtenerTodosUsuarios(): Flow<List<UsuarioEntity>>
    suspend fun obtenerUsuarioPorUsername(username: String): UsuarioEntity?
    suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity?
    suspend fun autenticar(usernameInput: String, passInput: String): String?
    suspend fun crearUsuario(
        nombre: String,
        correo: String,
        username: String,
        pass: String,
        rol: String
    ): String?
    suspend fun recuperarContrasena(
        correo: String,
        nuevaContrasena: String
    ): Pair<Boolean, String>
    suspend fun modificarPerfil(
        currentUsername: String,
        passwordActual: String,
        nuevoNombre: String,
        nuevoCorreo: String,
        nuevoUsername: String,
        nuevaPassword: String
    ): Triple<Boolean, String, String?>
    suspend fun editarCuenta(
        username: String,
        nuevoNombre: String,
        nuevoCorreo: String,
        nuevoRol: String,
        nuevaPassword: String
    ): String?
    suspend fun eliminarCuenta(username: String, adminActual: String): String?
    suspend fun cambiarBloqueoCuenta(
        username: String,
        bloquear: Boolean,
        adminActual: String
    ): String?
}

class UsuarioRepositoryImpl(
    private val usuarioDao: UsuarioDao
) : UsuarioRepository {

    override fun obtenerTodosUsuarios(): Flow<List<UsuarioEntity>> =
        usuarioDao.obtenerTodosUsuarios()

    override suspend fun obtenerUsuarioPorUsername(username: String): UsuarioEntity? =
        withContext(Dispatchers.IO) {
            usuarioDao.obtenerUsuarioPorUsername(username.trim())
        }

    override suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity? =
        withContext(Dispatchers.IO) {
            usuarioDao.obtenerUsuarioPorCorreo(correo.trim())
        }

    override suspend fun autenticar(usernameInput: String, passInput: String): String? =
        withContext(Dispatchers.IO) {
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

    override suspend fun crearUsuario(
        nombre: String,
        correo: String,
        username: String,
        pass: String,
        rol: String
    ): String? = withContext(Dispatchers.IO) {
        val cleanUser = username.trim()
        val cleanCorreo = correo.trim()

        val usuarioPorUsername = usuarioDao.obtenerUsuarioPorUsername(cleanUser)
        if (usuarioPorUsername != null || cleanUser.equals("admin", ignoreCase = true)) {
            return@withContext "El nombre de usuario '$cleanUser' ya se encuentra registrado"
        }

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

    override suspend fun recuperarContrasena(
        correo: String,
        nuevaContrasena: String
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
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

    override suspend fun modificarPerfil(
        currentUsername: String,
        passwordActual: String,
        nuevoNombre: String,
        nuevoCorreo: String,
        nuevoUsername: String,
        nuevaPassword: String
    ): Triple<Boolean, String, String?> = withContext(Dispatchers.IO) {
        val oldUserClean = currentUsername.trim()
        val passActualClean = passwordActual.trim()
        val nomClean = nuevoNombre.trim()
        val correoClean = nuevoCorreo.trim()
        val userClean = nuevoUsername.trim()
        val passNuevaClean = nuevaPassword.trim()

        val usuarioBD = usuarioDao.obtenerUsuarioPorUsername(oldUserClean)

        val esAdminMaestro = oldUserClean.equals("admin", ignoreCase = true) &&
                (passActualClean == "admin1234" || passActualClean == "admin")

        val contrasenaCorrecta = esAdminMaestro || (usuarioBD != null && PasswordHasher.verifyPassword(passActualClean, usuarioBD.contrasena))

        if (!contrasenaCorrecta) {
            return@withContext Triple(false, "La contraseña actual es incorrecta", null)
        }

        if (!userClean.equals(oldUserClean, ignoreCase = true)) {
            val otroUsername = usuarioDao.obtenerUsuarioPorUsername(userClean)
            if (otroUsername != null || userClean.equals("admin", ignoreCase = true)) {
                return@withContext Triple(false, "El nombre de usuario '$userClean' ya se encuentra registrado por otra cuenta", null)
            }
        }

        val correoAnterior = usuarioBD?.correo ?: ""
        if (!correoClean.equals(correoAnterior, ignoreCase = true)) {
            val otroCorreo = usuarioDao.obtenerUsuarioPorCorreo(correoClean)
            if (otroCorreo != null) {
                return@withContext Triple(false, "El correo electrónico '$correoClean' ya se encuentra registrado por otra cuenta", null)
            }
        }

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

        when {
            usuarioBD == null -> usuarioDao.insertarUsuario(usuarioModificado)
            usuarioBD.username == userClean -> usuarioDao.actualizarUsuario(usuarioModificado)
            else -> usuarioDao.reemplazarUsername(usuarioBD.username, usuarioModificado)
        }

        Triple(true, "Perfil actualizado con éxito", userClean)
    }

    override suspend fun editarCuenta(
        username: String,
        nuevoNombre: String,
        nuevoCorreo: String,
        nuevoRol: String,
        nuevaPassword: String
    ): String? = withContext(Dispatchers.IO) {
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

    override suspend fun eliminarCuenta(username: String, adminActual: String): String? =
        withContext(Dispatchers.IO) {
            if (username.equals(adminActual, ignoreCase = true)) {
                return@withContext "No puedes eliminar tu propia cuenta"
            }
            usuarioDao.eliminarUsuarioPorUsername(username)
            null
        }

    override suspend fun cambiarBloqueoCuenta(
        username: String,
        bloquear: Boolean,
        adminActual: String
    ): String? = withContext(Dispatchers.IO) {
        if (username.equals(adminActual, ignoreCase = true)) {
            return@withContext "No puedes bloquear tu propia cuenta"
        }
        usuarioDao.cambiarBloqueo(username, bloquear)
        null
    }

    companion object {
        const val ROL_BLOQUEADO = "BLOQUEADO"
    }
}
