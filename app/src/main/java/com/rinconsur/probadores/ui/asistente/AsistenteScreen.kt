package com.rinconsur.probadores.ui.asistente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rinconsur.probadores.data.local.entities.ProbadorEntity

@Composable
fun AsistenteScreen(
    username: String,
    viewModel: AsistenteViewModel,
    onNavigateToCliente: (String) -> Unit
) {
    val probadores by viewModel.probadoresLiveData.observeAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
                    text = "Usuario: $username",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = { onNavigateToCliente(username) }
            ) {
                Text("Vista Cliente")
            }
        }

        Text(
            text = "Estado de Probadores",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(probadores, key = { it.codigo }) { probador ->
                ItemProbador(
                    probador = probador,
                    onCambiarEstado = { p, nuevoEstado ->
                        viewModel.cambiarEstadoProbador(p, nuevoEstado)
                    }
                )
            }
        }
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Probador: ${probador.codigo}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = probador.estado,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (probador.estado) {
                        "LIBRE" -> MaterialTheme.colorScheme.primary
                        "OCUPADO" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.tertiary
                    }
                )
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

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onCambiarEstado(probador, siguienteEstado) },
                enabled = probador.estado != "FUERA_SERVICIO",
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cambiar a $siguienteEstado")
            }
        }
    }
}
