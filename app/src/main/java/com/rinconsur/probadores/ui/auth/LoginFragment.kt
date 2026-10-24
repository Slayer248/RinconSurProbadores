package com.rinconsur.probadores.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rinconsur.probadores.R
import com.rinconsur.probadores.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // Controla si estamos en modo invitado o modo inicio de sesión
    private var esModoInvitado = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón Secundario: Alterna entre Modo Invitado y Modo Normal
        binding.btnIngresarInvitado.setOnClickListener {
            esModoInvitado = !esModoInvitado
            actualizarInterfaz()
        }

        // Botón Principal: Procesa el ingreso según el modo
        binding.btnIniciarSesion.setOnClickListener {
            val usuarioInput = binding.etRut.text.toString().trim()

            if (esModoInvitado) {
                // Validación para Invitado
                if (usuarioInput.isEmpty()) {
                    Toast.makeText(requireContext(), "Por favor ingresa un nombre para continuar", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                Toast.makeText(requireContext(), "Bienvenido/a $usuarioInput", Toast.LENGTH_SHORT).show()
                // Navegar a la pantalla de clientes (FirstFragment)
                findNavController().navigate(R.id.action_loginFragment_to_FirstFragment)

            } else {
                // Validación para Login Normal (Colaboradores / Admin)
                val passInput = binding.etContrasena.text.toString().trim()

                if (usuarioInput.isEmpty() || passInput.isEmpty()) {
                    Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Aquí va la lógica de validación de credenciales con AsistenteViewModel o UsuarioDao
            }
        }
    }

    private fun actualizarInterfaz() {
        if (esModoInvitado) {
            // Cambios de UI para Modo Invitado
            binding.tilContrasena.visibility = View.GONE
            binding.tilRut.hint = "Nombre de Invitado"
            binding.btnIniciarSesion.text = "Ingresar a Probadores"
            binding.btnIngresarInvitado.text = "Volver a Inicio de Sesión"
            binding.etRut.text?.clear()
        } else {
            // Cambios de UI para Modo Normal
            binding.tilContrasena.visibility = View.VISIBLE
            binding.tilRut.hint = "RUT o Usuario"
            binding.btnIniciarSesion.text = "Iniciar Sesión"
            binding.btnIngresarInvitado.text = "Ingresar como Cliente Invitado"
            binding.etRut.text?.clear()
            binding.etContrasena.text?.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}