package com.rinconsur.probadores.ui

import com.rinconsur.probadores.data.repository.ProbadorRepository
import com.rinconsur.probadores.data.repository.TurnoRepository
import com.rinconsur.probadores.data.repository.UsuarioRepository
import java.lang.reflect.Proxy

/**
 * Repositorios de mentira para probar los ViewModel sin red ni librerías de mocks.
 * [respuestas] asocia el nombre de cada función con lo que debe devolver.
 * Las funciones no definidas devuelven null (por ejemplo, "sin resultado").
 */
private inline fun <reified T : Any> falso(respuestas: Map<String, (Array<Any?>) -> Any?>): T =
    Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, metodo, args ->
        respuestas[metodo.name]?.invoke(args ?: emptyArray())
    } as T

fun turnoRepositoryFalso(vararg respuestas: Pair<String, (Array<Any?>) -> Any?>): TurnoRepository =
    falso(respuestas.toMap())

fun usuarioRepositoryFalso(vararg respuestas: Pair<String, (Array<Any?>) -> Any?>): UsuarioRepository =
    falso(respuestas.toMap())

fun probadorRepositoryFalso(vararg respuestas: Pair<String, (Array<Any?>) -> Any?>): ProbadorRepository =
    falso(respuestas.toMap())
