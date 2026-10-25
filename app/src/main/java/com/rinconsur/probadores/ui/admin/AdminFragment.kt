package com.rinconsur.probadores.ui.admin

import android.os.Bundle
import android.text.InputFilter
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

        // Filtro para permitir ÚNICAMENTE letras (incluye acentos, ñ y espacios)
        val filtroSoloLetras = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                val c = source[i]
                if (!Character.isLetter(c) && !Character.isWhitespace(c)) {
                    return@InputFilter ""
                }
            }
            null
        }

        // Filtro para Nombre de Usuario (Solo letras, sin espacios)
        val filtroUsernameSoloLetras = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                val c = source[i]
                if (!Character.isLetter(c)) {
                    return@InputFilter ""
                }
            }
            null
        }

        // Aplicar filtros de entrada en tiempo real
        binding.etNombreNuevo.filters = arrayOf(filtroSoloLetras)
        binding.etUsernameNuevo.filters = arrayOf(filtroUsernameSoloLetras)

        // Acceso directo a Asistente
        binding.btnIrVistaAsistente.setOnClickListener {
            val bundle = Bundle().apply { putString("USERNAME", "Admin") }
            findNavController().navigate(R.id.action_AdminFragment_to_SecondFragment, bundle)
        }

        // Botón Volver Atrás
        binding.btnVolverAtras.setOnClickListener {
            findNavController().navigateUp()
        }

        // Crear Cuenta con Validaciones de Longitud
        binding.btnCrearCuenta.setOnClickListener {
            val nombre = binding.etNombreNuevo.text.toString().trim()
            val username = binding.etUsernameNuevo.text.toString().trim()
            val pass = binding.etPassNuevo.text.toString().trim()
            val rol = if (binding.rbAdmin.isChecked) "ADMIN" else "ASISTENTE"

            // Validation : Campos vacíos
            if (nombre.isEmpty() || username.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validation : Mínimo 3 letras para Username
            if (username.length < 3) {
                Toast.makeText(requireContext(), "El nombre de usuario debe tener mínimo 3 letras", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validation : Mínimo 8 caracteres para Contraseña
            if (pass.length < 8) {
                Toast.makeText(requireContext(), "La contraseña debe tener mínimo 8 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Registro exitoso si cumple con las condiciones
            viewModel.crearUsuario(nombre, username, pass, rol) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Cuenta registrada con éxito para $username", Toast.LENGTH_SHORT).show()
                    binding.etNombreNuevo.text?.clear()
                    binding.etUsernameNuevo.text?.clear()
                    binding.etPassNuevo.text?.clear()
                }
            }
        }

        // Observar cuentas y filtrar para mostrar solo ASISTENTEs
        viewModel.usuariosLiveData.observe(viewLifecycleOwner) { listaCompleta ->
            val soloAsistentes = listaCompleta.filter { it.rol == "ASISTENTE" }
            usuarioAdapter.submitList(soloAsistentes)
        }
    }

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