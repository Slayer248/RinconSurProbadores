package com.rinconsur.probadores.data.repository

import com.rinconsur.probadores.data.remote.ClimaActual
import com.rinconsur.probadores.data.remote.ClimaApi
import com.rinconsur.probadores.data.remote.ClimaResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

/** Repository: cómo se usa la API externa del clima. La API se simula, no se usa internet. */
class ClimaRepositoryTest {

    @Test
    fun climaDisponible() = runBlocking {
        val apiFalsa = object : ClimaApi {
            override suspend fun obtenerClima(latitud: Double, longitud: Double, datos: String) =
                ClimaResponse(ClimaActual(18.5))
        }
        val temperatura = ClimaRepositoryImpl(apiFalsa).obtenerTemperatura()

        assertEquals(18.5, temperatura!!, 0.0)
    }

    @Test
    fun climaSinInternet() = runBlocking {
        val apiFalsa = object : ClimaApi {
            override suspend fun obtenerClima(latitud: Double, longitud: Double, datos: String): ClimaResponse =
                throw IOException("sin internet")
        }

        val temperatura = ClimaRepositoryImpl(apiFalsa).obtenerTemperatura()

        assertNull(temperatura)
    }
}
