package com.rinconsur.probadores.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Modelos: lo que la app envía al servidor al crear o editar una cuenta. */
class SolicitudesTest {

    @Test
    fun CrearCuentaSinContrasena() {
        val registro = RegistroUsuarioRequest(username = "pmunoz", nombre = "Pedro Munoz", correo = "pm@rinconsur.test", rolId = 2)
        assertNull(registro.contrasena)
    }

    @Test
    fun EditarCuentaSinContrasena() {
        val edicion = ActualizarUsuarioRequest(username = "pmunoz", nombre = "Pedro Munoz", correo = "pm@rinconsur.test", rolId = 2)
        assertNull(edicion.nuevaContrasena)
    }

    @Test
    fun CambiarContrasena() {
        val cambio = CambiarContrasenaRequest(actual = "Clave#2026", nueva = "Nueva#2027")

        assertEquals("Clave#2026", cambio.actual)
        assertEquals("Nueva#2027", cambio.nueva)
    }
}
