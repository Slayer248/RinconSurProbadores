package com.rinconsur.probadores.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val URL_ROLES = "http://10.0.2.2:8081/"
    private const val URL_USUARIOS = "http://10.0.2.2:8082/"
    private const val URL_PROBADORES = "http://10.0.2.2:8083/"
    private const val URL_TURNOS = "http://10.0.2.2:8084/"

    private fun crearRetrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val rolApi: RolApi by lazy { crearRetrofit(URL_ROLES).create(RolApi::class.java) }
    val usuarioApi: UsuarioApi by lazy { crearRetrofit(URL_USUARIOS).create(UsuarioApi::class.java) }
    val probadorApi: ProbadorApi by lazy { crearRetrofit(URL_PROBADORES).create(ProbadorApi::class.java) }
    val turnoApi: TurnoApi by lazy { crearRetrofit(URL_TURNOS).create(TurnoApi::class.java) }
}