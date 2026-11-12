package com.rinconsur.probadores.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey
    val username: String, // Clave única de usuario
    val nombre: String,   // Nombre completo
    val correo: String = "", // Correo electrónico
    val contrasena: String,
    val rol: String,      // "ADMIN" o "ASISTENTE"
    @ColumnInfo(defaultValue = "0")
    val bloqueado: Boolean = false // Una cuenta bloqueada no puede iniciar sesión
)
