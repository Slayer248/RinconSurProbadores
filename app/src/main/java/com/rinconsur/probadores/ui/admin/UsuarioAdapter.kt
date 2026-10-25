package com.rinconsur.probadores.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rinconsur.probadores.R
import com.rinconsur.probadores.data.local.entities.UsuarioEntity

class UsuarioAdapter(
    private val onUsuarioClick: (UsuarioEntity) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    private var listaUsuarios = listOf<UsuarioEntity>()

    fun submitList(nuevosUsuarios: List<UsuarioEntity>) {
        listaUsuarios = nuevosUsuarios
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = listaUsuarios[position]
        holder.bind(usuario)
        holder.itemView.setOnClickListener {
            onUsuarioClick(usuario)
        }
    }

    override fun getItemCount(): Int = listaUsuarios.size

    class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombreUsuario)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsernameUsuario)
        private val tvRol: TextView = itemView.findViewById(R.id.tvRolUsuario)

        fun bind(usuario: UsuarioEntity) {
            tvNombre.text = usuario.nombre
            tvUsername.text = "Usuario: ${usuario.username}"
            tvRol.text = usuario.rol
        }
    }
}