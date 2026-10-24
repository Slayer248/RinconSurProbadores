package com.rinconsur.probadores.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "turnos")
data class TurnoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val numeroTurno: String,
    val nombreCliente: String,
    val cantidadPrendas: Int,
    val esPreferente: Boolean,
    val estado: String = "EN_ESPERA"
)