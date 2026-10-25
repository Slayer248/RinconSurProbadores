package com.rinconsur.probadores.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.rinconsur.probadores.R
import com.rinconsur.probadores.databinding.FragmentLoginBinding
import com.rinconsur.probadores.ui.asistente.AsistenteViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AsistenteViewModel by activityViewModels()
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

        binding.btnIngresarInvitado.setOnClickListener {
            esModoInvitado = !esModoInvitado
            actualizarInterfaz()
        }

        binding.btnIniciarSesion.setOnClickListener {
            val usuarioInput = binding.etRut.text.toString().trim()

            if (esModoInvitado) {
                if (usuarioInput.isEmpty()) {
                    Toast.makeText(requireContext(), "Por favor ingresa un nombre para continuar", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                Toast.makeText(requireContext(), "Bienvenido/a $usuarioInput", Toast.LENGTH_SHORT).show()
                val bundle = Bundle().apply { putString("USERNAME", usuarioInput) }
                findNavController().navigate(R.id.action_loginFragment_to_FirstFragment, bundle)

            } else {
                val passInput = binding.etContrasena.text.toString().trim()

                if (usuarioInput.isEmpty() || passInput.isEmpty()) {
                    Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                viewModel.autenticar(usuarioInput, passInput) { rol ->
                    activity?.runOnUiThread {
                        when (rol) {
                            "ADMIN" -> {
                                Toast.makeText(requireContext(), "Bienvenido Administrador", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_loginFragment_to_AdminFragment)
                            }
                            "ASISTENTE" -> {
                                Toast.makeText(requireContext(), "Bienvenido Asistente", Toast.LENGTH_SHORT).show()
                                val bundle = Bundle().apply { putString("USERNAME", usuarioInput) }
                                findNavController().navigate(R.id.action_loginFragment_to_SecondFragment, bundle)
                            }
                            else -> {
                                Toast.makeText(requireContext(), "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun actualizarInterfaz() {
        if (esModoInvitado) {
            binding.tilContrasena.visibility = View.GONE
            binding.tilRut.hint = "Nombre de Invitado"
            binding.btnIniciarSesion.text = "Ingresar a Probadores"
            binding.btnIngresarInvitado.text = "Volver a Inicio de Sesión"
            binding.etRut.text?.clear()
        } else {
            binding.tilContrasena.visibility = View.VISIBLE
            binding.tilRut.hint = "Nombre de Usuario"
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