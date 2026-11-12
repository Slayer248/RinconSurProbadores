package com.rinconsur.probadores.utils

import java.security.MessageDigest

object PasswordHasher {

    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.trim().toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(plainPassword: String, storedPassword: String): Boolean {
        if (storedPassword.isBlank()) return false
        val hashedInput = hashPassword(plainPassword)
        // Comparación con hash SHA-256 o compatibilidad con texto plano heredado
        return hashedInput.equals(storedPassword, ignoreCase = true) || plainPassword.trim() == storedPassword
    }
}
