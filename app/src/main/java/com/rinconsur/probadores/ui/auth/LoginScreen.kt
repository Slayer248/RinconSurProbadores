package com.rinconsur.probadores.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rinconsur.probadores.ui.asistente.AsistenteViewModel

@Composable
fun LoginScreen(
    viewModel: AsistenteViewModel,
    onNavigateToCliente: (String) -> Unit,
    onNavigateToAsistente: (String) -> Unit,
    onNavigateToAdmin: (String) -> Unit
) {
    val context = LocalContext.current
    var nombreInvitado by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var mostrarDialogRecuperar by remember { mutableStateOf(false) }
    var correoRecuperacion by remember { mutableStateOf("") }
    var nuevaPassword by remember { mutableStateOf("") }

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
            modifier = Modifier.padding(bottom = 32.dp)
        )

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
                        // Filtro solo letras y espacios
                        if (input.all { it.isLetter() || it.isWhitespace() }) {
                            nombreInvitado = input
                        }
                    },
                    label = { Text("Tu Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val nombreClean = nombreInvitado.trim()
                        if (nombreClean.isEmpty()) {
                            Toast.makeText(context, "Ingresa tu nombre para continuar", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (nombreClean.length < 3) {
                            Toast.makeText(context, "El nombre de invitado debe tener mínimo 3 letras", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onNavigateToCliente(nombreClean)
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

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Usuario") },
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
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
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
                        val userClean = username.trim()
                        val passClean = password.trim()

                        if (userClean.isEmpty() || passClean.isEmpty()) {
                            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (userClean.length < 3) {
                            Toast.makeText(context, "El usuario debe tener mínimo 3 letras", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (passClean.length < 4) {
                            Toast.makeText(context, "La contraseña debe tener mínimo 4 caracteres", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        viewModel.autenticar(userClean, passClean) { rol ->
                            when (rol) {
                                "ADMIN" -> onNavigateToAdmin(userClean)
                                "ASISTENTE" -> onNavigateToAsistente(userClean)
                                AsistenteViewModel.ROL_BLOQUEADO -> {
                                    Toast.makeText(context, "Tu cuenta está bloqueada. Contacta al administrador", Toast.LENGTH_LONG).show()
                                }
                                else -> {
                                    Toast.makeText(context, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
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
                    onClick = { mostrarDialogRecuperar = true },
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

                    OutlinedTextField(
                        value = correoRecuperacion,
                        onValueChange = { correoRecuperacion = it },
                        label = { Text("Correo Electrónico") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nuevaPassword,
                        onValueChange = { nuevaPassword = it },
                        label = { Text("Nueva Contraseña") },
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
                        val mailClean = correoRecuperacion.trim()
                        val passClean = nuevaPassword.trim()

                        if (mailClean.isEmpty() || passClean.isEmpty()) {
                            Toast.makeText(context, "Ingresa el correo y la nueva contraseña", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mailClean).matches()) {
                            Toast.makeText(context, "Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val errPass = com.rinconsur.probadores.utils.PasswordValidator.validarContrasena(passClean)
                        if (errPass != null) {
                            Toast.makeText(context, errPass, Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        viewModel.recuperarContrasena(mailClean, passClean) { exito, mensaje ->
                            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                            if (exito) {
                                correoRecuperacion = ""
                                nuevaPassword = ""
                                mostrarDialogRecuperar = false
                            }
                        }
                    }
                ) {
                    Text("Cambiar Contraseña")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        correoRecuperacion = ""
                        nuevaPassword = ""
                        mostrarDialogRecuperar = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
