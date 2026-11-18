package com.rinconsur.probadores.ui.auth

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rinconsur.probadores.data.repository.UsuarioRepository
import com.rinconsur.probadores.data.repository.UsuarioRepositoryImpl
import com.rinconsur.probadores.utils.PasswordValidator
import com.rinconsur.probadores.utils.SessionManager
import kotlinx.coroutines.launch

class CambioContrasenaViewModel @JvmOverloads constructor(
    application: Application,
    private val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl()
) : AndroidViewModel(application) {

    fun cambiarContrasena(
        username: String,
        actual: String,
        nueva: String,
        onResultado: (exito: Boolean, mensaje: String) -> Unit
    ) {
        viewModelScope.launch {
            val (exito, mensaje) = usuarioRepository.cambiarContrasena(username, actual, nueva)
            if (exito) SessionManager.marcarContrasenaCambiada(getApplication())
            onResultado(exito, mensaje)
        }
    }

    fun cerrarSesion() {
        SessionManager.cerrarSesionEmpleado(getApplication())
    }
}

/**
 * Aviso obligatorio para cuentas nuevas o con contraseña temporal.
 * No se puede cerrar hasta cambiar la contraseña o salir de la sesión.
 */
@Composable
fun AvisoCambioContrasena(
    username: String,
    onCerrarSesion: () -> Unit,
    viewModel: CambioContrasenaViewModel = viewModel()
) {
    val context = LocalContext.current
    var pendiente by remember { mutableStateOf(SessionManager.debeCambiarContrasena(context)) }
    if (!pendiente) return

    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text("Por favor cambia tu contraseña", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Estás usando una contraseña temporal. Por seguridad, debes crear una nueva para continuar.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                CampoContrasena("Contraseña temporal", actual, !guardando) {
                    actual = it
                    error = null
                }
                Spacer(modifier = Modifier.height(8.dp))
                CampoContrasena("Nueva contraseña", nueva, !guardando) {
                    nueva = it
                    error = null
                }
                Spacer(modifier = Modifier.height(8.dp))
                CampoContrasena("Confirmar nueva contraseña", confirmar, !guardando) {
                    confirmar = it
                    error = null
                }

                Text(
                    text = error ?: "Mínimo 8 caracteres, con mayúscula, minúscula, número y carácter especial.",
                    fontSize = 12.sp,
                    color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !guardando,
                onClick = {
                    val actualClean = actual.trim()
                    val nuevaClean = nueva.trim()
                    val validacion = when {
                        actualClean.isEmpty() -> "Ingresa la contraseña temporal"
                        nuevaClean != confirmar.trim() -> "Las contraseñas no coinciden"
                        nuevaClean == actualClean -> "La nueva contraseña debe ser diferente a la temporal"
                        else -> PasswordValidator.validarContrasena(nuevaClean)
                    }
                    error = validacion
                    if (validacion == null) {
                        guardando = true
                        viewModel.cambiarContrasena(username, actualClean, nuevaClean) { exito, mensaje ->
                            guardando = false
                            if (exito) {
                                Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                                pendiente = false
                            } else {
                                error = mensaje
                            }
                        }
                    }
                }
            ) {
                Text(if (guardando) "Guardando..." else "Cambiar")
            }
        },
        dismissButton = {
            OutlinedButton(
                enabled = !guardando,
                onClick = {
                    viewModel.cerrarSesion()
                    onCerrarSesion()
                }
            ) {
                Text("Cerrar sesión")
            }
        }
    )
}

@Composable
private fun CampoContrasena(
    etiqueta: String,
    valor: String,
    habilitado: Boolean,
    onCambio: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        enabled = habilitado,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth()
    )
}
