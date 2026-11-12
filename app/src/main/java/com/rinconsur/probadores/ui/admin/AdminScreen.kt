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

@Composable
fun AdminScreen(
    viewModel: AsistenteViewModel,
    onNavigateToAsistente: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf("ASISTENTE") }

    var usuarioSeleccionadoDialog by remember { mutableStateOf<UsuarioEntity?>(null) }

    val todosUsuarios by viewModel.usuariosLiveData.observeAsState(initial = emptyList())
    val soloAsistentes = remember(todosUsuarios) { todosUsuarios.filter { it.rol == "ASISTENTE" } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Administración",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onNavigateToAsistente("Admin") }) {
                    Text("Vista Asistente")
                }
                OutlinedButton(onClick = onNavigateBack) {
                    Text("Volver")
                }
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
                    value = username,
                    onValueChange = { input ->
                        if (input.all { it.isLetter() }) {
                            username = input
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
                    label = { Text("Contraseña (mínimo 8 caracteres)") },
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
                        val userClean = username.trim()
                        val passClean = password.trim()

                        if (nomClean.isEmpty() || userClean.isEmpty() || passClean.isEmpty()) {
                            Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (userClean.length < 3) {
                            Toast.makeText(context, "El nombre de usuario debe tener mínimo 3 letras", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (passClean.length < 8) {
                            Toast.makeText(context, "La contraseña debe tener mínimo 8 caracteres", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        viewModel.crearUsuario(nomClean, userClean, passClean, rolSeleccionado) {
                            nombre = ""
                            username = ""
                            password = ""
                            Toast.makeText(context, "Cuenta registrada con éxito para $userClean", Toast.LENGTH_SHORT).show()
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

    // Dialog de información de cuenta
    usuarioSeleccionadoDialog?.let { usuario ->
        AlertDialog(
            onDismissRequest = { usuarioSeleccionadoDialog = null },
            title = { Text("Información de la Cuenta") },
            text = {
                Column {
                    Text("Nombre: ${usuario.nombre}")
                    Text("Username: ${usuario.username}")
                    Text("Contraseña: ${usuario.contrasena}")
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
