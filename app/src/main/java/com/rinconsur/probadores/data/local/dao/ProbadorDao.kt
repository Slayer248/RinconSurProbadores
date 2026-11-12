package com.rinconsur.probadores.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rinconsur.probadores.data.local.entities.ProbadorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProbadorDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarProbadores(probadores: List<ProbadorEntity>)

    // ABORT: un código de probador repetido no sobrescribe el existente
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarProbador(probador: ProbadorEntity)

    @Update
    suspend fun actualizarProbador(probador: ProbadorEntity) // Libre, Ocupado, Limpieza

    @Delete
    suspend fun eliminarProbador(probador: ProbadorEntity)

    @Query("SELECT * FROM probador WHERE UPPER(codigo) = UPPER(:codigo) LIMIT 1")
    suspend fun obtenerProbadorPorCodigo(codigo: String): ProbadorEntity?

    @Query("SELECT * FROM probador ORDER BY codigo ASC")
    fun obtenerTodosProbadores(): Flow<List<ProbadorEntity>>

    @Query("SELECT COUNT(*) FROM probador")
    suspend fun obtenerCantidadProbadores(): Int
}
