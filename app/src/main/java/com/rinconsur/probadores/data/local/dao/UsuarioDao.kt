package com.rinconsur.probadores.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rinconsur.probadores.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE username = :username LIMIT 1")
    suspend fun obtenerUsuarioPorUsername(username: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    fun obtenerTodosUsuarios(): Flow<List<UsuarioEntity>>
}