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

    @Query("SELECT * FROM usuarios WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun obtenerUsuarioPorUsername(username: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE LOWER(correo) = LOWER(:correo) LIMIT 1")
    suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    fun obtenerTodosUsuarios(): Flow<List<UsuarioEntity>>

    @Query("DELETE FROM usuarios WHERE LOWER(username) = LOWER(:username)")
    suspend fun eliminarUsuarioPorUsername(username: String)
}