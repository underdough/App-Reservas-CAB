package com.amkj.appreservascab

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.amkj.appreservascab.databinding.ActivityInformesBinding
import com.google.android.material.tabs.TabLayoutMediator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InformesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInformesBinding

    // Fechas en formato "YYYY-MM-DD"
    private var fechaInicio: String = ""
    private var fechaFin: String = ""

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicial: mes actual → hoy
        val (ini, fin) = rangoMesActual()
        fechaInicio = ini
        fechaFin = fin
        binding.btnInicio.text = fechaInicio
        binding.btnFin.text = fechaFin

        // ViewPager + Tabs
        binding.viewPagerInformes.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2
            override fun createFragment(position: Int) =
                if (position == 0) ReporteAmbientesFragment() else ReporteEquiposFragment()
        }
        TabLayoutMediator(binding.tabLayoutInformes, binding.viewPagerInformes) { tab, pos ->
            tab.text = if (pos == 0) "Ambientes" else "Equipos"
        }.attach()

        // Pickers
        binding.btnInicio.setOnClickListener { abrirDatePicker(true) }
        binding.btnFin.setOnClickListener { abrirDatePicker(false) }

        // Cargar: envía el rango a los fragments
        binding.btnCargar.setOnClickListener {
            emitirRangoAFragments(fechaInicio, fechaFin)
        }

        // Emitir rango inicial para que carguen al abrir
        binding.btnCargar.post {
            emitirRangoAFragments(fechaInicio, fechaFin)
        }
    }

    private fun abrirDatePicker(esInicio: Boolean) {
        // Usar la fecha actual del botón como base
        val cal = Calendar.getInstance()
        val texto = if (esInicio) binding.btnInicio.text.toString() else binding.btnFin.text.toString()
        runCatching {
            cal.time = sdf.parse(texto) ?: Calendar.getInstance().time
        }

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val dlg = DatePickerDialog(this, { _, y, m, d ->
            val picked = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0) }
            val valor = sdf.format(picked.time)
            if (esInicio) {
                fechaInicio = valor
                binding.btnInicio.text = valor
            } else {
                fechaFin = valor
                binding.btnFin.text = valor
            }
        }, year, month, day)

        dlg.show()
    }

    private fun rangoMesActual(): Pair<String, String> {
        val cal = Calendar.getInstance()
        val fin = sdf.format(cal.time) // hoy
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val ini = sdf.format(cal.time)  // primer día del mes
        return Pair(ini, fin)
    }

    /** Envia el rango seleccionado a ambos fragments mediante FragmentResult */
    private fun emitirRangoAFragments(inicio: String, fin: String) {
        val fm = supportFragmentManager
        val bundle = android.os.Bundle().apply {
            putString("inicio", inicio)
            putString("fin", fin)
        }
        fm.setFragmentResult("RANGO_INFORMES", bundle)
        // feedback visual opcional
        binding.btnCargar.isEnabled = false
        binding.btnCargar.text = "Cargando..."
        binding.btnCargar.postDelayed({
            binding.btnCargar.isEnabled = true
            binding.btnCargar.text = "Cargar"
        }, 400L)
    }
}
