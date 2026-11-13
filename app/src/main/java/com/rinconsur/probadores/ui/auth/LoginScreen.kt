package com.rinconsur.probadores.ui.auth

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
import com.rinconsur.probadores.utils.PasswordValidator
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

    var mostrarDialogRecuperar by remember { mutableStateOf(false) }
    var correoRecuperacion by remember { mutableStateOf("") }
    var correoRecuperacionError by remember { mutableStateOf<String?>(null) }

    var nuevaPassword by remember { mutableStateOf("") }
    var nuevaPasswordError by remember { mutableStateOf<String?>(null) }

    var mensajeRecuperacion by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

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
                                // Si coincide con el invitado recordado y tiene un turno activo propio, permitir ingresar
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
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        capitalization = KeyboardCapitalization.None
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val errUser = viewModel.validarUsername(username)
                        val errPass = viewModel.validarContrasenaLogin(password)

                        usernameError = errUser
                        passwordError = errPass

                        if (errUser == null && errPass == null) {
                            viewModel.autenticar(username, password) { rol ->
                                when (rol) {
                                    "ADMIN" -> onNavigateToAdmin(username.trim())
                                    "ASISTENTE" -> onNavigateToAsistente(username.trim())
                                    LoginViewModel.ROL_BLOQUEADO -> {
                                        loginGeneralError = "Tu cuenta se encuentra bloqueada. Contacta al administrador."
                                    }
                                    else -> {
                                        loginGeneralError = "Usuario o contraseña incorrectos."
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar Sesión")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        correoRecuperacion = ""
                        nuevaPassword = ""
                        correoRecuperacionError = null
                        nuevaPasswordError = null
                        mensajeRecuperacion = null
                        mostrarDialogRecuperar = true
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("¿Olvidaste tu contraseña?")
                }
            }
        }
    }

    // Diálogo para recuperación de contraseña
    if (mostrarDialogRecuperar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogRecuperar = false },
            title = { Text("Recuperar Contraseña") },
            text = {
                Column {
                    Text(
                        text = "Ingresa el correo electrónico asociado a tu cuenta y la nueva contraseña.",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    mensajeRecuperacion?.let { (exito, msg) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (exito) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = msg,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = correoRecuperacion,
                        onValueChange = {
                            correoRecuperacion = it
                            correoRecuperacionError = null
                        },
                        label = { Text("Correo Electrónico") },
                        isError = correoRecuperacionError != null,
                        supportingText = correoRecuperacionError?.let { err -> { Text(err) } },
                        trailingIcon = if (correoRecuperacionError != null) {
                            { Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) }
                        } else null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nuevaPassword,
                        onValueChange = {
                            nuevaPassword = it
                            nuevaPasswordError = null
                        },
                        label = { Text("Nueva Contraseña") },
                        isError = nuevaPasswordError != null,
                        supportingText = nuevaPasswordError?.let { err -> { Text(err) } },
                        trailingIcon = if (nuevaPasswordError != null) {
                            { Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) }
                        } else null,
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val errMail = viewModel.validarCorreo(correoRecuperacion)
                        val errPass = PasswordValidator.validarContrasena(nuevaPassword.trim())

                        correoRecuperacionError = errMail
                        nuevaPasswordError = errPass

                        if (errMail == null && errPass == null) {
                            viewModel.recuperarContrasena(correoRecuperacion, nuevaPassword) { exito, mensaje ->
                                mensajeRecuperacion = Pair(exito, mensaje)
                                if (exito) {
                                    correoRecuperacion = ""
                                    nuevaPassword = ""
                                }
                            }
                        }
                    }
                ) {
                    Text("Cambiar Contraseña")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarDialogRecuperar = false }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}
