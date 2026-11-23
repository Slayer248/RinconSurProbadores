package com.rinconsur.probadores.ui.auth

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.rinconsur.probadores.data.local.entities.TurnoEntity
import com.rinconsur.probadores.data.local.entities.UsuarioEntity
import com.rinconsur.probadores.ui.turnoRepositoryFalso
import com.rinconsur.probadores.ui.usuarioRepositoryFalso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val reglaLiveData = InstantTaskExecutorRule()

    @Before
    fun prepararHilos() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun limpiarHilos() = Dispatchers.resetMain()

    private val turnoDeAna = TurnoEntity(1, "T-1001", "Ana Morales", 2, false)
    private val cuentaMaria = UsuarioEntity("mgonzalez", "Maria Gonzalez", "mg@rinconsur.test", "", "ASISTENTE")

    private fun crearViewModel() = LoginViewModel(
        Application(),
        usuarioRepositoryFalso(
            "obtenerUsuarioPorUsername" to { args ->
                if ((args[0] as String).equals("mgonzalez", ignoreCase = true)) cuentaMaria else null
            }
        ),
        turnoRepositoryFalso(
            "obtenerTurnosPorEstado" to { _ -> emptyList<TurnoEntity>() },
            "obtenerTurnoActivoCliente" to { args ->
                if ((args[0] as String).equals("Ana Morales", ignoreCase = true)) turnoDeAna else null
            }
        )
    )

    private fun validar(vm: LoginViewModel, nombre: String): String? {
        var mensaje: String? = "sin respuesta"
        vm.validarInvitadoDisponible(nombre) { mensaje = it }
        return mensaje
    }

    @Test
    fun validarInvitadoDisponible_rechazaNombresInvalidosRepetidosOReservados() {
        val vm = crearViewModel()

        assertEquals("El nombre no puede estar vacío", validar(vm, "  "))
        assertEquals("El nombre de invitado debe tener al menos 3 letras", validar(vm, "Al"))
        assertEquals(
            "Ya existe un cliente activo en la fila con el nombre 'Ana Morales'. Ingresa tu apellido o un nombre distinto.",
            validar(vm, " Ana Morales ")
        )
        assertEquals(
            "El nombre 'mgonzalez' pertenece a una cuenta registrada. Inicia sesión en la sección de empleados.",
            validar(vm, "mgonzalez")
        )
        assertEquals(
            "El nombre 'ADMIN' pertenece a una cuenta registrada. Inicia sesión en la sección de empleados.",
            validar(vm, "ADMIN")
        )
        // Un nombre libre puede entrar a la fila
        assertNull(validar(vm, "Camila Rojas"))
    }
}
