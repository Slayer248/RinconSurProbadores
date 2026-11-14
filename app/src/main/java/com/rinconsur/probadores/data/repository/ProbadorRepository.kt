package com.rinconsur.probadores.data.repository

import com.rinconsur.probadores.data.local.entities.ProbadorEntity
import com.rinconsur.probadores.data.remote.*

interface ProbadorRepository {
    suspend fun obtenerTodosProbadores(estado: String? = null): List<ProbadorEntity>
    suspend fun obtenerProbadorPorCodigo(codigo: String): ProbadorEntity?
    suspend fun cambiarEstadoProbador(probador: ProbadorEntity, nuevoEstado: String): String?
    suspend fun crearProbador(codigo: String, esAccesible: Boolean): String?
    suspend fun cambiarBloqueoProbador(probador: ProbadorEntity, bloquear: Boolean, motivo: String): String?
    suspend fun eliminarProbador(probador: ProbadorEntity): String?
}

class ProbadorRepositoryImpl(
    private val probadorApi: ProbadorApi = RetrofitClient.probadorApi
) : ProbadorRepository {

    override suspend fun obtenerTodosProbadores(estado: String?): List<ProbadorEntity> {
        return when (val res = llamarApi { probadorApi.listarProbadores(estado) }) {
            is ResultadoApi.Exito -> res.datos.map { it.aProbadorEntity() }
            is ResultadoApi.Error -> emptyList()
        }
    }

    override suspend fun obtenerProbadorPorCodigo(codigo: String): ProbadorEntity? {
        return when (val res = llamarApi { probadorApi.obtenerPorCodigo(codigo.trim().uppercase()) }) {
            is ResultadoApi.Exito -> res.datos.aProbadorEntity()
            is ResultadoApi.Error -> null
        }
    }

    override suspend fun cambiarEstadoProbador(probador: ProbadorEntity, nuevoEstado: String): String? {
        val req = CambiarEstadoProbadorRequest(estado = nuevoEstado)
        return when (val res = llamarApi { probadorApi.cambiarEstado(probador.codigo, req) }) {
            is ResultadoApi.Exito -> null
            is ResultadoApi.Error -> res.mensaje
        }
    }

    override suspend fun crearProbador(codigo: String, esAccesible: Boolean): String? {
        val req = CrearProbadorRequest(
            codigo = codigo.trim().uppercase(),
            esAccesible = esAccesible
        )
        return when (val res = llamarApi { probadorApi.crearProbador(req) }) {
            is ResultadoApi.Exito -> null
            is ResultadoApi.Error -> res.mensaje
        }
    }

    override suspend fun cambiarBloqueoProbador(
        probador: ProbadorEntity,
        bloquear: Boolean,
        motivo: String
    ): String? {
        val req = BloqueoProbadorRequest(
            bloqueado = bloquear,
            motivo = motivo.trim().ifEmpty { null }
        )
        return when (val res = llamarApi { probadorApi.cambiarBloqueo(probador.codigo, req) }) {
            is ResultadoApi.Exito -> null
            is ResultadoApi.Error -> res.mensaje
        }
    }

    override suspend fun eliminarProbador(probador: ProbadorEntity): String? {
        return when (val res = llamarApi { probadorApi.eliminarProbador(probador.codigo) }) {
            is ResultadoApi.Exito -> null
            is ResultadoApi.Error -> res.mensaje
        }
    }

    companion object {
        const val ESTADO_FUERA_SERVICIO = "FUERA_SERVICIO"
    }
}