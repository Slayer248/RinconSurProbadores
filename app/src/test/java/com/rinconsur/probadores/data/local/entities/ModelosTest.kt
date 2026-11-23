package com.rinconsur.probadores.data.local.entities

import com.rinconsur.probadores.data.remote.ProbadorResponse
import com.rinconsur.probadores.data.remote.TurnoResponse
import com.rinconsur.probadores.data.remote.UsuarioResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Modelos: conversión de las respuestas del servidor a las entidades que usa la app. */
class ModelosTest {

    @Test
    fun respuestasDelServidor_seConvierten_aEntidadesSinPerderDatos() {
        val usuario = UsuarioResponse(
            id = 5, username = "mgonzalez", nombre = "Maria Gonzalez", correo = "mg@rinconsur.test",
            rolId = 2, rolNombre = "ASISTENTE", bloqueado = true
        ).aUsuarioEntity()
        assertEquals("mgonzalez", usuario.username)
        assertEquals("ASISTENTE", usuario.rol)
        assertTrue(usuario.bloqueado)
        // La contraseña nunca viaja al cliente
        assertEquals("", usuario.contrasena)

        val turno = TurnoResponse(9, "T-1001", "Ana Morales", 3, true, "EN_ESPERA").aTurnoEntity()
        assertEquals(9L, turno.id)
        assertEquals("Ana Morales", turno.nombreCliente)
        assertEquals(3, turno.cantidadPrendas)
        assertTrue(turno.esPreferente)

        val probador = ProbadorResponse("P-01", true, "OCUPADO", "14:30", null, "Ana Morales", "T-1001", "20-11-2026 14:30")
            .aProbadorEntity()
        assertEquals("P-01", probador.codigo)
        assertTrue(probador.esAccesible)
        assertEquals("Ana Morales", probador.ultimoCliente)
    }

    @Test
    fun probadorEntity_ultimoUsoTexto_describeElUltimoInvitado() {
        val sinUso = ProbadorEntity("P-01", false, "LIBRE", "-")
        assertEquals("Último uso: sin registros", sinUso.ultimoUsoTexto)
        assertFalse(ProbadorEntity("P-01", false, "LIBRE", "-", ultimoCliente = "  ").ultimoUsoTexto.contains("("))

        val conUso = ProbadorEntity(
            "P-01", false, "LIBRE", "-",
            ultimoCliente = "Ana Morales", ultimoTurno = "T-1001", ultimoUso = "20-11-2026 14:30"
        )
        assertEquals("Último uso: Ana Morales (T-1001) el 20-11-2026 14:30", conUso.ultimoUsoTexto)

        val sinTurno = conUso.copy(ultimoTurno = null, ultimoUso = null)
        assertEquals("Último uso: Ana Morales (-) el -", sinTurno.ultimoUsoTexto)
    }
}
