// RolApi.kt
package com.rinconsur.probadores.data.remote

import retrofit2.http.GET

interface RolApi {
    @GET("api/roles")
    suspend fun listarRoles(): List<RolDto>
}