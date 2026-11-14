package com.rinconsur.probadores.data.remote

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

sealed interface ResultadoApi<out T> {
    data class Exito<T>(val datos: T) : ResultadoApi<T>
    data class Error(val mensaje: String, val codigoHttp: Int? = null) : ResultadoApi<Nothing>
}

suspend fun <T> llamarApi(bloque: suspend () -> T): ResultadoApi<T> {
    return try {
        ResultadoApi.Exito(bloque())
    } catch (e: HttpException) {
        val cuerpoError = e.response()?.errorBody()?.string()
        val mensajeServidor = try {
            if (!cuerpoError.isNullOrBlank()) {
                JSONObject(cuerpoError).optString("message").takeIf { it.isNotBlank() }
            } else null
        } catch (_: Exception) {
            null
        }
        ResultadoApi.Error(
            mensaje = mensajeServidor ?: "Error del servidor (${e.code()})",
            codigoHttp = e.code()
        )
    } catch (_: IOException) {
        ResultadoApi.Error("No se pudo conectar con el servidor")
    } catch (e: Exception) {
        ResultadoApi.Error(e.message ?: "Error inesperado")
    }
}