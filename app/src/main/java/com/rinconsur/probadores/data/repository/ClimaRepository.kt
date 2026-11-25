package com.rinconsur.probadores.data.repository

import com.rinconsur.probadores.data.remote.ClimaApi
import com.rinconsur.probadores.data.remote.ClimaClient
import com.rinconsur.probadores.data.remote.ResultadoApi
import com.rinconsur.probadores.data.remote.llamarApi

interface ClimaRepository {
    /** Temperatura actual en °C, o null si no se pudo obtener. */
    suspend fun obtenerTemperatura(): Double?
}

class ClimaRepositoryImpl(
    private val climaApi: ClimaApi = ClimaClient.climaApi
) : ClimaRepository {

    override suspend fun obtenerTemperatura(): Double? {
        return when (val res = llamarApi { climaApi.obtenerClima() }) {
            is ResultadoApi.Exito -> res.datos.current?.temperatura
            is ResultadoApi.Error -> null
        }
    }
}
