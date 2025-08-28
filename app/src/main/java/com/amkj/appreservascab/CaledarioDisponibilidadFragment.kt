    package com.amkj.appreservascab

    import android.app.Activity
    import android.content.Intent
    import android.os.Build
    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import androidx.annotation.RequiresApi
    import androidx.core.content.ContextCompat
    import androidx.core.os.bundleOf
    import androidx.core.view.isVisible
    import androidx.fragment.app.Fragment
    import androidx.lifecycle.lifecycleScope
    import com.amkj.appreservascab.Modelos.DiaEstado
    import com.amkj.appreservascab.databinding.FragmentCaledarioDisponibilidadBinding
    import com.amkj.appreservascab.databinding.ItemDayCalendarBinding
    import com.amkj.appreservascab.servicios.RetrofitClient
    import com.kizitonwose.calendar.core.CalendarDay
    import com.kizitonwose.calendar.core.DayPosition
    import com.kizitonwose.calendar.view.MonthDayBinder
    import com.kizitonwose.calendar.view.ViewContainer
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.withContext
    import java.time.DayOfWeek
    import java.time.LocalDate
    import java.time.YearMonth
    import java.time.format.DateTimeFormatter
    import java.util.Locale

    class CalendarioDisponibilidadFragment : Fragment() {

        companion object {
            const val EXTRA_FECHA = "extra_fecha"
            const val EXTRA_JORNADA = "extra_jornada" // compatibilidad hacia atrás (una)
            const val EXTRA_JORNADAS_LIST = "extra_jornadas_list" // ← NUEVO (lista M/T/N)


            fun newInstance(tipo: String, recursoId: Int, returnResult: Boolean = false) =
                CalendarioDisponibilidadFragment().apply {
                    arguments = bundleOf(
                        "tipo" to tipo,
                        "recursoId" to recursoId,
                        "returnResult" to returnResult
                    )
                }
        }

        // -------- Binding --------
        private var _binding: FragmentCaledarioDisponibilidadBinding? = null
        private val binding get() = _binding!!

        // -------- API --------
        private val api by lazy { RetrofitClient.servicioApi }

        // -------- Args --------
        private val tipo: String by lazy { requireArguments().getString("tipo") ?: "ambiente" }
        private val recursoId: Int by lazy { requireArguments().getInt("recursoId") }
        private val returnResult: Boolean by lazy { requireArguments().getBoolean("returnResult", false) }

        // Cache por mes: YearMonth -> (LocalDate -> DiaEstado)
        private val cacheMes = mutableMapOf<YearMonth, Map<LocalDate, DiaEstado>>()

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            _binding = FragmentCaledarioDisponibilidadBinding.inflate(inflater, container, false)
            return binding.root
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // Layout de cada día del calendario
            binding.calendarView.dayViewResource = R.layout.item_day_calendar

            val currentMonth = YearMonth.now()
            val startMonth = currentMonth.minusMonths(12)
            val endMonth = currentMonth.plusMonths(12)

            binding.calendarView.setup(startMonth, endMonth, DayOfWeek.MONDAY)
            binding.calendarView.scrollToMonth(currentMonth)
            binding.tvMonthTitle.text = formatMonth(currentMonth)

            // Navegación de mes
            binding.btnPrevMonth.setOnClickListener {
                binding.calendarView.findFirstVisibleMonth()?.let { vm ->
                    binding.calendarView.smoothScrollToMonth(vm.yearMonth.minusMonths(1))
                }
            }
            binding.btnNextMonth.setOnClickListener {
                binding.calendarView.findFirstVisibleMonth()?.let { vm ->
                    binding.calendarView.smoothScrollToMonth(vm.yearMonth.plusMonths(1))
                }
            }

            // Binder del día con view binding del item
            binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, day: CalendarDay) = container.bind(day)
            }

            // Cuando cambia el mes visible, cargamos si falta
            binding.calendarView.monthScrollListener = { month ->
                binding.tvMonthTitle.text = formatMonth(month.yearMonth)
                viewLifecycleOwner.lifecycleScope.launch { cargarMes(month.yearMonth) }
            }

            // Cargar el mes inicial
            viewLifecycleOwner.lifecycleScope.launch { cargarMes(currentMonth) }
        }

        // -------- ViewContainer de cada celda --------
        @RequiresApi(Build.VERSION_CODES.O)
        inner class DayViewContainer(view: View) : ViewContainer(view) {
            private val item = ItemDayCalendarBinding.bind(view)
            private lateinit var day: CalendarDay

            init {
                view.setOnClickListener {
                    if (this::day.isInitialized && day.position == DayPosition.MonthDate) {
                        onDayClicked(day.date)
                    }
                }
            }

            @RequiresApi(Build.VERSION_CODES.O)
            fun bind(day: CalendarDay) {
                this.day = day
                item.tvDayNumber.text = day.date.dayOfMonth.toString()

                val enMes = day.position == DayPosition.MonthDate
                item.tvDayNumber.alpha = if (enMes) 1f else 0.35f

                val estado = cacheMes[day.date.toYearMonth()]?.get(day.date)

                fun col(res: Int) = ContextCompat.getColor(requireContext(), res)
                fun colorFor(s: String?) = when (s) {
                    "ocupado" -> col(R.color.ocupado)
                    "pendiente" -> col(R.color.pendiente)
                    "libre" -> col(R.color.disponible)
                    else -> 0xFFB0BEC5.toInt() // gris
                }

                val cM = if (enMes && estado != null) colorFor(estado.M) else 0xFFB0BEC5.toInt()
                val cT = if (enMes && estado != null) colorFor(estado.T) else 0xFFB0BEC5.toInt()
                val cN = if (enMes && estado != null) colorFor(estado.N) else 0xFFB0BEC5.toInt()

                item.pillM.background.setTint(cM)
                item.pillT.background.setTint(cT)
                item.pillN.background.setTint(cN)
            }
        }

        // -------- Carga con ProgressBar --------
        @RequiresApi(Build.VERSION_CODES.O)
        private suspend fun cargarMes(ym: YearMonth) {
            if (cacheMes.containsKey(ym)) {
                binding.calendarView.notifyMonthChanged(ym)
                return
            }
            showProgress(true)
            try {
                val inicio = ym.atDay(1).toString()
                val fin = ym.atEndOfMonth().toString()

                if (tipo == "equipo") {
                    // (igual que ya tenías para equipos)
                    val resp = withContext(Dispatchers.IO) {
                        RetrofitClient.servicioApi
                            .disponibilidadEquipoRango(recursoId, inicio, fin)
                            .execute()
                    }
                    if (resp.isSuccessful && resp.body()?.ok == true) {
                        val mapaRemoto = resp.body()!!.mapa // Map<String, EstadoDiaCod>
                        val mapaLocal: Map<LocalDate, DiaEstado> =
                            mapaRemoto.mapKeys { LocalDate.parse(it.key) }
                                .mapValues { (_, v) -> DiaEstado(M = v.M, T = v.T, N = v.N) }
                        cacheMes[ym] = mapaLocal
                        binding.calendarView.notifyMonthChanged(ym)
                    } else {
                        Toast.makeText(requireContext(), "Error al cargar disponibilidad de equipo.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // ⭐ AMBIENTES: usa tu endpoint real y mapea libre_en -> M/T/N
                    val resp = withContext(Dispatchers.IO) {
                        // OJO: esta es la misma interfaz que ya usas en la Activity
                        RetrofitClient.instance
                            .disponibilidadAmbiente(ambienteId = recursoId, desde = inicio, hasta = fin)
                            .execute()
                    }

                    if (resp.isSuccessful && resp.body() != null) {
                        val dias = resp.body()!!.dias
                        val mapaLocal: Map<LocalDate, DiaEstado> = dias.associate { d ->
                            val libres = d.libre_en.map { it.trim() }.toSet()
                            val estadoM = if ("Mañana" in libres) "libre" else "ocupado"
                            val estadoT = if ("Tarde"  in libres) "libre" else "ocupado"
                            val estadoN = if ("Noche"  in libres) "libre" else "ocupado"
                            LocalDate.parse(d.fecha) to DiaEstado(M = estadoM, T = estadoT, N = estadoN)
                        }
                        cacheMes[ym] = mapaLocal
                        binding.calendarView.notifyMonthChanged(ym)
                        android.util.Log.d("CalAmb", "YM=$ym -> ${cacheMes[ym]}")

                    } else {
                        Toast.makeText(requireContext(), "Error al cargar disponibilidad del ambiente.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                android.util.Log.d("CalAmb", "YM=$ym -> ${cacheMes[ym]}")

            } finally {
                showProgress(false)
                android.util.Log.d("CalAmb", "YM=$ym -> ${cacheMes[ym]}")

            }
        }



        @RequiresApi(Build.VERSION_CODES.O)
        private fun onDayClicked(date: LocalDate) {
            val dia = cacheMes[date.toYearMonth()]?.get(date)

            // Construye opciones SOLO con jornadas libres
            val libres = mutableListOf<Pair<String, String>>() // (label, code)
            if ((dia?.M ?: "libre") == "libre") libres += "Mañana" to "M"
            if ((dia?.T ?: "libre") == "libre") libres += "Tarde"  to "T"
            if ((dia?.N ?: "libre") == "libre") libres += "Noche"  to "N"

            if (libres.isEmpty()) {
                Toast.makeText(requireContext(), "No hay jornadas disponibles en $date", Toast.LENGTH_SHORT).show()
                return
            }

            val labels = libres.map { it.first }.toTypedArray()
            val checked = BooleanArray(labels.size)
            val seleccion = mutableListOf<String>() // codes

            val dlg = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Elige 1 o 2 jornadas para $date")
                .setMultiChoiceItems(labels, checked) { dialog, which, isChecked ->
                    val code = libres[which].second
                    if (isChecked) {
                        // tope 2
                        if (seleccion.size >= 2) {
                            (dialog as androidx.appcompat.app.AlertDialog).listView.setItemChecked(which, false)
                            Toast.makeText(requireContext(), "Máximo 2 jornadas", Toast.LENGTH_SHORT).show()
                        } else {
                            if (!seleccion.contains(code)) seleccion += code
                        }
                    } else {
                        seleccion.remove(code)
                    }
                }
                .setPositiveButton("Aceptar") { _, _ ->
                    if (seleccion.isEmpty()) {
                        Toast.makeText(requireContext(), "Selecciona al menos una jornada", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    irASolicitudMultiple(date.toString(), seleccion)
                }
                .setNegativeButton("Cancelar", null)
                .create()

            dlg.show()
        }

        private fun irASolicitudMultiple(fecha: String, jornadasCodes: List<String>) {
            if (returnResult) {
                requireActivity().setResult(
                    Activity.RESULT_OK,
                    Intent().apply {
                        putExtra(EXTRA_FECHA, fecha)
                        putExtra(EXTRA_JORNADA, jornadasCodes.first()) // compat
                        putStringArrayListExtra(EXTRA_JORNADAS_LIST, ArrayList(jornadasCodes))
                    }
                )
                requireActivity().finish()
                return
            }

            // Modo directo (si alguna vez lo usas sin returnResult):
            val jornadasCsv = jornadasCodes.joinToString(",")
            if (tipo == "equipo") {
                startActivity(Intent(requireContext(), SolicitudReservasEquipo::class.java).apply {
                    putExtra("fecha", fecha)
                    putExtra("jornadas_codes", jornadasCsv) // "M,T" por ejemplo
                    putExtra("elemento_id", recursoId)
                })
            } else {
                startActivity(Intent(requireContext(), SolicitudReservas::class.java).apply {
                    putExtra("fecha", fecha)
                    putExtra("jornadas_codes", jornadasCsv)
                    putExtra("ambiente_id", recursoId)
                })
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onResume() {
            super.onResume()
            // Vuelve a cargar el mes visible para reflejar reservas recién creadas
            binding.calendarView.findFirstVisibleMonth()?.let { vm ->
                cacheMes.remove(vm.yearMonth) // invalida cache
                viewLifecycleOwner.lifecycleScope.launch {
                    cargarMes(vm.yearMonth)   // vuelve a pedir al backend
                }
            }
        }



        private fun irASolicitud(fecha: String, jornada: String) {
            // Si se usa como selector (llamado desde una Activity con startActivityForResult)
            if (returnResult) {
                requireActivity().setResult(
                    Activity.RESULT_OK,
                    Intent().apply {
                        putExtra(EXTRA_FECHA, fecha)
                        putExtra(EXTRA_JORNADA, jornada) // "M" | "T" | "N"
                    }
                )
                requireActivity().finish()
                return
            }

            // Modo directo (abrir la pantalla de solicitud)
            if (tipo == "equipo") {
                startActivity(Intent(requireContext(), SolicitudReservasEquipo::class.java).apply {
                    putExtra("fecha", fecha)
                    putExtra("jornadas", jornada)
                    putExtra("elemento_id", recursoId)
                })
            } else {
                startActivity(Intent(requireContext(), SolicitudReservas::class.java).apply {
                    putExtra("fecha", fecha)
                    putExtra("jornadas", jornada)
                    putExtra("ambiente_id", recursoId)
                })
            }
        }

        private fun showProgress(show: Boolean) {
            binding.progress.isVisible = show
            binding.calendarView.isEnabled = !show
            binding.btnPrevMonth.isEnabled = !show
            binding.btnNextMonth.isEnabled = !show
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun formatMonth(ym: YearMonth): String {
            val f = DateTimeFormatter.ofPattern("LLLL yyyy", Locale("es", "ES"))
            val txt = ym.atDay(1).format(f)
            return txt.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun LocalDate.toYearMonth(): YearMonth = YearMonth.of(year, month)

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }
