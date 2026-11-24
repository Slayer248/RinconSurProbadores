package com.rinconsur.probadores.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lógica de negocio: el hash de contraseñas y su verificación. */
class PasswordHasherTest {

    @Test
    fun hashPassword_generaSha256ConocidoEIgnoraEspaciosExtremos() {
        // SHA-256 de "abc" (vector de prueba estándar)
        val esperado = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"

        assertEquals(esperado, PasswordHasher.hashPassword("abc"))
        assertEquals(esperado, PasswordHasher.hashPassword("  abc  "))
        assertEquals(64, PasswordHasher.hashPassword("otra clave").length)
    }

    @Test
    fun clavesDistintas_generanHuellasDistintas() {
        val huellaA = PasswordHasher.hashPassword("abc")
        val huellaB = PasswordHasher.hashPassword("abd")

        assertNotEquals(huellaA, huellaB)
    }

    @Test
    fun verifyPassword_aceptaHashYTextoPlanoHeredado_yRechazaElResto() {
        val hash = PasswordHasher.hashPassword("Clave#2026")

        assertTrue(PasswordHasher.verifyPassword("Clave#2026", hash))
        assertTrue(PasswordHasher.verifyPassword("Clave#2026", hash.uppercase()))
        // Compatibilidad con contraseñas guardadas antiguamente en texto plano
        assertTrue(PasswordHasher.verifyPassword("admin1234", "admin1234"))

        assertFalse(PasswordHasher.verifyPassword("otra", hash))
        assertFalse(PasswordHasher.verifyPassword("Clave#2026", ""))
        assertFalse(PasswordHasher.verifyPassword("Clave#2026", "   "))
    }
}
