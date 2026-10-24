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

    // CAMBIO: Se corrigió "FROM turno" por "FROM turnos"
    @Query("SELECT * FROM turnos WHERE estado = 'EN_ESPERA' ORDER BY esPreferente DESC, id ASC")
    fun obtenerTurnosEnEspera(): Flow<List<TurnoEntity>>

    // CAMBIO: Se corrigió "FROM turno" por "FROM turnos"
    @Query("SELECT MAX(CAST(SUBSTR(numeroTurno, 3) AS INTEGER)) FROM turnos")
    suspend fun obtenerUltimoNumero(): Int?
}