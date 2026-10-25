package com.rinconsur.probadores.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rinconsur.probadores.data.local.entities.TurnoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TurnoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTurno(turno: TurnoEntity)

    @Query("SELECT MAX(id) FROM turnos")
    suspend fun obtenerUltimoNumero(): Int?

    @Query("SELECT * FROM turnos WHERE estado = :estado ORDER BY esPreferente DESC, id ASC")
    fun obtenerTurnosPorEstado(estado: String): Flow<List<TurnoEntity>>

    @Query("SELECT * FROM turnos WHERE estado = 'EN_ESPERA' ORDER BY esPreferente DESC, id ASC LIMIT 1")
    suspend fun obtenerSiguienteTurnoEnEspera(): TurnoEntity?

    @Query("UPDATE turnos SET estado = :nuevoEstado WHERE id = :turnoId")
    suspend fun actualizarEstadoTurno(turnoId: Long, nuevoEstado: String)
}