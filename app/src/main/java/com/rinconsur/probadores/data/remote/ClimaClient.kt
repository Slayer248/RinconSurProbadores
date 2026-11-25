package com.rinconsur.probadores.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Conexión propia para la API externa del clima.
 * Va aparte de [RetrofitClient]: no pasa por el gateway ni toca los microservicios.
 */
object ClimaClient {

    private const val BASE_URL = "https://api.open-meteo.com/"

    val climaApi: ClimaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClimaApi::class.java)
    }
}
