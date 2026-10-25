package com.rinconsur.probadores.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rinconsur.probadores.data.local.entities.ProbadorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProbadorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProbadores(probadores: List<ProbadorEntity>)

    @Update
    suspend fun actualizarProbador(probador: ProbadorEntity)

    @Query("SELECT * FROM probador")
    fun obtenerTodosProbadores(): Flow<List<ProbadorEntity>>

    @Query("SELECT COUNT(*) FROM probador")
    suspend fun obtenerCantidadProbadores(): Int
}