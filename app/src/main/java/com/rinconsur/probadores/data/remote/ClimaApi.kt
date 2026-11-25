package com.rinconsur.probadores.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/** Respuesta de Open-Meteo. Solo se declaran los datos que usa la app. */
data class ClimaResponse(
    val current: ClimaActual?
)

data class ClimaActual(
    @SerializedName("temperature_2m") val temperatura: Double?
)

/** Servicio Retrofit de la API EXTERNA Open-Meteo (https://open-meteo.com). No requiere clave. */
interface ClimaApi {

    @GET("v1/forecast")
    suspend fun obtenerClima(
        @Query("latitude") latitud: Double = LATITUD_SANTIAGO,
        @Query("longitude") longitud: Double = LONGITUD_SANTIAGO,
        @Query("current") datos: String = "temperature_2m"
    ): ClimaResponse

    companion object {
        const val LATITUD_SANTIAGO = -33.45
        const val LONGITUD_SANTIAGO = -70.66
    }
}
