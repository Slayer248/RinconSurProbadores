package com.rinconsur.probadores.data.remote

import com.rinconsur.probadores.data.local.entities.ProbadorEntity
import com.rinconsur.probadores.data.local.entities.TurnoEntity
import com.rinconsur.probadores.data.local.entities.UsuarioEntity

// --- ROLES ---
data class RolDto(
    val id: Long,
    val nombre: String
)

// --- USUARIOS ---
data class LoginRequest(
    val username: String,
    val contrasena: String
)

data class RegistroUsuarioRequest(
    val username: String,
    val nombre: String,
    val correo: String,
    val contrasena: String? = null,
    val rolId: Long
)

data class ActualizarUsuarioRequest(
    val username: String,
    val nombre: String,
    val correo: String,
    val rolId: Long,
    val nuevaContrasena: String? = null
)

data class CambiarContrasenaRequest(
    val actual: String,
    val nueva: String
)

data class RecuperarContrasenaRequest(
    val username: String
)

data class BloqueoRequest(
    val bloqueado: Boolean
)

data class UsuarioResponse(
    val id: Long,
    val username: String,
    val nombre: String,
    val correo: String,
    val rolId: Long,
    val rolNombre: String,
    val bloqueado: Boolean,
    val debeCambiarContrasena: Boolean = false
) {
    fun aUsuarioEntity(): UsuarioEntity = UsuarioEntity(
        username = username,
        nombre = nombre,
        correo = correo,
        contrasena = "", // El hash nunca viaja al cliente
        rol = rolNombre,
        bloqueado = bloqueado
    )
}

// --- PROBADORES ---
data class CrearProbadorRequest(
    val codigo: String,
    val esAccesible: Boolean
)

data class CambiarEstadoProbadorRequest(
    val estado: String
)

data class BloqueoProbadorRequest(
    val bloqueado: Boolean,
    val motivo: String? = null
)

data class ProbadorResponse(
    val codigo: String,
    val esAccesible: Boolean,
    val estado: String,
    val desdeHora: String,
    val motivoFueraServicio: String?,
    val ultimoCliente: String? = null,
    val ultimoTurno: String? = null,
    val ultimoUso: String? = null
) {
    fun aProbadorEntity(): ProbadorEntity = ProbadorEntity(
        codigo = codigo,
        esAccesible = esAccesible,
        estado = estado,
        desdeHora = desdeHora,
        motivoFueraServicio = motivoFueraServicio,
        ultimoCliente = ultimoCliente,
        ultimoTurno = ultimoTurno,
        ultimoUso = ultimoUso
    )
}

// --- TURNOS ---
data class CrearTurnoRequest(
    val nombreCliente: String,
    val cantidadPrendas: Int,
    val esPreferente: Boolean
)

data class CambiarEstadoTurnoRequest(
    val estado: String
)

data class TurnoResponse(
    val id: Long,
    val numeroTurno: String,
    val nombreCliente: String,
    val cantidadPrendas: Int,
    val esPreferente: Boolean,
    val estado: String
) {
    fun aTurnoEntity(): TurnoEntity = TurnoEntity(
        id = id,
        numeroTurno = numeroTurno,
        nombreCliente = nombreCliente,
        cantidadPrendas = cantidadPrendas,
        esPreferente = esPreferente,
        estado = estado
    )
}