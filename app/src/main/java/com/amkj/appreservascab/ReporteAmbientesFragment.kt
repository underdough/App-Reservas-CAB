package com.amkj.appreservascab

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amkj.appreservascab.databinding.FragmentListaReporteBinding
import com.amkj.appreservascab.servicios.ConexionDB
import com.amkj.appreservascab.servicios.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReporteAmbientesFragment : Fragment(R.layout.fragment_lista_reporte) {

    private var _binding: FragmentListaReporteBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ReportAdapter

    private val api: ConexionDB.ReporteService by lazy { RetrofitClient.reporteService }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentListaReporteBinding.bind(view)

        adapter = ReportAdapter(tipoFijo = "Ambiente")
        binding.rvReporte.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReporte.adapter = adapter

        // 1) Carga inicial (mes actual) por si la Activity aún no envía el rango
        val (ini, fin) = rangoMesActual()
        cargar(ini, fin)

        // 2) Escucha el rango enviado por InformesActivity (botón Cargar)
        parentFragmentManager.setFragmentResultListener("RANGO_INFORMES", viewLifecycleOwner) { _, result ->
            val inicio = result.getString("inicio") ?: return@setFragmentResultListener
            val finSel = result.getString("fin") ?: return@setFragmentResultListener
            cargar(inicio, finSel)
        }
    }

    private fun cargar(inicio: String, fin: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.pbCargando.visibility = View.VISIBLE
                val res = api.getTopReservas(inicio, fin, 10)
                if (res.isSuccessful) {
                    val body = res.body()
                    if (body != null) {
                        adapter.submit(body.ambientes_top)
                        binding.tvVacio.visibility =
                            if (body.ambientes_top.isEmpty()) View.VISIBLE else View.GONE
                    } else {
                        binding.tvVacio.text = "Respuesta vacía"
                        binding.tvVacio.visibility = View.VISIBLE
                    }
                } else {
                    binding.tvVacio.text = "Error ${res.code()}"
                    binding.tvVacio.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                binding.tvVacio.text = "Error: ${e.message}"
                binding.tvVacio.visibility = View.VISIBLE
            } finally {
                binding.pbCargando.visibility = View.GONE
            }
        }
    }

    // Utilidad para rango por defecto (mes actual)
    private fun rangoMesActual(): Pair<String, String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val fin = sdf.format(cal.time)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val ini = sdf.format(cal.time)
        return Pair(ini, fin)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
