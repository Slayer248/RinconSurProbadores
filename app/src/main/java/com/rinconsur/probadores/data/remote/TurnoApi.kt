package com.rinconsur.probadores.data.remote

import retrofit2.http.*

interface TurnoApi {

    @POST("api/turnos")
    suspend fun crearTurno(@Body request: CrearTurnoRequest): TurnoResponse

    @GET("api/turnos")
    suspend fun listarTurnos(
        @Query("estado") estado: String? = "EN_ESPERA"
    ): List<TurnoResponse>

    @GET("api/turnos/{id}")
    suspend fun obtenerPorId(@Path("id") id: Long): TurnoResponse

    @POST("api/turnos/atender-siguiente")
    suspend fun atenderSiguiente(): TurnoResponse

    @PATCH("api/turnos/{id}/estado")
    suspend fun cambiarEstado(
        @Path("id") id: Long,
        @Body request: CambiarEstadoTurnoRequest
    ): TurnoResponse
}