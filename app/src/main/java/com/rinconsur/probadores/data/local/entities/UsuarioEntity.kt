package com.rinconsur.probadores.data.local.entities

data class UsuarioEntity(
    val username: String, // Clave única de usuario
    val nombre: String,   // Nombre completo
    val correo: String = "", // Correo electrónico
    val contrasena: String,
    val rol: String,      // "ADMIN" o "ASISTENTE"
    val bloqueado: Boolean = false // Una cuenta bloqueada no puede iniciar sesión
)
