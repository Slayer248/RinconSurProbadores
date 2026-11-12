package com.rinconsur.probadores.ui.admin

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rinconsur.probadores.data.local.entities.UsuarioEntity
import com.rinconsur.probadores.ui.asistente.AsistenteViewModel
import com.rinconsur.probadores.utils.PasswordValidator

@Composable
fun AdminScreen(
    username: String,
    viewModel: AsistenteViewModel
) {
    val context = LocalContext.current
    var currentUsernameState by remember(username) { mutableStateOf(username) }

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var usernameField by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf("ASISTENTE") }

    var usuarioSeleccionadoDialog by remember { mutableStateOf<UsuarioEntity?>(null) }

    var mostrarDialogPerfil by remember { mutableStateOf(false) }
    var nombrePerfil by remember { mutableStateOf("") }
    var correoPerfil by remember { mutableStateOf("") }
    var usernamePerfil by remember { mutableStateOf("") }
    var passActual by remember { mutableStateOf("") }
    var passNueva by remember { mutableStateOf("") }

    val todosUsuarios by viewModel.usuariosLiveData.observeAsState(initial = emptyList())
    val soloAsistentes = remember(todosUsuarios) { todosUsuarios.filter { it.rol == "ASISTENTE" } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Cabecera del Panel de Administración
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Administración",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Usuario: $currentUsernameState",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = {
                    viewModel.obtenerUsuario(currentUsernameState) { usuario ->
                        if (usuario != null) {
                            nombrePerfil = usuario.nombre
                            correoPerfil = usuario.correo
                            usernamePerfil = usuario.username
                        } else {
                            nombrePerfil = "Administrador"
                            correoPerfil = "admin@rinconsur.com"
                            usernamePerfil = currentUsernameState
                        }
                        passActual = ""
                        passNueva = ""
                        mostrarDialogPerfil = true
                    }
                }
            ) {
                Text("Modificar Perfil")
            }
        }

        // Sección Formulario Crear Cuenta
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Registrar Nueva Cuenta",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { input ->
                        if (input.all { it.isLetter() || it.isWhitespace() }) {
                            nombre = input
                        }
                    },
                    label = { Text("Nombre Completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo Electrónico") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = usernameField,
                    onValueChange = { input ->
                        if (input.all { it.isLetter() }) {
                            usernameField = input
                        }
                    },
                    label = { Text("Nombre de Usuario (Username)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña (Min 8, mayús, minús, num, especial)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = rolSeleccionado == "ASISTENTE",
                        onClick = { rolSeleccionado = "ASISTENTE" }
                    )
                    Text("Asistente", modifier = Modifier.padding(end = 16.dp))

                    RadioButton(
                        selected = rolSeleccionado == "ADMIN",
                        onClick = { rolSeleccionado = "ADMIN" }
                    )
                    Text("Administrador")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val nomClean = nombre.trim()
                        val correoClean = correo.trim()
                        val userClean = usernameField.trim()
                        val passClean = password.trim()
                        val confirmPassClean = confirmPassword.trim()

                        if (nomClean.isEmpty() || correoClean.isEmpty() || userClean.isEmpty() || passClean.isEmpty() || confirmPassClean.isEmpty()) {
                            Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correoClean).matches()) {
                            Toast.makeText(context, "Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (userClean.length < 3) {
                            Toast.makeText(context, "El nombre de usuario debe tener mínimo 3 letras", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (passClean != confirmPassClean) {
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val errPass = PasswordValidator.validarContrasena(passClean)
                        if (errPass != null) {
                            Toast.makeText(context, errPass, Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        viewModel.crearUsuario(nomClean, correoClean, userClean, passClean, rolSeleccionado) { errorMsg ->
                            if (errorMsg != null) {
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            } else {
                                nombre = ""
                                correo = ""
                                usernameField = ""
                                password = ""
                                confirmPassword = ""
                                Toast.makeText(context, "Cuenta registrada con éxito para $userClean", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear Cuenta")
                }
            }
        }

        Text(
            text = "Cuentas de Asistentes",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(soloAsistentes, key = { it.username }) { usuario ->
                ItemUsuario(
                    usuario = usuario,
                    onClick = { usuarioSeleccionadoDialog = usuario }
                )
            }
        }
    }

    // Dialog de información de cuenta seleccionada
    usuarioSeleccionadoDialog?.let { usuario ->
        AlertDialog(
            onDismissRequest = { usuarioSeleccionadoDialog = null },
            title = { Text("Información de la Cuenta") },
            text = {
                Column {
                    Text("Nombre: ${usuario.nombre}")
                    Text("Correo: ${if (usuario.correo.isNotBlank()) usuario.correo else "Sin correo"}")
                    Text("Username: ${usuario.username}")
                    Text("Rol: ${usuario.rol}")
                }
            },
            confirmButton = {
                TextButton(onClick = { usuarioSeleccionadoDialog = null }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Diálogo Modificar Perfil de Admin
    if (mostrarDialogPerfil) {
        AlertDialog(
            onDismissRequest = { mostrarDialogPerfil = false },
            title = { Text("Modificar Perfil") },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Ingresa tu contraseña actual para confirmar y guardar los cambios de tu perfil.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = nombrePerfil,
                        onValueChange = { input ->
                            if (input.all { it.isLetter() || it.isWhitespace() }) {
                                nombrePerfil = input
                            }
                        },
                        label = { Text("Nombre Completo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = correoPerfil,
                        onValueChange = { correoPerfil = it },
                        label = { Text("Correo Electrónico") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = usernamePerfil,
                        onValueChange = { input ->
                            if (input.all { it.isLetter() }) {
                                usernamePerfil = input
                            }
                        },
                        label = { Text("Nombre de Usuario (Username)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = passActual,
                        onValueChange = { passActual = it },
                        label = { Text("Contraseña Actual (Obligatoria)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = passNueva,
                        onValueChange = { passNueva = it },
                        label = { Text("Nueva Contraseña (Opcional)") },
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
                        val nomClean = nombrePerfil.trim()
                        val mailClean = correoPerfil.trim()
                        val userClean = usernamePerfil.trim()
                        val passActClean = passActual.trim()
                        val passNuevClean = passNueva.trim()

                        if (passActClean.isEmpty()) {
                            Toast.makeText(context, "Debes ingresar tu contraseña actual para confirmar", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (nomClean.isEmpty() || mailClean.isEmpty() || userClean.isEmpty()) {
                            Toast.makeText(context, "Por favor completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mailClean).matches()) {
                            Toast.makeText(context, "Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (userClean.length < 3) {
                            Toast.makeText(context, "El usuario debe tener mínimo 3 letras", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (passNuevClean.isNotEmpty()) {
                            val errPass = PasswordValidator.validarContrasena(passNuevClean)
                            if (errPass != null) {
                                Toast.makeText(context, errPass, Toast.LENGTH_LONG).show()
                                return@Button
                            }
                        }

                        viewModel.modificarPerfil(
                            currentUsername = currentUsernameState,
                            passwordActual = passActClean,
                            nuevoNombre = nomClean,
                            nuevoCorreo = mailClean,
                            nuevoUsername = userClean,
                            nuevaPassword = passNuevClean
                        ) { exito, mensaje, nuevoUser ->
                            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                            if (exito) {
                                if (nuevoUser != null) {
                                    currentUsernameState = nuevoUser
                                }
                                mostrarDialogPerfil = false
                            }
                        }
                    }
                ) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarDialogPerfil = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ItemUsuario(
    usuario: UsuarioEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = usuario.nombre,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            if (usuario.correo.isNotBlank()) {
                Text(
                    text = "Correo: ${usuario.correo}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Usuario: ${usuario.username}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Rol: ${usuario.rol}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
