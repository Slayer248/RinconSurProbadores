// UsuarioApi.kt
package com.rinconsur.probadores.data.remote

import retrofit2.Response
import retrofit2.http.*

interface UsuarioApi {

    @POST("api/usuarios/login")
    suspend fun login(@Body request: LoginRequest): UsuarioResponse

    @GET("api/usuarios")
    suspend fun listarUsuarios(
        @Query("buscar") buscar: String? = null,
        @Query("rolId") rolId: Long? = null
    ): List<UsuarioResponse>

    @GET("api/usuarios/{id}")
    suspend fun obtenerPorId(@Path("id") id: Long): UsuarioResponse

    @GET("api/usuarios/username/{username}")
    suspend fun obtenerPorUsername(@Path("username") username: String): UsuarioResponse

    @POST("api/usuarios")
    suspend fun crearUsuario(@Body request: RegistroUsuarioRequest): UsuarioResponse

    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(
        @Path("id") id: Long,
        @Body request: ActualizarUsuarioRequest
    ): UsuarioResponse

    @PUT("api/usuarios/{id}/contrasena")
    suspend fun cambiarContrasena(
        @Path("id") id: Long,
        @Body request: CambiarContrasenaRequest
    ): Response<Unit>

    @PATCH("api/usuarios/{id}/bloqueo")
    suspend fun cambiarBloqueo(
        @Path("id") id: Long,
        @Body request: BloqueoRequest,
        @Header("X-Admin-Id") adminId: Long? = null
    ): UsuarioResponse

    @DELETE("api/usuarios/{id}")
    suspend fun eliminarUsuario(
        @Path("id") id: Long,
        @Header("X-Admin-Id") adminId: Long? = null
    ): Response<Unit>
}