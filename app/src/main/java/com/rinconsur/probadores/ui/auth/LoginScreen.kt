package com.rinconsur.probadores.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rinconsur.probadores.R
import com.rinconsur.probadores.data.repository.ResultadoLogin
import com.rinconsur.probadores.utils.SessionManager

private enum class ModoAcceso {
    CLIENTES,
    EMPLEADOS
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onNavigateToCliente: (String) -> Unit,
    onNavigateToAsistente: (String) -> Unit,
    onNavigateToAdmin: (String) -> Unit,
    autoReingresoPendiente: Boolean = false,
    onAutoReingresoConsumido: () -> Unit = {}
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

    // Al abrir la app con un turno activo recordado, se entra directo a él (para verlo o cancelarlo)
    LaunchedEffect(turnoRecordado, autoReingresoPendiente) {
        if (autoReingresoPendiente && turnoRecordado != null && ultimoInvitadoRecordado != null) {
            onAutoReingresoConsumido()
            onNavigateToCliente(ultimoInvitadoRecordado)
        }
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
    var modoAcceso by rememberSaveable { mutableStateOf(ModoAcceso.CLIENTES) }

    Scaffold(
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (modoAcceso == ModoAcceso.CLIENTES) {
                        Button(
                            onClick = { modoAcceso = ModoAcceso.CLIENTES },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clientes")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { modoAcceso = ModoAcceso.CLIENTES },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clientes")
                        }
                    }

                    if (modoAcceso == ModoAcceso.EMPLEADOS) {
                        Button(
                            onClick = { modoAcceso = ModoAcceso.EMPLEADOS },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Empleados")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { modoAcceso = ModoAcceso.EMPLEADOS },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Empleados")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo Rincón Sur Probadores",
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Rincón Sur",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Control de Acceso y Probadores",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 40.dp)
            )

            if (modoAcceso == ModoAcceso.CLIENTES) {
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
                                    val ingresarComoInvitado = {
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

                                    val otroInvitado = ultimoInvitadoRecordado != null &&
                                        !cleanName.equals(ultimoInvitadoRecordado, ignoreCase = true)
                                    if (otroInvitado) {
                                        // Con un turno vigente no se puede entrar como otro invitado hasta cancelarlo
                                        viewModel.buscarTurnoActivo(ultimoInvitadoRecordado!!) { turnoVigente ->
                                            if (turnoVigente != null) {
                                                nombreInvitadoError = "Tienes el turno ${turnoVigente.numeroTurno} activo como '$ultimoInvitadoRecordado'. Reingresa a tu turno y cancélalo para entrar con otro nombre."
                                            } else {
                                                ingresarComoInvitado()
                                            }
                                        }
                                    } else {
                                        ingresarComoInvitado()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ingresar como Cliente")
                        }
                    }
                }
            } else {
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
        }
    }

    // Diálogo de Recuperación de Contraseña
    if (mostrarDialogRecuperar) {
        var usernameRecuperacion by remember { mutableStateOf("") }
        var usernameRecuperacionError by remember { mutableStateOf<String?>(null) }
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
                        text = "Ingresa tu nombre de usuario (username). Te enviaremos una nueva contraseña temporal al correo electrónico registrado en tu cuenta.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = usernameRecuperacion,
                        onValueChange = {
                            usernameRecuperacion = it
                            usernameRecuperacionError = null
                        },
                        label = { Text("Nombre de Usuario") },
                        isError = usernameRecuperacionError != null,
                        supportingText = usernameRecuperacionError?.let { err -> { Text(err) } },
                        singleLine = true,
                        enabled = !cargandoRecuperacion,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !cargandoRecuperacion,
                    onClick = {
                        val cleanUser = usernameRecuperacion.trim()
                        if (cleanUser.isEmpty()) {
                            usernameRecuperacionError = "Ingresa tu nombre de usuario"
                            return@Button
                        }

                        cargandoRecuperacion = true
                        viewModel.recuperarContrasena(cleanUser) { exito, mensaje ->
                            cargandoRecuperacion = false
                            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                            if (exito) {
                                mostrarDialogRecuperar = false
                            }
                        }
                    }
                ) {
                    Text(if (cargandoRecuperacion) "Enviando..." else "Enviar al Correo")
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
