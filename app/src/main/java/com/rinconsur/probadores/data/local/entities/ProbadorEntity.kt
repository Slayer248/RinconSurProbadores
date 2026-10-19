package com.rinconsur.probadores.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "probador")
data class ProbadorEntity(
    @PrimaryKey val codigo: String, // P-01 a P-08
    val esAccesible: Boolean,       // true solo para P-08
    val estado: String,             // LIBRE, OCUPADO, ORDENAMIENTO, FUERA_SERVICIO
    val desdeHora: String,          // HH:mm
    val motivoFueraServicio: String? = null
)