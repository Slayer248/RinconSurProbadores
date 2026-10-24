package com.rinconsur.probadores.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class UsuarioEntity(
    @PrimaryKey
    val rut: String,              // Ej: "12345678-K" o "admin"
    val nombreCompleto: String,
    val contrasena: String,
    val rol: String                // "ADMIN", "ASISTENTE", "CLIENTE"
)