package com.rinconsur.probadores.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    /**
     * Dirección del gateway, la única puerta de entrada al backend (él reparte hacia cada microservicio).
     * - Emulador en el mismo PC que el backend: "http://10.0.2.2:8090/"
     * - Otro PC o celular en la misma red: "http://<IP del PC del backend>:8090/"
     */
    private const val BASE_URL = "http://10.0.2.2:8090/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val rolApi: RolApi by lazy { retrofit.create(RolApi::class.java) }
    val usuarioApi: UsuarioApi by lazy { retrofit.create(UsuarioApi::class.java) }
    val probadorApi: ProbadorApi by lazy { retrofit.create(ProbadorApi::class.java) }
    val turnoApi: TurnoApi by lazy { retrofit.create(TurnoApi::class.java) }
}