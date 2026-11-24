package com.rinconsur.probadores.data.local.entities

import org.junit.Assert.assertEquals
import org.junit.Test

/** Modelos: el texto de "Último uso" que arma cada probador. */
class ModelosTest {

    @Test
    fun ProbadorSinUso() {
        val sinUso = ProbadorEntity("P-01", false, "LIBRE", "-")
        assertEquals("Último uso: sin registros", sinUso.ultimoUsoTexto)
    }

    @Test
    fun ProbadorUsado() {
        val conUso = ProbadorEntity(
            "P-01", false, "LIBRE", "-",
            ultimoCliente = "Ana Morales", ultimoTurno = "T-1001", ultimoUso = "20-11-2026 14:30"
        )
        assertEquals("Último uso: Ana Morales (T-1001) el 20-11-2026 14:30", conUso.ultimoUsoTexto)
    }

    @Test
    fun ProbadorSinTurno() {
        val sinTurno = ProbadorEntity("P-01", false, "LIBRE", "-", ultimoCliente = "Ana Morales")
        assertEquals("Último uso: Ana Morales (-) el -", sinTurno.ultimoUsoTexto)
    }
}
