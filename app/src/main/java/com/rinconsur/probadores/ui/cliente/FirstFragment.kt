package com.rinconsur.probadores.ui.cliente

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rinconsur.probadores.databinding.FragmentFirstBinding
import com.rinconsur.probadores.ui.asistente.AsistenteViewModel

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AsistenteViewModel by activityViewModels()
    private val turnoAdapter = TurnoAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        setupRecyclerView()

        // Captura el nombre enviado desde el Login (ej: "Juan Pérez")
        val nombreUsuarioSesion = arguments?.getString("USERNAME")
            ?: arguments?.getString("nombreUsuario")
            ?: "Cliente"

        // Despliega el nombre exacto de la sesión en el banner superior
        binding.tvUsuarioActual.text = "Sesión: $nombreUsuarioSesion"

        // Filtro para asegurar que solo se permitan letras y espacios si existe un campo de texto para el cliente
        val filtroSoloLetras = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                val c = source[i]
                if (!Character.isLetter(c) && !Character.isWhitespace(c)) {
                    return@InputFilter ""
                }
            }
            null
        }

        // Evento solicitar turno
        binding.btnSolicitarTurno.setOnClickListener {
            val prendasText = binding.etCantidadPrendas.text.toString().trim()

            if (prendasText.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor ingresa la cantidad de prendas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prendas = prendasText.toIntOrNull() ?: 0
            if (prendas < 1 || prendas > 6) {
                Toast.makeText(requireContext(), "La cantidad de prendas debe ser entre 1 y 6", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val esPreferente = binding.cbEsPreferente.isChecked

            // Registra el turno utilizando automáticamente el nombre de la sesión
            viewModel.registrarTurno(
                nombreCliente = nombreUsuarioSesion,
                cantidadPrendas = prendas,
                esPreferente = esPreferente
            )

            binding.etCantidadPrendas.text?.clear()
            binding.cbEsPreferente.isChecked = false
            Toast.makeText(requireContext(), "¡Turno registrado para $nombreUsuarioSesion!", Toast.LENGTH_LONG).show()
        }

        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvListaEspera.layoutManager = LinearLayoutManager(requireContext())
        binding.rvListaEspera.adapter = turnoAdapter
        binding.rvListaEspera.isNestedScrollingEnabled = false
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    private fun observeViewModel() {
        viewModel.probadoresLiveData.observe(viewLifecycleOwner) { listaProbadores ->
            val libres = listaProbadores.count { it.estado == "LIBRE" }
            binding.tvProbadoresLibres.text = libres.toString()
        }

        viewModel.turnosEnEsperaLiveData.observe(viewLifecycleOwner) { listaEnEspera ->
            val enFila = listaEnEspera.size
            binding.tvPersonasFila.text = enFila.toString()

            val tiempoEstimado = if (enFila == 0) 0 else enFila * 2
            binding.tvTiempoEspera.text = "~ $tiempoEstimado min"

            turnoAdapter.submitList(listaEnEspera)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}