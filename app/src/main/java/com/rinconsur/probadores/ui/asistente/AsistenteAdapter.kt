package com.rinconsur.probadores.ui.asistente

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rinconsur.probadores.data.local.entities.ProbadorEntity
import com.rinconsur.probadores.databinding.ItemProbadorBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AsistenteAdapter(
    private val onAccionClick: (ProbadorEntity) -> Unit
) : ListAdapter<ProbadorEntity, AsistenteAdapter.ProbadorViewHolder>(ProbadorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProbadorViewHolder {
        val binding = ItemProbadorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProbadorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProbadorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProbadorViewHolder(
        private val binding: ItemProbadorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(probador: ProbadorEntity) {
            binding.tvCodigoProbador.text = probador.codigo
            binding.tvTipoProbador.text = if (probador.esAccesible) "Accesible (P-08)" else "Estándar"
            binding.tvEstadoProbador.text = probador.estado

            // Formatear desdeHora si viene como String u otro valor
            binding.tvDesdeHora.text = "Desde: ${probador.desdeHora}"

            // Transiciones de estado (RN-01)
            binding.btnAccionProbador.text = when (probador.estado) {
                "LIBRE" -> "Ocupar"
                "OCUPADO" -> "Liberar / Limpiar"
                "ORDENAMIENTO" -> "Marcar Listo"
                else -> "Bloqueado"
            }

            binding.btnAccionProbador.isEnabled = probador.estado != "FUERA_SERVICIO"

            binding.btnAccionProbador.setOnClickListener {
                onAccionClick(probador)
            }
        }
    }

    class ProbadorDiffCallback : DiffUtil.ItemCallback<ProbadorEntity>() {
        override fun areItemsTheSame(oldItem: ProbadorEntity, newItem: ProbadorEntity): Boolean {
            return oldItem.codigo == newItem.codigo
        }

        override fun areContentsTheSame(oldItem: ProbadorEntity, newItem: ProbadorEntity): Boolean {
            return oldItem == newItem
        }
    }
}