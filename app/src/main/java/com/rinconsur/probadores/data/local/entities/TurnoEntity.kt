package com.rinconsur.probadores.data.local.entities

data class TurnoEntity(
    val id: Long = 0,
    val numeroTurno: String,
    val nombreCliente: String,
    val cantidadPrendas: Int,
    val esPreferente: Boolean,
    val estado: String = "EN_ESPERA"
)
