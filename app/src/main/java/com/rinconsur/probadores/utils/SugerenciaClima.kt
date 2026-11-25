package com.rinconsur.probadores.utils

/** Sugiere qué probarse según la temperatura exterior. */
object SugerenciaClima {

    fun sugerir(temperatura: Double): String {
        return when {
            temperatura < 15 -> "Buen momento para probarte un abrigo."
            temperatura > 24 -> "Buen momento para probarte ropa ligera."
            else -> "Ni mucho frío ni mucho calor: buen momento para probarte algo cómodo."
        }
    }
}
