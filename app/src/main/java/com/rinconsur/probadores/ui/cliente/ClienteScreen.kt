package com.rinconsur.probadores.ui.cliente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rinconsur.probadores.data.local.entities.TurnoEntity
import com.rinconsur.probadores.ui.asistente.AsistenteViewModel

@Composable
fun ClienteScreen(
    username: String,
    viewModel: AsistenteViewModel
) {
    var cantidadPrendasText by remember { mutableStateOf("") }
    var prendasError by remember { mutableStateOf<String?>(null) }
    var esPreferente by remember { mutableStateOf(false) }
    var turnoMensaje by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Sesión: $username",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Resumen de estado
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
                    Text(
                        text = "$probadoresLibres",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "En Fila", fontSize = 12.sp)
                    Text(
                        text = "$personasEnFila",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Tiempo Est.", fontSize = 12.sp)
                    Text(
                        text = "~ $tiempoEstimado min",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Si el cliente ya tiene un turno activo, muestra una tarjeta informativa
        if (miTurnoActivo != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Turno Activo en Espera",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ya posees el turno ${miTurnoActivo.numeroTurno} en la posición #$posicionMiTurno.",
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
                                }
                            }
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
            itemsIndexed(turnosEnEspera) { index, turno ->
                ItemTurno(
                    turno = turno,
                    lugar = index + 1,
                    esMiTurno = turno.id == miTurnoActivo?.id
                )
            }
        }
    }
}

@Composable
private fun ItemTurno(turno: TurnoEntity, lugar: Int, esMiTurno: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (esMiTurno) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)) else CardDefaults.cardColors(),
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
