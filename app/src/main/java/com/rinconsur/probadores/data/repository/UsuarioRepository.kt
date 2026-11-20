package com.rinconsur.probadores.data.repository

import com.rinconsur.probadores.data.local.entities.UsuarioEntity
import com.rinconsur.probadores.data.remote.*

interface UsuarioRepository {
    suspend fun obtenerTodosUsuarios(): List<UsuarioEntity>
    suspend fun obtenerRoles(): List<RolDto>
    suspend fun obtenerUsuarioPorUsername(username: String): UsuarioEntity?
    suspend fun autenticar(usernameInput: String, passInput: String): ResultadoLogin
    suspend fun crearUsuario(
        nombre: String,
        correo: String,
        username: String,
        pass: String = "",
        rol: String
    ): String?
    suspend fun modificarPerfil(
        currentUsername: String,
        passwordActual: String,
        nuevoNombre: String,
        nuevoCorreo: String,
        nuevoUsername: String
    ): Triple<Boolean, String, String?>
    suspend fun cambiarContrasena(
        username: String,
        passActual: String,
        passNueva: String
    ): Pair<Boolean, String>
    suspend fun recuperarContrasena(
        username: String
    ): Pair<Boolean, String>
    suspend fun editarCuenta(
        username: String,
        nuevoNombre: String,
        nuevoCorreo: String,
        nuevoRol: String
    ): String?
    suspend fun eliminarCuenta(username: String, adminIdActual: Long?): String?
    suspend fun cambiarBloqueoCuenta(
        username: String,
        bloquear: Boolean,
        adminIdActual: Long?
    ): String?
}

sealed interface ResultadoLogin {
    data class Exito(val usuario: UsuarioResponse) : ResultadoLogin
    data class Error(val mensaje: String) : ResultadoLogin
}

