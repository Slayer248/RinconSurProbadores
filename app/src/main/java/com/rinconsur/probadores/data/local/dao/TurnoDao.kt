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

    @Query("SELECT * FROM turnos WHERE estado = :estado ORDER BY id ASC")
    fun obtenerTurnosPorEstado(estado: String): Flow<List<TurnoEntity>>

    @Query("SELECT * FROM turnos ORDER BY id DESC")
    fun obtenerTodosTurnos(): Flow<List<TurnoEntity>>
}