package com.rinconsur.probadores.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rinconsur.probadores.R
import com.rinconsur.probadores.data.local.entities.UsuarioEntity
import com.rinconsur.probadores.databinding.FragmentAdminBinding
import com.rinconsur.probadores.ui.asistente.AsistenteViewModel

class AdminFragment : Fragment() {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AsistenteViewModel by activityViewModels()

    // Configuración del adapter con listener para mostrar datos al hacer clic
    private val usuarioAdapter = UsuarioAdapter { usuario ->
        mostrarDetalleCuenta(usuario)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvUsuarios.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsuarios.adapter = usuarioAdapter

        // Acceso directo a Asistente
        binding.btnIrVistaAsistente.setOnClickListener {
            val bundle = Bundle().apply { putString("USERNAME", "Admin") }
            findNavController().navigate(R.id.action_AdminFragment_to_SecondFragment, bundle)
        }

        // Botón Volver Atrás
        binding.btnVolverAtras.setOnClickListener {
            findNavController().navigateUp()
        }

        // Botón Cerrar Sesión
        binding.btnCerrarSesion.setOnClickListener {
            findNavController().popBackStack(R.id.loginFragment, false)
        }

        // Crear Cuenta
        binding.btnCrearCuenta.setOnClickListener {
            val nombre = binding.etNombreNuevo.text.toString().trim()
            val username = binding.etUsernameNuevo.text.toString().trim()
            val pass = binding.etPassNuevo.text.toString().trim()
            val rol = if (binding.rbAdmin.isChecked) "ADMIN" else "ASISTENTE"

            if (nombre.isEmpty() || username.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.crearUsuario(nombre, username, pass, rol) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Cuenta creada para $username", Toast.LENGTH_SHORT).show()
                    binding.etNombreNuevo.text?.clear()
                    binding.etUsernameNuevo.text?.clear()
                    binding.etPassNuevo.text?.clear()
                }
            }
        }

        // Observar cuentas y FILTRAR excluyendo las de otros Administradores
        viewModel.usuariosLiveData.observe(viewLifecycleOwner) { listaCompleta ->
            val soloAsistentes = listaCompleta.filter { it.rol == "ASISTENTE" }
            usuarioAdapter.submitList(soloAsistentes)
        }
    }

    // Modal para visualizar Nombre, Username y Contraseña al presionar sobre la cuenta
    private fun mostrarDetalleCuenta(usuario: UsuarioEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Información de la Cuenta")
            .setMessage(
                """
                Nombre: ${usuario.nombre}
                Username: ${usuario.username}
                Contraseña: ${usuario.contrasena}
                Rol: ${usuario.rol}
                """.trimIndent()
            )
            .setPositiveButton("Aceptar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}