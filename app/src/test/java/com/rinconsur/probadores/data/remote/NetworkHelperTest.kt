package com.rinconsur.probadores.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** Lógica de negocio: cómo se traducen las respuestas y fallos de la API a resultados para la app. */
class NetworkHelperTest {

    @Test
    fun DevuelveOk() = runBlocking {
        val resultado = llamarApi { "ok" }
        assertEquals(ResultadoApi.Exito("ok"), resultado)
    }

    @Test
    fun sinInternet() = runBlocking {
        val resultado = llamarApi<String> { throw IOException("sin internet") }
        assertEquals(ResultadoApi.Error("No se pudo conectar con el servidor"), resultado)
    }

    @Test
    fun errorSinMensaje() = runBlocking {
        val resultado = llamarApi<String> {
            throw HttpException(Response.error<String>(500, "".toResponseBody(null)))
        } as ResultadoApi.Error

        assertEquals("Error del servidor (500)", resultado.mensaje)
        assertEquals(500, resultado.codigoHttp)
    }

    @Test
    fun errorConMensaje() = runBlocking {
        val cuerpo = """{"message":"El usuario ya existe"}""".toResponseBody("application/json".toMediaType())

        val resultado = llamarApi<String> {
            throw HttpException(Response.error<String>(409, cuerpo))
        } as ResultadoApi.Error

        assertEquals("El usuario ya existe", resultado.mensaje)
        assertEquals(409, resultado.codigoHttp)
    }
}
