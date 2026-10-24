package com.rinconsur.probadores.ui.cliente

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rinconsur.probadores.R
import com.rinconsur.probadores.data.local.entities.TurnoEntity

class TurnoAdapter : RecyclerView.Adapter<TurnoAdapter.TurnoViewHolder>() {

    private var listaTurnos = listOf<TurnoEntity>()

    fun submitList(nuevosTurnos: List<TurnoEntity>) {
        listaTurnos = nuevosTurnos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TurnoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_turno_espera, parent, false)
        return TurnoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TurnoViewHolder, position: Int) {
        holder.bind(listaTurnos[position], position + 1)
    }

    override fun getItemCount(): Int = listaTurnos.size

    class TurnoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPosicion: TextView = itemView.findViewById(R.id.tvPosicion)
        private val tvNombreCliente: TextView = itemView.findViewById(R.id.tvNombreCliente)
        private val tvDetalle: TextView = itemView.findViewById(R.id.tvDetalle)

        fun bind(turno: TurnoEntity, lugar: Int) {
            tvPosicion.text = "#$lugar"
            tvNombreCliente.text = turno.nombreCliente
            val preferenteText = if (turno.esPreferente) " • Preferencial" else ""
            tvDetalle.text = "${turno.numeroTurno} • ${turno.cantidadPrendas} prendas$preferenteText"
        }
    }
}