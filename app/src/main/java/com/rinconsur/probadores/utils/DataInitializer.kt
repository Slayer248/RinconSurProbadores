package com.rinconsur.probadores.utils

import com.rinconsur.probadores.data.local.dao.ProbadorDao
import com.rinconsur.probadores.data.local.dao.UsuarioDao
import com.rinconsur.probadores.data.local.entities.ProbadorEntity
import com.rinconsur.probadores.data.local.entities.UsuarioEntity

object DataInitializer {

    suspend fun inicializarDatosSiEstaVacio(
        usuarioDao: UsuarioDao,
        probadorDao: ProbadorDao
    ) {
        // 1. Inicializar Administrador por defecto si no existe
        val adminExiste = usuarioDao.obtenerUsuarioPorUsername("admin")
        if (adminExiste == null) {
            val adminInicial = UsuarioEntity(
                username = "admin",
                nombre = "Administrador Principal",
                contrasena = "admin1234",
                rol = "ADMIN"
            )
            usuarioDao.insertarUsuario(adminInicial)
        }

        // 2. Inicializar Probadores si la tabla está vacía
        val cantidad = probadorDao.obtenerCantidadProbadores()
        if (cantidad == 0) {
            val probadoresIniciales = listOf(
                ProbadorEntity(codigo = "P-01", estado = "LIBRE", esAccesible = false, desdeHora = "-"),
                ProbadorEntity(codigo = "P-02", estado = "LIBRE", esAccesible = false, desdeHora = "-"),
                ProbadorEntity(codigo = "P-03", estado = "LIBRE", esAccesible = false, desdeHora = "-")
            )
            probadorDao.insertarProbadores(probadoresIniciales)
        }
    }
}