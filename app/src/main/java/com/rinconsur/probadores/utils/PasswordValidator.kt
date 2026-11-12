package com.rinconsur.probadores.utils

object PasswordValidator {

    fun validarContrasena(password: String): String? {
        val pass = password.trim()
        if (pass.length < 8) {
            return "La contraseña debe tener al menos 8 caracteres"
        }
        if (!pass.any { it.isUpperCase() }) {
            return "La contraseña debe incluir al menos una letra mayúscula (A-Z)"
        }
        if (!pass.any { it.isLowerCase() }) {
            return "La contraseña debe incluir al menos una letra minúscula (a-z)"
        }
        if (!pass.any { it.isDigit() }) {
            return "La contraseña debe incluir al menos un número (0-9)"
        }
        if (!pass.any { !it.isLetterOrDigit() && !it.isWhitespace() }) {
            return "La contraseña debe incluir al menos un carácter especial (ej: @, #, $, %, !)"
        }
        return null
    }
}
