package com.rinconsur.probadores.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rinconsur.probadores.data.repository.ResultadoLogin
import com.rinconsur.probadores.utils.SessionManager

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onNavigateToCliente: (String) -> Unit,
    onNavigateToAsistente: (String) -> Unit,
    onNavigateToAdmin: (String) -> Unit
) {
    val context = LocalContext.current

    val turnosEnEspera by viewModel.turnosEnEsperaLiveData.observeAsState(initial = emptyList())

    val ultimoInvitadoRecordado = remember(context) {
        SessionManager.obtenerUltimoInvitado(context)
    }

    val turnoRecordado = remember(turnosEnEspera, ultimoInvitadoRecordado) {
        if (!ultimoInvitadoRecordado.isNullOrBlank()) {
            turnosEnEspera.find { it.nombreCliente.equals(ultimoInvitadoRecordado, ignoreCase = true) }
        } else null
    }

    val posicionTurnoRecordado = remember(turnoRecordado, turnosEnEspera) {
        if (turnoRecordado != null) turnosEnEspera.indexOf(turnoRecordado) + 1 else 0
    }

    var nombreInvitado by remember { mutableStateOf(ultimoInvitadoRecordado ?: "") }
    var nombreInvitadoError by remember { mutableStateOf<String?>(null) }

    var username by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf<String?>(null) }

    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var loginGeneralError by remember { mutableStateOf<String?>(null) }
    var cargandoLogin by remember { mutableStateOf(false) }

    var mostrarDialogRecuperar by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Rincón Sur",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Control de Acceso y Probadores",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Banner para reingresar a un turno activo recordado en el dispositivo
        if (ultimoInvitadoRecordado != null && turnoRecordado != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.HourglassTop,
                        contentDescription = "Turno Recordado",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 12.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¡Hola, $ultimoInvitadoRecordado!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tienes el turno ${turnoRecordado.numeroTurno} activo en la posición #$posicionTurnoRecordado de la fila.",
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                SessionManager.guardarUltimoInvitado(context, ultimoInvitadoRecordado)
                                onNavigateToCliente(ultimoInvitadoRecordado)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reingresar a mi Turno")
                        }
                    }
                }
            }
        }

        // Card Acceso Invitado
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Acceso Clientes / Invitados",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = nombreInvitado,
                    onValueChange = { input ->
                        if (input.all { it.isLetter() || it.isWhitespace() }) {
                            nombreInvitado = input
                            nombreInvitadoError = null
                        }
                    },
                    label = { Text("Tu Nombre") },
                    isError = nombreInvitadoError != null,
                    supportingText = nombreInvitadoError?.let { err -> { Text(err) } },
                    trailingIcon = if (nombreInvitadoError != null) {
                        { Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val basicError = viewModel.validarNombreInvitado(nombreInvitado)
                        nombreInvitadoError = basicError

                        if (basicError == null) {
                            val cleanName = nombreInvitado.trim()
                            viewModel.validarInvitadoDisponible(cleanName) { dbError ->
                                val esMismoInvitadoRecordado = cleanName.equals(ultimoInvitadoRecordado, ignoreCase = true) && turnoRecordado != null
                                if (dbError == null || esMismoInvitadoRecordado) {
                                    SessionManager.guardarUltimoInvitado(context, cleanName)
                                    onNavigateToCliente(cleanName)
                                } else {
                                    nombreInvitadoError = dbError
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ingresar como Cliente")
                }
            }
        }

        // Card Acceso Empleados
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Acceso Empleados",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (loginGeneralError != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = "Alerta",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = loginGeneralError!!,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        usernameError = null
                        loginGeneralError = null
                    },
                    label = { Text("Usuario") },
                    isError = usernameError != null,
                    supportingText = usernameError?.let { err -> { Text(err) } },
                    trailingIcon = if (usernameError != null) {
                        { Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    enabled = !cargandoLogin,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.None
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                        loginGeneralError = null
                    },
                    label = { Text("Contraseña") },
                    isError = passwordError != null,
                    supportingText = passwordError?.let { err -> { Text(err) } },
                    trailingIcon = if (passwordError != null) {
                        { Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    enabled = !cargandoLogin,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        capitalization = KeyboardCapitalization.None
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { mostrarDialogRecuperar = true },
                        enabled = !cargandoLogin
                    ) {
                        Text("¿Olvidaste tu contraseña?", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    enabled = !cargandoLogin,
                    onClick = {
                        val errUser = viewModel.validarUsername(username)
                        val errPass = viewModel.validarContrasenaLogin(password)

                        usernameError = errUser
                        passwordError = errPass

                        if (errUser == null && errPass == null) {
                            cargandoLogin = true
                            viewModel.autenticar(username, password) { res ->
                                cargandoLogin = false
                                when (res) {
                                    is ResultadoLogin.Exito -> {
                                        when (res.usuario.rolNombre) {
                                            "ADMIN" -> onNavigateToAdmin(res.usuario.username)
                                            "ASISTENTE" -> onNavigateToAsistente(res.usuario.username)
                                            else -> loginGeneralError = "Rol no autorizado."
                                        }
                                    }
                                    is ResultadoLogin.Error -> {
                                        loginGeneralError = res.mensaje
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (cargandoLogin) "Conectando..." else "Iniciar Sesión")
                }
            }
        }
    }

    // Diálogo de Recuperación de Contraseña
    if (mostrarDialogRecuperar) {
        var correoRecuperacion by remember { mutableStateOf("") }
        var correoRecuperacionError by remember { mutableStateOf<String?>(null) }
        var nuevaPasswordRecuperacion by remember { mutableStateOf("") }
        var nuevaPasswordError by remember { mutableStateOf<String?>(null) }
        var cargandoRecuperacion by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!cargandoRecuperacion) mostrarDialogRecuperar = false },
            title = {
                Text(
                    text = "Recuperar Contraseña",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Ingresa tu correo electrónico registrado y tu nueva contraseña para actualizar la clave de tu cuenta.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = correoRecuperacion,
                        onValueChange = {
                            correoRecuperacion = it
                            correoRecuperacionError = null
                        },
                        label = { Text("Correo Electrónico") },
                        isError = correoRecuperacionError != null,
                        supportingText = correoRecuperacionError?.let { err -> { Text(err) } },
                        singleLine = true,
                        enabled = !cargandoRecuperacion,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nuevaPasswordRecuperacion,
                        onValueChange = {
                            nuevaPasswordRecuperacion = it
                            nuevaPasswordError = null
                        },
                        label = { Text("Nueva Contraseña") },
                        isError = nuevaPasswordError != null,
                        supportingText = nuevaPasswordError?.let { err -> { Text(err) } },
                        singleLine = true,
                        enabled = !cargandoRecuperacion,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !cargandoRecuperacion,
                    onClick = {
                        val errMail = viewModel.validarCorreo(correoRecuperacion)
                        val errPass = com.rinconsur.probadores.utils.PasswordValidator.validarContrasena(nuevaPasswordRecuperacion)

                        correoRecuperacionError = errMail
                        nuevaPasswordError = errPass

                        if (errMail == null && errPass == null) {
                            cargandoRecuperacion = true
                            viewModel.recuperarContrasena(
                                correo = correoRecuperacion,
                                nuevaContrasena = nuevaPasswordRecuperacion
                            ) { exito, mensaje ->
                                cargandoRecuperacion = false
                                Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                                if (exito) {
                                    mostrarDialogRecuperar = false
                                }
                            }
                        }
                    }
                ) {
                    Text(if (cargandoRecuperacion) "Actualizando..." else "Restablecer")
                }
            },
            dismissButton = {
                OutlinedButton(
                    enabled = !cargandoRecuperacion,
                    onClick = { mostrarDialogRecuperar = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
