package com.rinconsur.probadores.data.repository

import com.rinconsur.probadores.data.local.entities.TurnoEntity
import com.rinconsur.probadores.data.remote.*

interface TurnoRepository {
    suspend fun obtenerTurnosPorEstado(estado: String = "EN_ESPERA"): List<TurnoEntity>
    suspend fun obtenerTurnoActivoCliente(nombreCliente: String): TurnoEntity?
    suspend fun registrarTurno(
        nombreCliente: String,
        cantidadPrendas: Int,
        esPreferente: Boolean
    ): Pair<Boolean, String>
    suspend fun actualizarEstadoTurno(turnoId: Long, nuevoEstado: String): String?
}

class TurnoRepositoryImpl(
    private val turnoApi: TurnoApi = RetrofitClient.turnoApi
) : TurnoRepository {

    override suspend fun obtenerTurnosPorEstado(estado: String): List<TurnoEntity> {
        return when (val res = llamarApi { turnoApi.listarTurnos(estado) }) {
            is ResultadoApi.Exito -> res.datos.map { it.aTurnoEntity() }
            is ResultadoApi.Error -> emptyList()
        }
    }

    override suspend fun obtenerTurnoActivoCliente(nombreCliente: String): TurnoEntity? {
        val lista = obtenerTurnosPorEstado("EN_ESPERA")
        return lista.find { it.nombreCliente.equals(nombreCliente.trim(), ignoreCase = true) }
    }

    override suspend fun registrarTurno(
        nombreCliente: String,
        cantidadPrendas: Int,
        esPreferente: Boolean
    ): Pair<Boolean, String> {
        val req = CrearTurnoRequest(
            nombreCliente = nombreCliente.trim(),
            cantidadPrendas = cantidadPrendas,
            esPreferente = esPreferente
        )
        return when (val res = llamarApi { turnoApi.crearTurno(req) }) {
            is ResultadoApi.Exito -> Pair(true, "¡Turno ${res.datos.numeroTurno} registrado con éxito!")
            is ResultadoApi.Error -> Pair(false, res.mensaje)
        }
    }

    override suspend fun actualizarEstadoTurno(turnoId: Long, nuevoEstado: String): String? {
        val req = CambiarEstadoTurnoRequest(estado = nuevoEstado)
        return when (val res = llamarApi { turnoApi.cambiarEstado(turnoId, req) }) {
            is ResultadoApi.Exito -> null
            is ResultadoApi.Error -> res.mensaje
        }
    }
}