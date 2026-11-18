package com.rinconsur.probadores.data.local.entities

data class ProbadorEntity(
    val codigo: String,
    val esAccesible: Boolean,
    val estado: String,             // LIBRE, OCUPADO, ORDENAMIENTO, FUERA_SERVICIO
    val desdeHora: String,
    val motivoFueraServicio: String? = null
)