class UsuarioRepositoryImpl(
    private val usuarioApi: UsuarioApi = RetrofitClient.usuarioApi,
    private val rolApi: RolApi = RetrofitClient.rolApi
) : UsuarioRepository {

    override suspend fun obtenerTodosUsuarios(): List<UsuarioEntity> {
        return when (val res = llamarApi { usuarioApi.listarUsuarios() }) {
            is ResultadoApi.Exito -> res.datos.map { it.aUsuarioEntity() }
            is ResultadoApi.Error -> emptyList()
        }
    }

    override suspend fun obtenerRoles(): List<RolDto> {
        return when (val res = llamarApi { rolApi.listarRoles() }) {
            is ResultadoApi.Exito -> res.datos
            is ResultadoApi.Error -> listOf(
                RolDto(1L, "ADMIN"),
                RolDto(2L, "ASISTENTE")
            )
        }
    }

    override suspend fun obtenerUsuarioPorUsername(username: String): UsuarioEntity? {
        return when (val res = llamarApi { usuarioApi.obtenerPorUsername(username.trim()) }) {
            is ResultadoApi.Exito -> res.datos.aUsuarioEntity()
            is ResultadoApi.Error -> null
        }
    }

    override suspend fun autenticar(usernameInput: String, passInput: String): ResultadoLogin {
        val req = LoginRequest(username = usernameInput.trim(), contrasena = passInput.trim())
        return when (val res = llamarApi { usuarioApi.login(req) }) {
            is ResultadoApi.Exito -> ResultadoLogin.Exito(res.datos)
            is ResultadoApi.Error -> ResultadoLogin.Error(res.mensaje)
        }
    }

    override suspend fun crearUsuario(
        nombre: String,
        correo: String,
        username: String,
        pass: String,
        rol: String
    ): String? {
        val rolId = resolverRolId(rol)
        val req = RegistroUsuarioRequest(
            username = username.trim(),
            nombre = nombre.trim(),
            correo = correo.trim(),
            contrasena = pass.trim().ifEmpty { null },
            rolId = rolId
        )
        return when (val res = llamarApi { usuarioApi.crearUsuario(req) }) {
            is ResultadoApi.Exito -> null
            is ResultadoApi.Error -> res.mensaje
        }
    }

    override suspend fun modificarPerfil(
        currentUsername: String,
        passwordActual: String,
        nuevoNombre: String,
        nuevoCorreo: String,
        nuevoUsername: String
    ): Triple<Boolean, String, String?> {
        // Validar contraseña actual autenticando con la API
        val authRes = autenticar(currentUsername.trim(), passwordActual.trim())
        if (authRes is ResultadoLogin.Error) {
            return Triple(false, "La contraseña actual es incorrecta", null)
        }

        val actual = when (val res = llamarApi { usuarioApi.obtenerPorUsername(currentUsername.trim()) }) {
            is ResultadoApi.Exito -> res.datos
            is ResultadoApi.Error -> return Triple(false, res.mensaje, null)
        }

        val req = ActualizarUsuarioRequest(
            username = nuevoUsername.trim(),
            nombre = nuevoNombre.trim(),
            correo = nuevoCorreo.trim(),
            rolId = actual.rolId,
            nuevaContrasena = null
        )

        val updateRes = llamarApi { usuarioApi.actualizarUsuario(actual.id, req) }
        if (updateRes is ResultadoApi.Error) {
            return Triple(false, updateRes.mensaje, null)
        }

        return Triple(true, "Perfil actualizado con éxito", nuevoUsername.trim())
    }

    override suspend fun cambiarContrasena(
        username: String,
        passActual: String,
        passNueva: String
    ): Pair<Boolean, String> {
        val actual = when (val res = llamarApi { usuarioApi.obtenerPorUsername(username.trim()) }) {
            is ResultadoApi.Exito -> res.datos
            is ResultadoApi.Error -> return Pair(false, res.mensaje)
        }

        val passReq = CambiarContrasenaRequest(
            actual = passActual.trim(),
            nueva = passNueva.trim()
        )
        return when (val passRes = llamarApi { usuarioApi.cambiarContrasena(actual.id, passReq) }) {
            is ResultadoApi.Exito -> Pair(true, "Contraseña modificada exitosamente")
            is ResultadoApi.Error -> Pair(false, passRes.mensaje)
        }
    }

    override suspend fun recuperarContrasena(username: String): Pair<Boolean, String> {
        val req = RecuperarContrasenaRequest(username = username.trim())
        return when (val res = llamarApi { usuarioApi.recuperarContrasena(req) }) {
            is ResultadoApi.Exito -> Pair(
                true,
                "Se ha enviado una contraseña temporal a tu correo. Deberás cambiarla al iniciar sesión."
            )
            is ResultadoApi.Error -> Pair(false, res.mensaje)
        }
    }

    override suspend fun editarCuenta(
        username: String,
        nuevoNombre: String,
        nuevoCorreo: String,
        nuevoRol: String
    ): String? {
        val actual = when (val res = llamarApi { usuarioApi.obtenerPorUsername(username.trim()) }) {
            is ResultadoApi.Exito -> res.datos
            is ResultadoApi.Error -> return res.mensaje
        }

        val rolId = resolverRolId(nuevoRol)
        val req = ActualizarUsuarioRequest(
            username = actual.username,
            nombre = nuevoNombre.trim(),
            correo = nuevoCorreo.trim(),
            rolId = rolId,
            // El administrador no puede cambiar la contraseña de otra cuenta
            nuevaContrasena = null
        )

        return when (val res = llamarApi { usuarioApi.actualizarUsuario(actual.id, req) }) {
            is ResultadoApi.Exito -> null
            is ResultadoApi.Error -> res.mensaje
        }
    }

    override suspend fun eliminarCuenta(username: String, adminIdActual: Long?): String? {
        val usuario = when (val res = llamarApi { usuarioApi.obtenerPorUsername(username.trim()) }) {
            is ResultadoApi.Exito -> res.datos
            is ResultadoApi.Error -> return res.mensaje
        }
        return when (val res = llamarApi { usuarioApi.eliminarUsuario(usuario.id, adminIdActual) }) {
            is ResultadoApi.Exito -> null
            is ResultadoApi.Error -> res.mensaje
        }
    }

    override suspend fun cambiarBloqueoCuenta(
        username: String,
        bloquear: Boolean,
        adminIdActual: Long?
    ): String? {
        val usuario = when (val res = llamarApi { usuarioApi.obtenerPorUsername(username.trim()) }) {
            is ResultadoApi.Exito -> res.datos
            is ResultadoApi.Error -> return res.mensaje
        }
        return when (val res = llamarApi {
            usuarioApi.cambiarBloqueo(usuario.id, BloqueoRequest(bloquear), adminIdActual)
        }) {
            is ResultadoApi.Exito -> null
            is ResultadoApi.Error -> res.mensaje
        }
    }

    private suspend fun resolverRolId(rolNombre: String): Long {
        val roles = obtenerRoles()
        return roles.find { it.nombre.equals(rolNombre.trim(), ignoreCase = true) }?.id
            ?: if (rolNombre.equals("ADMIN", ignoreCase = true)) 1L else 2L
    }
}