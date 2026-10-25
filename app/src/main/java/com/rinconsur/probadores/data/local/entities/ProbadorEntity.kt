package com.rinconsur.probadores.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "probador")
data class ProbadorEntity(
    @PrimaryKey val codigo: String,
    val esAccesible: Boolean,
    val estado: String,             // LIBRE, OCUPADO, ORDENAMIENTO, FUERA_SERVICIO
    val desdeHora: String,
    val motivoFueraServicio: String? = null
)