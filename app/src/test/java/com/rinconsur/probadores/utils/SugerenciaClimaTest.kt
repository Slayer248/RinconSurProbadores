package com.rinconsur.probadores.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/** Lógica de negocio: qué se sugiere probarse según la temperatura. */
class SugerenciaClimaTest {

    @Test
    fun sugerenciaTemperatura() {
        assertEquals("Buen momento para probarte un abrigo.", SugerenciaClima.sugerir(14.9))
        assertEquals("Ni mucho frío ni mucho calor: buen momento para probarte algo cómodo.", SugerenciaClima.sugerir(15.0))
        assertEquals("Ni mucho frío ni mucho calor: buen momento para probarte algo cómodo.", SugerenciaClima.sugerir(24.0))
        assertEquals("Buen momento para probarte ropa ligera.", SugerenciaClima.sugerir(24.1))
    }
}
