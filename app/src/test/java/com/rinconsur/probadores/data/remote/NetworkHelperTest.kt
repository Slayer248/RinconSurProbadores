package com.rinconsur.probadores.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** Lógica de negocio: cómo se traducen las respuestas y fallos de la API a resultados para la app. */
class NetworkHelperTest {

    @Test
    fun llamarApi_siElServidorEnviaUnMensaje_esElQueVeElUsuario() = runBlocking {
        val cuerpo = """{"message":"El usuario ya existe"}""".toResponseBody("application/json".toMediaType())

        val resultado = llamarApi<String> {
            throw HttpException(Response.error<String>(409, cuerpo))
        } as ResultadoApi.Error

        // Se muestra el texto del servidor y no un "Error del servidor (409)" genérico
        assertEquals("El usuario ya existe", resultado.mensaje)
        assertEquals(409, resultado.codigoHttp)
    }

    @Test
    fun llamarApi_traduceExitoYCadaTipoDeFallo() = runBlocking {
        val exito = llamarApi { "ok" }
        assertEquals(ResultadoApi.Exito("ok"), exito)

        val sinRed = llamarApi<String> { throw IOException("sin internet") }
        assertEquals(ResultadoApi.Error("No se pudo conectar con el servidor"), sinRed)

        val http = llamarApi<String> {
            throw HttpException(Response.error<String>(500, "".toResponseBody(null)))
        } as ResultadoApi.Error
        assertEquals("Error del servidor (500)", http.mensaje)
        assertEquals(500, http.codigoHttp)

        val inesperado = llamarApi<String> { throw IllegalStateException("falló algo") } as ResultadoApi.Error
        assertEquals("falló algo", inesperado.mensaje)
        assertNull(inesperado.codigoHttp)
        assertTrue(llamarApi<String> { throw RuntimeException() } is ResultadoApi.Error)
    }
}
