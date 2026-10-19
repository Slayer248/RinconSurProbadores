package com.rinconsur.probadores.utils

import com.rinconsur.probadores.data.local.dao.ProbadorDao
import com.rinconsur.probadores.data.local.entities.ProbadorEntity

object DataInitializer {
    suspend fun inicializarProbadoresSiEstaVacio(probadorDao: ProbadorDao) {
        val cantidad = probadorDao.obtenerCantidadProbadores()
        if (cantidad == 0) {
            val probadoresIniciales = listOf(
                ProbadorEntity(codigo = "P-01", estado = "LIBRE", esAccesible = false, desdeHora = "-"),
                ProbadorEntity(codigo = "P-02", estado = "LIBRE", esAccesible = false, desdeHora = "-"),
                ProbadorEntity(codigo = "P-03", estado = "LIBRE", esAccesible = false, desdeHora = "-")
                // Se removió P-08 si solo deben existir 3 camarines
            )
            probadorDao.insertarProbadores(probadoresIniciales)
        }
    }
}