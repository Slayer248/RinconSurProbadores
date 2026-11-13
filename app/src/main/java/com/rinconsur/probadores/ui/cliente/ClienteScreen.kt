package com.rinconsur.probadores.ui.cliente

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rinconsur.probadores.data.local.entities.TurnoEntity
import com.rinconsur.probadores.utils.NotificationHelper

@Composable
fun ClienteScreen(
    username: String,
    viewModel: ClienteViewModel = viewModel(),
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    var cantidadPrendasText by remember { mutableStateOf("") }
    var prendasError by remember { mutableStateOf<String?>(null) }
    var esPreferente by remember { mutableStateOf(false) }
    var turnoMensaje by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    // Solicitud de Permiso Runtime POST_NOTIFICATIONS (Android 13+ / API 33+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationHelper.vibrar(context, 300L)
        }
    }

    LaunchedEffect(Unit) {
        NotificationHelper.crearCanalNotificaciones(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val probadores by viewModel.probadoresLiveData.observeAsState(initial = emptyList())
    val turnosEnEspera by viewModel.turnosEnEsperaLiveData.observeAsState(initial = emptyList())

    val probadoresLibres = remember(probadores) { probadores.count { it.estado == "LIBRE" } }
    val personasEnFila = turnosEnEspera.size
    val tiempoEstimado = if (personasEnFila == 0) 0 else personasEnFila * 2

    val miTurnoActivo = remember(turnosEnEspera) {
        turnosEnEspera.find { it.nombreCliente.equals(username, ignoreCase = true) }
    }
    val posicionMiTurno = remember(miTurnoActivo, turnosEnEspera) {
        if (miTurnoActivo != null) turnosEnEspera.indexOf(miTurnoActivo) + 1 else 0
    }

    // Efecto: Notificar y vibrar cuando el turno del cliente pasa a posición #1
    LaunchedEffect(posicionMiTurno) {
        if (posicionMiTurno == 1 && miTurnoActivo != null) {
            NotificationHelper.enviarNotificacion(
                context = context,
                titulo = "¡Tu turno está próximo!",
                mensaje = "Estás en la posición #1 de la fila con el turno ${miTurnoActivo.numeroTurno}. ¡Prepárate para ingresar al probador!"
            )
        }
    }

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
                text = "Sesión: $username",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            OutlinedButton(
                onClick = onNavigateToLogin,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver al inicio de sesión",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Iniciar Sesión", fontSize = 12.sp)
            }
        }

        // Resumen de estado con números animados
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Probadores Libres", fontSize = 12.sp)
                    AnimatedContent(
                        targetState = probadoresLibres,
                        transitionSpec = {
                            (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                        },
                        label = "ProbadoresLibresAnimation"
                    ) { count ->
                        Text(
                            text = "$count",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "En Fila", fontSize = 12.sp)
                    AnimatedContent(
                        targetState = personasEnFila,
                        transitionSpec = {
                            (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                        },
                        label = "PersonasEnFilaAnimation"
                    ) { count ->
                        Text(
                            text = "$count",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Tiempo Est.", fontSize = 12.sp)
                    AnimatedContent(
                        targetState = tiempoEstimado,
                        transitionSpec = {
                            (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                        },
                        label = "TiempoEstimadoAnimation"
                    ) { tiempo ->
                        Text(
                            text = "~ $tiempo min",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Si el cliente ya tiene un turno activo, muestra una tarjeta informativa animada
        AnimatedVisibility(
            visible = miTurnoActivo != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            miTurnoActivo?.let { turno ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.HourglassTop,
                            contentDescription = "Turno Pendiente",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = "Turno Activo en Espera",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ya posees el turno ${turno.numeroTurno} en la posición #$posicionMiTurno.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Debes ser atendido antes de poder solicitar un nuevo turno.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Formulario Solicitud de Turno
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Solicitar Turno",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                AnimatedVisibility(
                    visible = turnoMensaje != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    turnoMensaje?.let { (exito, msg) ->
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
                }

                OutlinedTextField(
                    value = cantidadPrendasText,
                    onValueChange = {
                        cantidadPrendasText = it
                        prendasError = null
                        turnoMensaje = null
                    },
                    label = { Text("Cantidad de prendas (1 - 6)") },
                    isError = prendasError != null,
                    supportingText = prendasError?.let { err -> { Text(err) } },
                    trailingIcon = if (prendasError != null) {
                        { Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = miTurnoActivo == null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = esPreferente,
                        onCheckedChange = { esPreferente = it },
                        enabled = miTurnoActivo == null
                    )
                    Text(text = "Atención Preferencial (Adulto mayor, embarazada, etc.)")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val err = viewModel.validarCantidadPrendas(cantidadPrendasText)
                        prendasError = err

                        if (err == null) {
                            val prendas = cantidadPrendasText.trim().toInt()
                            viewModel.registrarTurno(
                                nombreCliente = username,
                                cantidadPrendas = prendas,
                                esPreferente = esPreferente
                            ) { exito, mensaje ->
                                turnoMensaje = Pair(exito, mensaje)
                                if (exito) {
                                    cantidadPrendasText = ""
                                    esPreferente = false
                                    // Notificación y vibración al registrar turno exitosamente
                                    NotificationHelper.enviarNotificacion(
                                        context = context,
                                        titulo = "¡Turno Solicitado!",
                                        mensaje = "Se ha generado tu turno con éxito. Revisa tu posición en la fila."
                                    )
                                } else {
                                    NotificationHelper.vibrar(context, 400L)
                                }
                            }
                        } else {
                            NotificationHelper.vibrar(context, 200L)
                        }
                    },
                    enabled = miTurnoActivo == null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (miTurnoActivo == null) "Solicitar Turno" else "Turno Activo Pendiente")
                }
            }
        }

        Text(
            text = "Fila de Espera",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(turnosEnEspera, key = { _, turno -> turno.id }) { index, turno ->
                Box(modifier = Modifier.animateItem()) {
                    ItemTurno(
                        turno = turno,
                        lugar = index + 1,
                        esMiTurno = turno.id == miTurnoActivo?.id
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemTurno(turno: TurnoEntity, lugar: Int, esMiTurno: Boolean) {
    val targetBackgroundColor = if (esMiTurno) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(durationMillis = 400),
        label = "TurnoColorAnimation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = animatedBgColor),
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
                color = if (esMiTurno) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = turno.nombreCliente,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (esMiTurno) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "Tu turno",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
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
