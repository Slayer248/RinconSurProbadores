package com.rinconsur.probadores.data.local.entities

data class ProbadorEntity(
    val codigo: String,
    val esAccesible: Boolean,
    val estado: String,             // LIBRE, OCUPADO, ORDENAMIENTO, FUERA_SERVICIO
    val desdeHora: String,
    val motivoFueraServicio: String? = null,
    val ultimoCliente: String? = null,
    val ultimoTurno: String? = null,
    val ultimoUso: String? = null
) {
    val ultimoUsoTexto: String
        get() = if (ultimoCliente.isNullOrBlank()) {
            "Último uso: sin registros"
        } else {
            "Último uso: $ultimoCliente (${ultimoTurno ?: "-"}) el ${ultimoUso ?: "-"}"
        }
}
