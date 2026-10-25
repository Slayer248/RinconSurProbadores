package com.rinconsur.probadores.ui.asistente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rinconsur.probadores.R
import com.rinconsur.probadores.databinding.FragmentSecondBinding

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AsistenteViewModel by activityViewModels()
    private lateinit var probadorAdapter: ProbadorAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración del adapter y RecyclerView
        probadorAdapter = ProbadorAdapter { probador, nuevoEstado ->
            viewModel.cambiarEstadoProbador(probador, nuevoEstado)
        }

        binding.rvProbadores.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = probadorAdapter
            isNestedScrollingEnabled = false // Permite dibujarse correctamente dentro de NestedScrollView
        }

        // Observar camarines en tiempo real
        viewModel.probadoresLiveData.observe(viewLifecycleOwner) { listaProbadores ->
            if (listaProbadores != null && listaProbadores.isNotEmpty()) {
                probadorAdapter.submitList(listaProbadores)
            }
        }

        // Registrar turno manual
        binding.btnAgregarTurno.setOnClickListener {
            val prendasStr = binding.etPrendas.text.toString().trim()
            val esPreferente = binding.cbPreferente.isChecked

            if (prendasStr.isEmpty()) {
                Toast.makeText(requireContext(), "Ingresa la cantidad de prendas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prendas = prendasStr.toIntOrNull() ?: 1
            viewModel.registrarTurno(
                nombreCliente = "Cliente Fila",
                cantidadPrendas = prendas,
                esPreferente = esPreferente
            )

            binding.etPrendas.text?.clear()
            binding.cbPreferente.isChecked = false
            Toast.makeText(requireContext(), "Turno registrado", Toast.LENGTH_SHORT).show()
        }

        // Ir a Vista Cliente
        binding.btnVistaCliente.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}