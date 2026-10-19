package com.rinconsur.probadores.ui.cliente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.rinconsur.probadores.R
import com.rinconsur.probadores.databinding.FragmentFirstBinding
import com.rinconsur.probadores.ui.asistente.AsistenteViewModel

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    // Usamos activityViewModels para mantener los datos en toda la app
    private val viewModel: AsistenteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    private fun observeViewModel() {
        // Escuchar cambios en probadores libres
        viewModel.probadoresLiveData.observe(viewLifecycleOwner) { listaProbadores ->
            val libres = listaProbadores.count { it.estado == "LIBRE" }
            binding.tvProbadoresLibres.text = libres.toString()
        }

        // Escuchar personas en fila y actualizar tiempo estimado
        viewModel.turnosEnEsperaLiveData.observe(viewLifecycleOwner) { listaEnEspera ->
            val enFila = listaEnEspera.size
            binding.tvPersonasFila.text = enFila.toString()

            val tiempoEstimado = if (enFila == 0) 0 else enFila * 2
            binding.tvTiempoEspera.text = "~ $tiempoEstimado min"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}