package com.rinconsur.probadores.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rinconsur.probadores.data.local.entities.TurnoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TurnoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTurno(turno: TurnoEntity)

    @Update
    suspend fun actualizarTurno(turno: TurnoEntity)

    // Apunta a la tabla "turno"
    @Query("SELECT * FROM turno WHERE estado = :estado")
    fun obtenerTurnosPorEstado(estado: String): Flow<List<TurnoEntity>>

    // Apunta a la tabla "turno"
    @Query("SELECT * FROM turno WHERE estado = 'EN_ESPERA' ORDER BY esPreferente DESC, fechaHoraIngreso ASC LIMIT 1")
    suspend fun obtenerSiguienteTurnoEnEspera(): TurnoEntity?
}