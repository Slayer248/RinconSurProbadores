package com.rinconsur.probadores.ui.cliente

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.rinconsur.probadores.data.local.entities.TurnoEntity
import com.rinconsur.probadores.ui.probadorRepositoryFalso
import com.rinconsur.probadores.ui.turnoRepositoryFalso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClienteViewModelTest {

    @get:Rule
    val reglaLiveData = InstantTaskExecutorRule()

    @Before
    fun prepararHilos() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun limpiarHilos() = Dispatchers.resetMain()

    private fun turno(id: Long, nombre: String) = TurnoEntity(
        id = id, numeroTurno = "T-$id", nombreCliente = nombre, cantidadPrendas = 2, esPreferente = false
    )

    private fun crearViewModel(
        fila: MutableList<TurnoEntity>,
        alActualizarEstado: (Long, String) -> String?
    ) = ClienteViewModel(
        Application(),
        turnoRepositoryFalso(
            "obtenerTurnosPorEstadoONull" to { _ -> fila.toList() },
            "actualizarEstadoTurno" to { args ->
                val error = alActualizarEstado(args[0] as Long, args[1] as String)
                if (error == null) fila.removeAll { it.id == args[0] as Long }
                error
            }
        ),
        probadorRepositoryFalso("obtenerTodosProbadores" to { _ -> emptyList<Any>() })
    )

    @Test
    fun validarCantidadPrendas_aceptaDe1a6_yRechazaElResto() {
        val vm = crearViewModel(mutableListOf()) { _, _ -> null }

        assertEquals("Por favor ingresa la cantidad de prendas", vm.validarCantidadPrendas("   "))
        assertEquals("La cantidad de prendas debe ser entre 1 y 6", vm.validarCantidadPrendas("0"))
        assertEquals("La cantidad de prendas debe ser entre 1 y 6", vm.validarCantidadPrendas("7"))
        assertEquals("La cantidad de prendas debe ser entre 1 y 6", vm.validarCantidadPrendas("tres"))
        for (n in 1..6) assertNull(vm.validarCantidadPrendas(" $n "))
    }

    @Test
    fun cancelarMiTurno_quitaElTurnoDeLaFila_peroConservaTodoSiHayError() {
        val fila = mutableListOf(turno(1, "Ana Morales"), turno(2, "Luis Pérez"))
        var error: String? = "Sin conexión"
        val vm = crearViewModel(fila) { _, _ -> error }
        assertEquals(2, vm.turnosEnEsperaLiveData.value?.size)

        // Con error del servidor, el turno sigue en la fila y se informa el motivo
        var resultado: Pair<Boolean, String?>? = null
        vm.cancelarMiTurno(1) { exito, msj -> resultado = exito to msj }
        assertEquals(false to "Sin conexión", resultado)
        assertEquals(2, vm.turnosEnEsperaLiveData.value?.size)

        // Sin error, el turno cancelado desaparece de la fila
        error = null
        vm.cancelarMiTurno(1) { exito, msj -> resultado = exito to msj }
        assertTrue(resultado?.first == true)
        assertNull(resultado?.second)
        assertEquals(listOf("Luis Pérez"), vm.turnosEnEsperaLiveData.value?.map { it.nombreCliente })
        assertFalse(vm.turnosEnEsperaLiveData.value.orEmpty().any { it.id == 1L })
    }
}
