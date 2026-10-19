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
import com.rinconsur.probadores.databinding.FragmentSecondBinding

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    // Compartimos la misma instancia de ViewModel que FirstFragment
    private val viewModel: AsistenteViewModel by activityViewModels()
    private lateinit var adapter: AsistenteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnVolverCliente.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnAgregarFila.setOnClickListener {
            val inputPrendas = binding.etPrendas.text.toString().trim()
            val cantPrendas = inputPrendas.toIntOrNull()

            when {
                cantPrendas == null || cantPrendas < 1 -> {
                    Toast.makeText(
                        requireContext(),
                        "Ingresa una cantidad válida (mínimo 1 prenda)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                cantPrendas > 6 -> {
                    Toast.makeText(
                        requireContext(),
                        "Límite superado: Máximo 6 prendas por turno",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    val esPreferente = binding.cbPreferente.isChecked
                    viewModel.registrarTurno(cantPrendas, esPreferente)

                    binding.etPrendas.setText("")
                    binding.cbPreferente.isChecked = false
                    Toast.makeText(
                        requireContext(),
                        "Turno registrado con éxito ($cantPrendas prendas)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = AsistenteAdapter { probador ->
            val nuevoEstado = when (probador.estado) {
                "LIBRE" -> "OCUPADO"
                "OCUPADO" -> "ORDENAMIENTO"
                "ORDENAMIENTO" -> "LIBRE"
                else -> probador.estado
            }

            if (nuevoEstado != probador.estado) {
                viewModel.cambiarEstadoProbador(probador, nuevoEstado)
                Toast.makeText(
                    context,
                    "Probador ${probador.codigo} pasó a $nuevoEstado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.rvProbadores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProbadores.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.probadoresLiveData.observe(viewLifecycleOwner) { listaProbadores ->
            adapter.submitList(listaProbadores)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}