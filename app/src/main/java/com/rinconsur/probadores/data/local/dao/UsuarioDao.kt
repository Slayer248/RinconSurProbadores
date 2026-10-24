package com.rinconsur.probadores.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rinconsur.probadores.data.local.entities.UsuarioEntity

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuario WHERE rut = :rut LIMIT 1")
    suspend fun obtenerUsuarioPorRut(rut: String): UsuarioEntity?

    @Query("SELECT * FROM usuario WHERE rol = :rol")
    suspend fun obtenerUsuariosPorRol(rol: String): List<UsuarioEntity>

    @Query("SELECT COUNT(*) FROM usuario")
    suspend fun obtenerCantidadUsuarios(): Int
}