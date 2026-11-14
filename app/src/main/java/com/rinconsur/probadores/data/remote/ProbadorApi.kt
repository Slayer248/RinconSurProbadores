package com.rinconsur.probadores.data.remote

import retrofit2.Response
import retrofit2.http.*

interface ProbadorApi {

    @GET("api/probadores")
    suspend fun listarProbadores(
        @Query("estado") estado: String? = null
    ): List<ProbadorResponse>

    @GET("api/probadores/{codigo}")
    suspend fun obtenerPorCodigo(@Path("codigo") codigo: String): ProbadorResponse

    @POST("api/probadores")
    suspend fun crearProbador(@Body request: CrearProbadorRequest): ProbadorResponse

    @PUT("api/probadores/{codigo}/estado")
    suspend fun cambiarEstado(
        @Path("codigo") codigo: String,
        @Body request: CambiarEstadoProbadorRequest
    ): ProbadorResponse

    @PATCH("api/probadores/{codigo}/bloqueo")
    suspend fun cambiarBloqueo(
        @Path("codigo") codigo: String,
        @Body request: BloqueoProbadorRequest
    ): ProbadorResponse

    @DELETE("api/probadores/{codigo}")
    suspend fun eliminarProbador(@Path("codigo") codigo: String): Response<Unit>
}