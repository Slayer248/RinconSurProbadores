package com.rinconsur.probadores.ui.cliente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.rinconsur.probadores.databinding.FragmentFirstBinding
import com.rinconsur.probadores.ui.asistente.AsistenteViewModel

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    // ViewModel compartido para mantener los datos sincronizados en tiempo real
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

        // Indicar que se modificará el menú superior para ocultar opciones
        setHasOptionsMenu(true)

        // Ocultar botón de acceso al panel de operaciones para vistas de invitado/cliente
        binding.buttonFirst.visibility = View.GONE

        // Iniciar la escucha de datos desde Room/LiveData
        observeViewModel()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        // Oculta el menú de tres puntos para el cliente/invitado
        menu.clear()
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