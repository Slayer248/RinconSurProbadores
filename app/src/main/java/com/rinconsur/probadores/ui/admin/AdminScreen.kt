package com.rinconsur.probadores.ui.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rinconsur.probadores.data.local.entities.ProbadorEntity
import com.rinconsur.probadores.data.local.entities.UsuarioEntity
import com.rinconsur.probadores.ui.asistente.AsistenteViewModel
import com.rinconsur.probadores.utils.PasswordValidator

/** Acción pendiente de confirmación en el panel de administración. */
private sealed interface AccionPendiente {
    data class EliminarCuenta(val usuario: UsuarioEntity) : AccionPendiente
    data class BloqueoCuenta(val usuario: UsuarioEntity) : AccionPendiente
    data class EliminarProbador(val probador: ProbadorEntity) : AccionPendiente
    data class BloqueoProbador(val probador: ProbadorEntity) : AccionPendiente
}

@Composable
fun AdminScreen(
    username: String,
    viewModel: AsistenteViewModel
) {
    val context = LocalContext.current
    var currentUsernameState by remember(username) { mutableStateOf(username) }

    var tabSeleccionada by rememberSaveable { mutableIntStateOf(0) }

    // Diálogos de cuentas: null = cerrado; en el de edición, el usuario a editar
    var mostrarDialogNuevaCuenta by remember { mutableStateOf(false) }
    var cuentaAEditar by remember { mutableStateOf<UsuarioEntity?>(null) }
    var mostrarDialogNuevoProbador by remember { mutableStateOf(false) }
    var accionPendiente by remember { mutableStateOf<AccionPendiente?>(null) }

    var mostrarDialogPerfil by remember { mutableStateOf(false) }
    var nombrePerfil by remember { mutableStateOf("") }
    var correoPerfil by remember { mutableStateOf("") }
    var usernamePerfil by remember { mutableStateOf("") }
    var passActual by remember { mutableStateOf("") }
    var passNueva by remember { mutableStateOf("") }

    val todosUsuarios by viewModel.usuariosLiveData.observeAsState(initial = emptyList())
    val probadores by viewModel.probadoresLiveData.observeAsState(initial = emptyList())
    // El administrador gestiona todas las cuentas menos la suya (esa se edita en "Modificar Perfil")
    val otrasCuentas = remember(todosUsuarios, currentUsernameState) {
        todosUsuarios.filterNot { it.username.equals(currentUsernameState, ignoreCase = true) }
    }

    val mostrarResultado: (String?, String) -> Unit = { error, exito ->
        Toast.makeText(context, error ?: exito, if (error != null) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
    }

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

        TabRow(selectedTabIndex = tabSeleccionada) {
            Tab(
                selected = tabSeleccionada == 0,
                onClick = { tabSeleccionada = 0 },
                text = { Text("Cuentas (${otrasCuentas.size})") }
            )
            Tab(
                selected = tabSeleccionada == 1,
                onClick = { tabSeleccionada = 1 },
                text = { Text("Probadores (${probadores.size})") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (tabSeleccionada) {
            0 -> {
                Button(
                    onClick = { mostrarDialogNuevaCuenta = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nueva Cuenta")
                }
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (otrasCuentas.isEmpty()) {
                        item { ListaVacia("No hay otras cuentas registradas") }
                    }
                    items(otrasCuentas, key = { it.username }) { usuario ->
                        ItemUsuario(
                            usuario = usuario,
                            onEditar = { cuentaAEditar = usuario },
                            onBloqueo = { accionPendiente = AccionPendiente.BloqueoCuenta(usuario) },
                            onEliminar = { accionPendiente = AccionPendiente.EliminarCuenta(usuario) }
                        )
                    }
                }
            }

            else -> {
                Button(
                    onClick = { mostrarDialogNuevoProbador = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nuevo Probador")
                }
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (probadores.isEmpty()) {
                        item { ListaVacia("No hay probadores registrados") }
                    }
                    items(probadores, key = { it.codigo }) { probador ->
                        ItemProbadorAdmin(
                            probador = probador,
                            onBloqueo = { accionPendiente = AccionPendiente.BloqueoProbador(probador) },
                            onEliminar = { accionPendiente = AccionPendiente.EliminarProbador(probador) }
                        )
                    }
                }
            }
        }
    }

    // Crear / editar cuenta
    if (mostrarDialogNuevaCuenta) {
        CuentaDialog(
            usuarioEditar = null,
            viewModel = viewModel,
            onDismiss = { mostrarDialogNuevaCuenta = false }
        )
    }
    cuentaAEditar?.let { usuario ->
        CuentaDialog(
            usuarioEditar = usuario,
            viewModel = viewModel,
            onDismiss = { cuentaAEditar = null }
        )
    }

    // Crear probador
    if (mostrarDialogNuevoProbador) {
        ProbadorDialog(
            viewModel = viewModel,
            onDismiss = { mostrarDialogNuevoProbador = false }
        )
    }

    // Confirmaciones de eliminar / bloquear
    when (val accion = accionPendiente) {
        is AccionPendiente.EliminarCuenta -> ConfirmarDialog(
            titulo = "Eliminar cuenta",
            mensaje = "¿Eliminar la cuenta de ${accion.usuario.nombre} (${accion.usuario.username})? Esta acción no se puede deshacer.",
            textoConfirmar = "Eliminar",
            destructivo = true,
            onDismiss = { accionPendiente = null },
            onConfirmar = {
                viewModel.eliminarCuenta(accion.usuario.username, currentUsernameState) { error ->
                    mostrarResultado(error, "Cuenta ${accion.usuario.username} eliminada")
                }
                accionPendiente = null
            }
        )

        is AccionPendiente.BloqueoCuenta -> {
            val bloquear = !accion.usuario.bloqueado
            ConfirmarDialog(
                titulo = if (bloquear) "Bloquear cuenta" else "Desbloquear cuenta",
                mensaje = if (bloquear) {
                    "${accion.usuario.username} no podrá iniciar sesión hasta que desbloquees la cuenta."
                } else {
                    "${accion.usuario.username} podrá volver a iniciar sesión."
                },
                textoConfirmar = if (bloquear) "Bloquear" else "Desbloquear",
                destructivo = bloquear,
                onDismiss = { accionPendiente = null },
                onConfirmar = {
                    viewModel.cambiarBloqueoCuenta(accion.usuario.username, bloquear, currentUsernameState) { error ->
                        mostrarResultado(error, if (bloquear) "Cuenta bloqueada" else "Cuenta desbloqueada")
                    }
                    accionPendiente = null
                }
            )
        }

        is AccionPendiente.EliminarProbador -> ConfirmarDialog(
            titulo = "Eliminar probador",
            mensaje = "¿Eliminar el probador ${accion.probador.codigo}? Esta acción no se puede deshacer.",
            textoConfirmar = "Eliminar",
            destructivo = true,
            onDismiss = { accionPendiente = null },
            onConfirmar = {
                viewModel.eliminarProbador(accion.probador) { error ->
                    mostrarResultado(error, "Probador ${accion.probador.codigo} eliminado")
                }
                accionPendiente = null
            }
        )

        is AccionPendiente.BloqueoProbador -> BloqueoProbadorDialog(
            probador = accion.probador,
            onDismiss = { accionPendiente = null },
            onConfirmar = { bloquear, motivo ->
                viewModel.cambiarBloqueoProbador(accion.probador, bloquear, motivo) { error ->
                    mostrarResultado(
                        error,
                        if (bloquear) "Probador ${accion.probador.codigo} bloqueado" else "Probador ${accion.probador.codigo} habilitado"
                    )
                }
                accionPendiente = null
            }
        )

        null -> Unit
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

/**
 * Formulario para crear (usuarioEditar = null) o editar una cuenta.
 * Al editar, el username no se puede cambiar y la contraseña es opcional.
 */
@Composable
private fun CuentaDialog(
    usuarioEditar: UsuarioEntity?,
    viewModel: AsistenteViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val esEdicion = usuarioEditar != null

    var nombre by remember { mutableStateOf(usuarioEditar?.nombre ?: "") }
    var correo by remember { mutableStateOf(usuarioEditar?.correo ?: "") }
    var usernameField by remember { mutableStateOf(usuarioEditar?.username ?: "") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(usuarioEditar?.rol ?: "ASISTENTE") }
    var guardando by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (esEdicion) "Editar Cuenta" else "Registrar Nueva Cuenta") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { input ->
                        if (input.all { it.isLetter() || it.isWhitespace() }) nombre = input
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
                        if (input.all { it.isLetter() }) usernameField = input
                    },
                    label = { Text("Nombre de Usuario (Username)") },
                    supportingText = if (esEdicion) {
                        { Text("El nombre de usuario no se puede modificar") }
                    } else null,
                    enabled = !esEdicion,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(if (esEdicion) "Nueva Contraseña (Opcional)" else "Contraseña (Min 8, mayús, minús, num, especial)")
                    },
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
                Row(verticalAlignment = Alignment.CenterVertically) {
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
            }
        },
        confirmButton = {
            Button(
                enabled = !guardando,
                onClick = {
                    val nomClean = nombre.trim()
                    val correoClean = correo.trim()
                    val userClean = usernameField.trim()
                    val passClean = password.trim()
                    val confirmClean = confirmPassword.trim()
                    // En edición la contraseña sólo se valida si se escribió una nueva
                    val validarPass = !esEdicion || passClean.isNotEmpty() || confirmClean.isNotEmpty()

                    val error = when {
                        nomClean.isEmpty() || correoClean.isEmpty() || userClean.isEmpty() ->
                            "Por favor completa todos los campos"
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(correoClean).matches() ->
                            "Ingresa un correo electrónico válido"
                        userClean.length < 3 ->
                            "El nombre de usuario debe tener mínimo 3 letras"
                        validarPass && passClean.isEmpty() ->
                            "Ingresa una contraseña"
                        validarPass && passClean != confirmClean ->
                            "Las contraseñas no coinciden"
                        validarPass -> PasswordValidator.validarContrasena(passClean)
                        else -> null
                    }
                    if (error != null) {
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    guardando = true
                    val alTerminar: (String?) -> Unit = { errorMsg ->
                        guardando = false
                        if (errorMsg != null) {
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        } else {
                            val msg = if (esEdicion) "Cuenta $userClean actualizada" else "Cuenta registrada con éxito para $userClean"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    }
                    if (esEdicion) {
                        viewModel.editarCuenta(userClean, nomClean, correoClean, rolSeleccionado, passClean, alTerminar)
                    } else {
                        viewModel.crearUsuario(nomClean, correoClean, userClean, passClean, rolSeleccionado, alTerminar)
                    }
                }
            ) {
                Text(if (esEdicion) "Guardar Cambios" else "Crear Cuenta")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun ProbadorDialog(
    viewModel: AsistenteViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var codigo by remember { mutableStateOf("") }
    var esAccesible by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Probador") },
        text = {
            Column {
                OutlinedTextField(
                    value = codigo,
                    onValueChange = { input ->
                        if (input.length <= 10 && input.all { it.isLetterOrDigit() || it == '-' }) {
                            codigo = input.uppercase()
                        }
                    },
                    label = { Text("Código (ej: P-04)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = esAccesible, onCheckedChange = { esAccesible = it })
                    Text("Preferencial / Accesible")
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !guardando,
                onClick = {
                    val codigoClean = codigo.trim()
                    if (codigoClean.length < 2) {
                        Toast.makeText(context, "El código debe tener al menos 2 caracteres", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    guardando = true
                    viewModel.crearProbador(codigoClean, esAccesible) { error ->
                        guardando = false
                        if (error != null) {
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Probador $codigoClean creado", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    }
                }
            ) {
                Text("Crear Probador")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun BloqueoProbadorDialog(
    probador: ProbadorEntity,
    onDismiss: () -> Unit,
    onConfirmar: (bloquear: Boolean, motivo: String) -> Unit
) {
    val bloquear = probador.estado != AsistenteViewModel.ESTADO_FUERA_SERVICIO
    var motivo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (bloquear) "Bloquear ${probador.codigo}" else "Habilitar ${probador.codigo}") },
        text = {
            Column {
                Text(
                    if (bloquear) {
                        "El probador quedará fuera de servicio y los asistentes no podrán asignarlo."
                    } else {
                        "El probador volverá a quedar LIBRE y disponible para los clientes."
                    }
                )
                if (bloquear) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = motivo,
                        onValueChange = { if (it.length <= 60) motivo = it },
                        label = { Text("Motivo (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(bloquear, motivo) },
                colors = if (bloquear) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(if (bloquear) "Bloquear" else "Habilitar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun ConfirmarDialog(
    titulo: String,
    mensaje: String,
    textoConfirmar: String,
    destructivo: Boolean,
    onDismiss: () -> Unit,
    onConfirmar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = if (destructivo) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(textoConfirmar)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun ListaVacia(mensaje: String) {
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
            Text(text = mensaje, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
    }
}

@Composable
private fun EstadoChip(texto: String, esAlerta: Boolean) {
    Surface(
        color = if (esAlerta) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = if (esAlerta) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = texto,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun ItemUsuario(
    usuario: UsuarioEntity,
    onEditar: () -> Unit,
    onBloqueo: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usuario.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (usuario.correo.isNotBlank()) {
                    Text(
                        text = usuario.correo,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Usuario: ${usuario.username}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    EstadoChip(texto = if (usuario.rol == "ADMIN") "ADMINISTRADOR" else "ASISTENTE", esAlerta = false)
                    if (usuario.bloqueado) {
                        EstadoChip(texto = "BLOQUEADA", esAlerta = true)
                    }
                }
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar cuenta")
            }
            IconButton(onClick = onBloqueo) {
                Icon(
                    imageVector = if (usuario.bloqueado) Icons.Filled.LockOpen else Icons.Filled.Lock,
                    contentDescription = if (usuario.bloqueado) "Desbloquear cuenta" else "Bloquear cuenta"
                )
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar cuenta",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ItemProbadorAdmin(
    probador: ProbadorEntity,
    onBloqueo: () -> Unit,
    onEliminar: () -> Unit
) {
    val bloqueado = probador.estado == AsistenteViewModel.ESTADO_FUERA_SERVICIO

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Probador ${probador.codigo}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (probador.esAccesible) "Preferencial / Accesible" else "Estándar",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (bloqueado && !probador.motivoFueraServicio.isNullOrBlank()) {
                    Text(
                        text = "Motivo: ${probador.motivoFueraServicio}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                EstadoChip(
                    texto = if (bloqueado) "BLOQUEADO" else probador.estado,
                    esAlerta = bloqueado
                )
            }
            IconButton(onClick = onBloqueo) {
                Icon(
                    imageVector = if (bloqueado) Icons.Filled.LockOpen else Icons.Filled.Lock,
                    contentDescription = if (bloqueado) "Habilitar probador" else "Bloquear probador"
                )
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar probador",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
