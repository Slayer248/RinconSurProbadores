package com.rinconsur.probadores.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.rinconsur.probadores.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    // ABORT: si el username ya existe lanza SQLiteConstraintException en vez de sobrescribir la cuenta
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarUsuario(usuario: UsuarioEntity)

    @Update
    suspend fun actualizarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun obtenerUsuarioPorUsername(username: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE LOWER(correo) = LOWER(:correo) LIMIT 1")
    suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    fun obtenerTodosUsuarios(): Flow<List<UsuarioEntity>>

    @Query("DELETE FROM usuarios WHERE LOWER(username) = LOWER(:username)")
    suspend fun eliminarUsuarioPorUsername(username: String)

    @Query("UPDATE usuarios SET bloqueado = :bloqueado WHERE LOWER(username) = LOWER(:username)")
    suspend fun cambiarBloqueo(username: String, bloqueado: Boolean)

    // Cambio de clave primaria (username): se elimina el registro anterior y se inserta el nuevo de forma atómica
    @Transaction
    suspend fun reemplazarUsername(usernameAnterior: String, usuarioNuevo: UsuarioEntity) {
        eliminarUsuarioPorUsername(usernameAnterior)
        insertarUsuario(usuarioNuevo)
    }
}
