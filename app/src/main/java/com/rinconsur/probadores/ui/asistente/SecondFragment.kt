package com.rinconsur.probadores.ui.asistente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rinconsur.probadores.R
import com.rinconsur.probadores.databinding.FragmentSecondBinding
import com.rinconsur.probadores.ui.cliente.TurnoAdapter

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AsistenteViewModel by activityViewModels()

    private val probadorAdapter = ProbadorAdapter { probador, nuevoEstado ->
        // Actualiza el estado del probador (LIBRE, OCUPADO, LIMPIEZA)
        viewModel.cambiarEstadoProbador(probador, nuevoEstado)
    }

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

        // Botón para ir a la vista cliente si es necesario
        binding.btnVistaCliente.setOnClickListener {
            val username = arguments?.getString("USERNAME") ?: "Asistente"
            val bundle = Bundle().apply {
                putString("USERNAME", username)
            }
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment, bundle)
        }
    }

    private fun setupRecyclerView() {
        // Configura el RecyclerView de Probadores / Camarines
        binding.rvProbadores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProbadores.adapter = probadorAdapter
    }

    private fun observeViewModel() {
        // Observa la lista de camarines/probadores y la actualiza reactivamente
        viewModel.probadoresLiveData.observe(viewLifecycleOwner) { listaProbadores ->
            probadorAdapter.submitList(listaProbadores)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}