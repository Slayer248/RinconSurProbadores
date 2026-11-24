package com.rinconsur.probadores.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Validación: reglas de contraseña segura que se aplican al cambiar la clave. */
class PasswordValidatorTest {

    @Test
    fun contrasenaMuestraError() {
        assertEquals(
            "La contraseña debe tener al menos 8 caracteres",
            PasswordValidator.validarContrasena("Ab1#")
        )
        assertEquals(
            "La contraseña debe incluir al menos una letra mayúscula (A-Z)",
            PasswordValidator.validarContrasena("clave#2026")
        )
        assertEquals(
            "La contraseña debe incluir al menos una letra minúscula (a-z)",
            PasswordValidator.validarContrasena("CLAVE#2026")
        )
        assertEquals(
            "La contraseña debe incluir al menos un número (0-9)",
            PasswordValidator.validarContrasena("Clave#Segura")
        )
        assertEquals(
            "La contraseña debe incluir al menos un carácter especial (ej: @, #, $, %, !)",
            PasswordValidator.validarContrasena("Clave2026")
        )
    }
}
