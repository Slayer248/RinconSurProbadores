package com.rinconsur.probadores.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "turno")
data class TurnoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val numeroTurno: String,
    val fechaHoraIngreso: Long,
    val esPreferente: Boolean,
    val cantidadPrendas: Int,
    val estado: String,
    val rutColaborador: String? = null
)