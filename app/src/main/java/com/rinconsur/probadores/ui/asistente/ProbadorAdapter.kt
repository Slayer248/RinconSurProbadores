package com.rinconsur.probadores.ui.asistente

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rinconsur.probadores.R
import com.rinconsur.probadores.data.local.entities.ProbadorEntity

class ProbadorAdapter(
    private val onCambiarEstadoClick: (ProbadorEntity, String) -> Unit
) : RecyclerView.Adapter<ProbadorAdapter.ProbadorViewHolder>() {

    private var listaProbadores = listOf<ProbadorEntity>()

    fun submitList(nuevaLista: List<ProbadorEntity>) {
        listaProbadores = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProbadorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_probador, parent, false)
        return ProbadorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProbadorViewHolder, position: Int) {
        val probador = listaProbadores[position]
        holder.bind(probador, onCambiarEstadoClick)
    }

    override fun getItemCount(): Int = listaProbadores.size

    class ProbadorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCodigo: TextView = itemView.findViewById(R.id.tvCodigoProbador)
        private val tvTipo: TextView = itemView.findViewById(R.id.tvTipoProbador)
        private val tvEstado: TextView = itemView.findViewById(R.id.tvEstadoProbador)
        private val tvDesdeHora: TextView = itemView.findViewById(R.id.tvDesdeHora)
        private val btnAccion: Button = itemView.findViewById(R.id.btnAccionProbador)

        fun bind(
            probador: ProbadorEntity,
            onCambiarEstadoClick: (ProbadorEntity, String) -> Unit
        ) {
            tvCodigo.text = "Probador: ${probador.codigo}"
            tvTipo.text = if (probador.esAccesible) "Tipo: Preferencial / Accesible" else "Tipo: Estándar"
            tvEstado.text = "Estado: ${probador.estado}"
            tvDesdeHora.text = "Ocupado desde: ${probador.desdeHora}"

            // Determinar ciclo de estados
            val siguienteEstado = when (probador.estado) {
                "LIBRE" -> "OCUPADO"
                "OCUPADO" -> "LIMPIEZA"
                else -> "LIBRE"
            }

            btnAccion.text = "Cambiar a $siguienteEstado"
            btnAccion.setOnClickListener {
                onCambiarEstadoClick(probador, siguienteEstado)
            }
        }
    }
}