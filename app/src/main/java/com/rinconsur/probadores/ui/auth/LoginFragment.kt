package com.rinconsur.probadores.ui.auth

import android.os.Bundle
import android.text.InputFilter
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Filtro: Solo permite ingresar letras y espacios en tiempo real
        val filtroSoloLetras = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                val c = source[i]
                if (!Character.isLetter(c) && !Character.isWhitespace(c)) {
                    return@InputFilter ""
                }
            }
            null
        }

        // Aplicar el filtro al campo de invitado
        binding.etNombreInvitado?.filters = arrayOf(filtroSoloLetras)

        // 1. Botón Ingresar como Invitado
        binding.btnIngresarInvitado?.setOnClickListener {
            val nombreInvitado = binding.etNombreInvitado?.text.toString().trim()

            if (nombreInvitado.isEmpty()) {
                Toast.makeText(requireContext(), "Ingresa tu nombre para continuar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validación: Mínimo 5 letras
            if (nombreInvitado.length < 3) {
                Toast.makeText(requireContext(), "El nombre de invitado debe tener mínimo 3 letras", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Bloquea el avance si tiene menos de 3 letras
            }

            val bundle = Bundle().apply {
                putString("USERNAME", nombreInvitado)
            }
            findNavController().navigate(R.id.action_loginFragment_to_FirstFragment, bundle)
        }

        // 2. Botón Iniciar Sesión (Asistente / Admin)
        binding.btnIniciarSesion?.setOnClickListener {
            val user = binding.etUsername?.text.toString().trim()
            val pass = binding.etPassword?.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (user.length < 3) {
                Toast.makeText(requireContext(), "El usuario debe tener mínimo 3 letras", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 8) {
                Toast.makeText(requireContext(), "La contraseña debe tener mínimo 8 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.autenticar(user, pass) { rol ->
                activity?.runOnUiThread {
                    when (rol) {
                        "ADMIN" -> {
                            val bundle = Bundle().apply { putString("USERNAME", user) }
                            findNavController().navigate(R.id.action_loginFragment_to_AdminFragment, bundle)
                        }
                        "ASISTENTE" -> {
                            val bundle = Bundle().apply { putString("USERNAME", user) }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}