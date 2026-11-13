package com.rinconsur.probadores.ui.asistente

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rinconsur.probadores.data.local.entities.ProbadorEntity
import com.rinconsur.probadores.data.local.entities.TurnoEntity

@Composable
fun AsistenteScreen(
    username: String,
    viewModel: AsistenteViewModel = viewModel()
) {
    val context = LocalContext.current
    var currentUsernameState by remember(username) { mutableStateOf(username) }

    val probadores by viewModel.probadoresLiveData.observeAsState(initial = emptyList())
    val turnosEnEspera by viewModel.turnosEnEsperaLiveData.observeAsState(initial = emptyList())

    var mostrarDialogPerfil by remember { mutableStateOf(false) }
    var nombrePerfil by remember { mutableStateOf("") }
    var correoPerfil by remember { mutableStateOf("") }
    var usernamePerfil by remember { mutableStateOf("") }
    var passActual by remember { mutableStateOf("") }
    var passNueva by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Cabecera del Panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Panel de Asistente",
                    fontSize = 20.sp,
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
                            nombrePerfil = "Asistente"
                            correoPerfil = ""
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Sección Estado de Probadores
            item {
                Text(
                    text = "Estado de Probadores",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(probadores, key = { it.codigo }) { probador ->
                Box(modifier = Modifier.animateItem()) {
                    ItemProbador(
                        probador = probador,
                        onCambiarEstado = { p, nuevoEstado ->
                            viewModel.cambiarEstadoProbador(p, nuevoEstado)
                            com.rinconsur.probadores.utils.NotificationHelper.enviarNotificacion(
                                context = context,
                                titulo = if (nuevoEstado == "LIBRE") "¡Probador Liberado!" else "Estado de Probador",
                                mensaje = "El probador ${p.codigo} cambió su estado a $nuevoEstado"
                            )
                        }
                    )
                }
            }

            // Sección Fila de Espera
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Fila de Espera",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    AnimatedContent(
                        targetState = turnosEnEspera.size,
                        transitionSpec = {
                            (slideInVertically { height -> height } + fadeIn()).togetherWith(
                                slideOutVertically { height -> -height } + fadeOut()
                            )
                        },
                        label = "FilaCountAnimation"
                    ) { count ->
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(text = "$count en espera", modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }

            if (turnosEnEspera.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay clientes en fila de espera",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(turnosEnEspera, key = { _, turno -> turno.id }) { index, turno ->
                    Box(modifier = Modifier.animateItem()) {
                        ItemTurnoAsistente(turno = turno, lugar = index + 1)
                    }
                }
            }
        }
    }

    // Diálogo Modificar Perfil
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
                            val errPass = com.rinconsur.probadores.utils.PasswordValidator.validarContrasena(passNuevClean)
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
private fun ItemProbador(
    probador: ProbadorEntity,
    onCambiarEstado: (ProbadorEntity, String) -> Unit
) {
    val siguienteEstado = when (probador.estado) {
        "LIBRE" -> "OCUPADO"
        "OCUPADO" -> "LIMPIEZA"
        else -> "LIBRE"
    }

    // Animación fluida de color de contenedor según el estado del probador
    val targetContainerColor = when (probador.estado) {
        "LIBRE" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        "OCUPADO" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        "FUERA_SERVICIO" -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
    }

    val animatedContainerColor by animateColorAsState(
        targetValue = targetContainerColor,
        animationSpec = tween(durationMillis = 400),
        label = "ProbadorColorAnimation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = animatedContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Probador: ${probador.codigo}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Transición animada del estado con íconos descriptivos
                AnimatedContent(
                    targetState = probador.estado,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(300)) + slideInVertically { it / 2 })
                            .togetherWith(fadeOut(animationSpec = tween(300)) + slideOutVertically { -it / 2 })
                    },
                    label = "EstadoTextAnimation"
                ) { estadoTarget ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (icon, color) = when (estadoTarget) {
                            "LIBRE" -> Pair(Icons.Filled.CheckCircle, MaterialTheme.colorScheme.primary)
                            "OCUPADO" -> Pair(Icons.Filled.Person, MaterialTheme.colorScheme.error)
                            "FUERA_SERVICIO" -> Pair(Icons.Filled.Lock, MaterialTheme.colorScheme.outline)
                            else -> Pair(Icons.Filled.CleaningServices, MaterialTheme.colorScheme.tertiary)
                        }

                        Icon(
                            imageVector = icon,
                            contentDescription = estadoTarget,
                            tint = color,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 4.dp)
                        )

                        Text(
                            text = estadoTarget,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (probador.esAccesible) "Tipo: Preferencial / Accesible" else "Tipo: Estándar",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Ocupado desde: ${probador.desdeHora}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (probador.estado == "FUERA_SERVICIO") {
                Text(
                    text = "Bloqueado por administración" +
                        (probador.motivoFueraServicio?.let { ": $it" } ?: ""),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onCambiarEstado(probador, siguienteEstado) },
                enabled = probador.estado != "FUERA_SERVICIO",
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (probador.estado == "FUERA_SERVICIO") "No disponible" else "Cambiar a $siguienteEstado")
            }
        }
    }
}

@Composable
private fun ItemTurnoAsistente(turno: TurnoEntity, lugar: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$lugar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = turno.nombreCliente,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                val preferenteText = if (turno.esPreferente) " • Preferencial" else ""
                Text(
                    text = "${turno.numeroTurno} • ${turno.cantidadPrendas} prendas$preferenteText",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
