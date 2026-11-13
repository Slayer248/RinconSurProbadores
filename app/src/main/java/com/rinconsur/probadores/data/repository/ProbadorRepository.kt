package com.rinconsur.probadores.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.rinconsur.probadores.data.local.dao.ProbadorDao
import com.rinconsur.probadores.data.local.dao.TurnoDao
import com.rinconsur.probadores.data.local.entities.ProbadorEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface ProbadorRepository {
    fun obtenerTodosProbadores(): Flow<List<ProbadorEntity>>
    suspend fun obtenerProbadorPorCodigo(codigo: String): ProbadorEntity?
    suspend fun obtenerCantidadProbadores(): Int
    suspend fun cambiarEstadoProbador(probador: ProbadorEntity, nuevoEstado: String)
    suspend fun crearProbador(codigo: String, esAccesible: Boolean): String?
    suspend fun cambiarBloqueoProbador(probador: ProbadorEntity, bloquear: Boolean, motivo: String): String?
    suspend fun eliminarProbador(probador: ProbadorEntity): String?
}

class ProbadorRepositoryImpl(
    private val probadorDao: ProbadorDao,
    private val turnoDao: TurnoDao
) : ProbadorRepository {

    override fun obtenerTodosProbadores(): Flow<List<ProbadorEntity>> =
        probadorDao.obtenerTodosProbadores()

    override suspend fun obtenerProbadorPorCodigo(codigo: String): ProbadorEntity? =
        withContext(Dispatchers.IO) {
            probadorDao.obtenerProbadorPorCodigo(codigo.trim().uppercase())
        }

    override suspend fun obtenerCantidadProbadores(): Int =
        withContext(Dispatchers.IO) {
            probadorDao.obtenerCantidadProbadores()
        }

    override suspend fun cambiarEstadoProbador(probador: ProbadorEntity, nuevoEstado: String) =
        withContext(Dispatchers.IO) {
            val actualizado = probador.copy(estado = nuevoEstado)
            probadorDao.actualizarProbador(actualizado)

            if (nuevoEstado == "OCUPADO") {
                val siguienteTurno = turnoDao.obtenerSiguienteTurnoEnEspera()
                siguienteTurno?.let { turno ->
                    turnoDao.actualizarEstadoTurno(turno.id, "ATENDIDO")
                }
            }
        }

    override suspend fun crearProbador(codigo: String, esAccesible: Boolean): String? =
        withContext(Dispatchers.IO) {
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

    override suspend fun cambiarBloqueoProbador(
        probador: ProbadorEntity,
        bloquear: Boolean,
        motivo: String
    ): String? = withContext(Dispatchers.IO) {
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

    override suspend fun eliminarProbador(probador: ProbadorEntity): String? =
        withContext(Dispatchers.IO) {
            if (probador.estado == "OCUPADO") {
                return@withContext "No se puede eliminar ${probador.codigo} mientras está ocupado"
            }
            probadorDao.eliminarProbador(probador)
            null
        }

    companion object {
        const val ESTADO_FUERA_SERVICIO = "FUERA_SERVICIO"
    }
}
