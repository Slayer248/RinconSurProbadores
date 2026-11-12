package com.rinconsur.probadores.ui.cliente

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
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
import com.rinconsur.probadores.data.local.entities.TurnoEntity
import com.rinconsur.probadores.ui.asistente.AsistenteViewModel

@Composable
fun ClienteScreen(
    username: String,
    viewModel: AsistenteViewModel
) {
    val context = LocalContext.current
    var cantidadPrendasText by remember { mutableStateOf("") }
    var esPreferente by remember { mutableStateOf(false) }

    val probadores by viewModel.probadoresLiveData.observeAsState(initial = emptyList())
    val turnosEnEspera by viewModel.turnosEnEsperaLiveData.observeAsState(initial = emptyList())

    val probadoresLibres = remember(probadores) { probadores.count { it.estado == "LIBRE" } }
    val personasEnFila = turnosEnEspera.size
    val tiempoEstimado = if (personasEnFila == 0) 0 else personasEnFila * 2

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

                OutlinedTextField(
                    value = cantidadPrendasText,
                    onValueChange = { cantidadPrendasText = it },
                    label = { Text("Cantidad de prendas (1 - 6)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = esPreferente,
                        onCheckedChange = { esPreferente = it }
                    )
                    Text(text = "Atención Preferencial (Adulto mayor, embarazada, etc.)")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val prendas = cantidadPrendasText.toIntOrNull() ?: 0
                        if (prendas < 1 || prendas > 6) {
                            Toast.makeText(context, "La cantidad de prendas debe ser entre 1 y 6", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        viewModel.registrarTurno(
                            nombreCliente = username,
                            cantidadPrendas = prendas,
                            esPreferente = esPreferente
                        )

                        cantidadPrendasText = ""
                        esPreferente = false
                        Toast.makeText(context, "¡Turno registrado para $username!", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Solicitar Turno")
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
                ItemTurno(turno = turno, lugar = index + 1)
            }
        }
    }
}

@Composable
private fun ItemTurno(turno: TurnoEntity, lugar: Int) {
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
