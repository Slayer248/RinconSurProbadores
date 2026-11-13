package com.rinconsur.probadores.data.repository

import com.rinconsur.probadores.data.local.dao.TurnoDao
import com.rinconsur.probadores.data.local.entities.TurnoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface TurnoRepository {
    fun obtenerTurnosPorEstado(estado: String): Flow<List<TurnoEntity>>
    suspend fun obtenerSiguienteTurnoEnEspera(): TurnoEntity?
    suspend fun obtenerTurnoActivoCliente(nombreCliente: String): TurnoEntity?
    suspend fun registrarTurno(
        nombreCliente: String,
        cantidadPrendas: Int,
        esPreferente: Boolean
    ): Pair<Boolean, String>
    suspend fun actualizarEstadoTurno(turnoId: Long, nuevoEstado: String)
}

class TurnoRepositoryImpl(
    private val turnoDao: TurnoDao
) : TurnoRepository {

    override fun obtenerTurnosPorEstado(estado: String): Flow<List<TurnoEntity>> =
        turnoDao.obtenerTurnosPorEstado(estado)

    override suspend fun obtenerSiguienteTurnoEnEspera(): TurnoEntity? =
        withContext(Dispatchers.IO) {
            turnoDao.obtenerSiguienteTurnoEnEspera()
        }

    override suspend fun obtenerTurnoActivoCliente(nombreCliente: String): TurnoEntity? =
        withContext(Dispatchers.IO) {
            turnoDao.obtenerTurnoActivoCliente(nombreCliente.trim())
        }

    override suspend fun registrarTurno(
        nombreCliente: String,
        cantidadPrendas: Int,
        esPreferente: Boolean
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
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

    override suspend fun actualizarEstadoTurno(turnoId: Long, nuevoEstado: String) =
        withContext(Dispatchers.IO) {
            turnoDao.actualizarEstadoTurno(turnoId, nuevoEstado)
        }
}
