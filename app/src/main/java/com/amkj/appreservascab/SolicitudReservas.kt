package com.amkj.appreservascab

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amkj.appreservascab.Modelos.*
import com.amkj.appreservascab.databinding.ActivitySolicitudReservasBinding
import com.amkj.appreservascab.models.DisponibilidadResponse
import com.amkj.appreservascab.servicios.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class SolicitudReservas : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitudReservasBinding
    private var fechaInicioSeleccionada: String = ""
    private var horaInicioSeleccionada: String = ""
    private var fechaFinSeleccionada: String = ""
    private var horaFinSeleccionada: String = ""

    // Día único (prioridad sobre rango)
    private var jornadasLibresDelDiaAmbiente: Set<String> = emptySet()

    // Rango (fecha -> libre_en) + intersección
    private val disponibilidadPorDia: MutableMap<String, List<String>> = mutableMapOf()
    private var jornadasLibresEnTodoElRango: Set<String> = emptySet()

    // Selección del usuario (máximo 2)
    private val jornadasSeleccionadas: MutableSet<String> = linkedSetOf()

    private val TAG = "SolicitudReservasAmb"
    private var ambienteIdParaCalendario: Int = 0

    // === Selector calendario: recibe fecha y 1..2 jornadas ===
    val seleccionarFechaJornada =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val data = res.data ?: return@registerForActivityResult

            // 1) ¿Vino RANGO?
            val desde = data.getStringExtra(CalendarioDisponibilidadFragment.EXTRA_FECHA_DESDE)
            val hasta = data.getStringExtra(CalendarioDisponibilidadFragment.EXTRA_FECHA_HASTA)

            // Códigos de jornadas (M/T/N)
            val codesArr = data.getStringArrayListExtra(CalendarioDisponibilidadFragment.EXTRA_JORNADAS_LIST)
            val fallback = data.getStringExtra(CalendarioDisponibilidadFragment.EXTRA_JORNADA)
            val codigos: List<String> = when {
                !codesArr.isNullOrEmpty()    -> codesArr.toList()
                !fallback.isNullOrEmpty()    -> listOf(fallback!!)
                else                         -> emptyList()
            }

            if (!desde.isNullOrEmpty() && !hasta.isNullOrEmpty()) {
                // === RANGO ===
                applyRangoSeleccionado(desde, hasta)

                // Ajusta horas combinadas y selección de badges
                setHorasPorJornadas(codigos)
                jornadasSeleccionadas.clear()
                jornadasSeleccionadas.addAll(codigos.map(::codeToLabel))
                setTextSeleccion()
                pintarBadges(neutral = false)
                setBadgesEnabled(true)

                // Consulta disponibilidad del RANGO (tu flujo actual)
                binding.pbDisponibilidad.visibility = View.VISIBLE
                setTextDisponibilidad("Consultando disponibilidad...")
                val amb = intent.getParcelableExtra<ModeloAmbientes>("ambiente")
                intentarActualizarDisponibilidad(amb)
                return@registerForActivityResult
            }

            // === DÍA ÚNICO (compat) ===
            val fecha = data.getStringExtra(CalendarioDisponibilidadFragment.EXTRA_FECHA) ?: return@registerForActivityResult
            applyDiaSeleccionado(fecha)

            setHorasPorJornadas(codigos)
            jornadasSeleccionadas.clear()
            jornadasSeleccionadas.addAll(codigos.map(::codeToLabel))
            setTextSeleccion()
            pintarBadges(neutral = false)
            setBadgesEnabled(true)

            // Consulta disponibilidad del DÍA
            binding.pbDisponibilidad.visibility = View.VISIBLE
            setTextDisponibilidad("Consultando disponibilidad del día...")
            consultarDisponibilidadAmbienteDelDia(ambienteIdParaCalendario, fecha)
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitudReservasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainlySolicitudReservas)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val ambiente = intent.getParcelableExtra<ModeloAmbientes>("ambiente")
        binding.tvNombreAmbiente.text = ambiente?.nombre ?: "Ambiente desconocido"
        ambienteIdParaCalendario = ambiente?.id ?: 0

        val sharedPref = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
        val usuario = ModeloUsuarios(
            id = sharedPref.getInt("id", -1),
            correo = sharedPref.getString("correo", null),
            contrasena = sharedPref.getString("contrasena", null),
            nombre = sharedPref.getString("nombre", "Nombre no disponible") ?: "Nombre no disponible",
            rol = sharedPref.getString("rol", "Rol no asignado") ?: "Rol no asignado",
            telefono = sharedPref.getString("telefono", "Telefono no ingresado") ?: "Telefono no ingresado"
        )
        binding.tvSolicitante.text = usuario.nombre
        val usuarioId = usuario.id

        val calendar = Calendar.getInstance()

        // Estado inicial
        pintarBadges(neutral = true)
        setBadgesEnabled(false)
        binding.pbDisponibilidad.visibility = View.GONE
        setTextDisponibilidad("Selecciona fecha inicio y fin para ver disponibilidad")
        setTextSeleccion()

        // Pickers INICIO
        binding.btnCalendarioInicio.setOnClickListener {
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    fechaInicioSeleccionada = String.format("%04d-%02d-%02d", y, m + 1, d)
                    binding.tvFechaInicio.text = fechaInicioSeleccionada
                    jornadasSeleccionadas.clear()
                    pintarBadges(neutral = true)
                    setBadgesEnabled(false)
                    binding.pbDisponibilidad.visibility = View.VISIBLE
                    setTextDisponibilidad("Consultando disponibilidad...")
                    intentarActualizarDisponibilidad(ambiente)
                    setTextSeleccion()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        binding.btnHoraInicio.setOnClickListener {
            TimePickerDialog(this, { _, h, min ->
                horaInicioSeleccionada = String.format("%02d:%02d:00", h, min)
                binding.tvHoraInicio.text = horaInicioSeleccionada
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        // Pickers FIN
        binding.btnCalendarioFin.setOnClickListener {
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    fechaFinSeleccionada = String.format("%04d-%02d-%02d", y, m + 1, d)
                    binding.tvFechaFin.text = fechaFinSeleccionada
                    jornadasSeleccionadas.clear()
                    pintarBadges(neutral = true)
                    setBadgesEnabled(false)
                    binding.pbDisponibilidad.visibility = View.VISIBLE
                    setTextDisponibilidad("Consultando disponibilidad...")
                    intentarActualizarDisponibilidad(ambiente)
                    setTextSeleccion()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        binding.btnHoraFin.setOnClickListener {
            TimePickerDialog(this, { _, h, min ->
                horaFinSeleccionada = String.format("%02d:%02d:00", h, min)
                binding.tvHoraFin.text = horaFinSeleccionada
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        // Calendario mensual (1..2 jornadas)
        binding.btnVerCalendario.setOnClickListener {
            jornadasSeleccionadas.clear()
            pintarBadges(neutral = true)
            setBadgesEnabled(false)
            setTextSeleccion()

            val amb = intent.getParcelableExtra<ModeloAmbientes>("ambiente")
            val id = amb?.id ?: 0
            if (id == 0) {
                Toast.makeText(this, "No se reconoce el ambiente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val i = Intent(this, DisponibilidadActivity::class.java).apply {
                putExtra("tipo", "ambiente")
                putExtra("recursoId", id)
                putExtra("returnResult", true)
            }
            seleccionarFechaJornada.launch(i)
        }


        // Badges
        binding.badgeManana.setOnClickListener { onBadgeClick("Mañana", binding.badgeManana) }
        binding.badgeTarde.setOnClickListener  { onBadgeClick("Tarde",  binding.badgeTarde) }
        binding.badgeNoche.setOnClickListener  { onBadgeClick("Noche",  binding.badgeNoche) }

        // Guardar (flujo igual; solo mensajes)
        binding.btnGuardar.setOnClickListener {
            if (ambiente == null ||
                fechaInicioSeleccionada.isEmpty() || horaInicioSeleccionada.isEmpty() ||
                fechaFinSeleccionada.isEmpty() || horaFinSeleccionada.isEmpty()
            ) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val fechaHoraInicio = "$fechaInicioSeleccionada $horaInicioSeleccionada"
            val fechaHoraFin = "$fechaFinSeleccionada $horaFinSeleccionada"
            val f1 = sdf.parse(fechaHoraInicio)
            val f2 = sdf.parse(fechaHoraFin)

            if (f1 == null || f2 == null || !f2.after(f1)) {
                Toast.makeText(this, "La fecha y hora de fin debe ser mayor que la de inicio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (jornadasSeleccionadas.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos una jornada", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (jornadasLibresEnTodoElRango.isNotEmpty() &&
                !jornadasSeleccionadas.all { it in jornadasLibresEnTodoElRango }) {
                Toast.makeText(this, "Hay jornadas no libres en TODO el rango. Revisa la disponibilidad.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val reserva = ModeloReserva(
                usuario_id = usuarioId,
                ambiente_id = ambiente.id,
                fecha_hora_inicio = fechaHoraInicio,
                fecha_hora_fin = fechaHoraFin,
                motivo = "Reserva de ambiente",
                jornadas = jornadasSeleccionadas.joinToString(", "),
                id_reserva = 0,
                ambiente_nombre = ambiente.nombre,
                ambiente_imagen = null
            )

            val datos = SolicitudDisponibilidadRequest(
                ambiente_id = ambiente.id,
                fecha = fechaInicioSeleccionada,
                jornadas = jornadasSeleccionadas.joinToString(", ")
            )

            RetrofitClient.instance.validarDisponibilidadAmbiente(datos)
                .enqueue(object : Callback<ValidacionAmbienteResponse> {
                    override fun onResponse(
                        call: Call<ValidacionAmbienteResponse>,
                        response: Response<ValidacionAmbienteResponse>
                    ) {
                        val body = response.body()
                        if (!response.isSuccessful || body == null) {
                            Toast.makeText(this@SolicitudReservas, "No se pudo validar disponibilidad", Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "validarDisponibilidadAmbiente code=${response.code()}")
                            return
                        }
                        if (body.disponible != true) {
                            val bloqueadas = body.detalle?.filterValues { it != "libre" }?.keys?.joinToString()
                            val msg = if (!bloqueadas.isNullOrEmpty())
                                "Jornadas no disponibles: $bloqueadas"
                            else "Este ambiente ya tiene una reserva en esa(s) jornada(s)"
                            Toast.makeText(this@SolicitudReservas, msg, Toast.LENGTH_LONG).show()
                            return
                        }

                        RetrofitClient.instance.guardarReserva(reserva)
                            .enqueue(object : Callback<ResponseBody> {
                                override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                                    if (resp.isSuccessful) {
                                        Toast.makeText(this@SolicitudReservas, "Reserva exitosa", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        Toast.makeText(this@SolicitudReservas, "No se pudo guardar la reserva", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                    Toast.makeText(this@SolicitudReservas, "Error al guardar: ${t.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                    override fun onFailure(call: Call<ValidacionAmbienteResponse>, t: Throwable) {
                        Toast.makeText(this@SolicitudReservas, "Error de red al validar disponibilidad: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        binding.ibAtras.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    /** Pinta SIEMPRE ambos extremos del rango en UI y estado interno */
    private fun applyRangoSeleccionado(desde: String, hasta: String) {
        fechaInicioSeleccionada = desde
        fechaFinSeleccionada    = hasta
        binding.tvFechaInicio.text = desde
        binding.tvFechaFin.text    = hasta

        // Mensaje claro en la misma etiqueta de estado:
        val textoRango = if (desde == hasta) "Día seleccionado: $desde"
        else "Rango seleccionado: $desde → $hasta"
        setTextDisponibilidad(textoRango)
    }

    /** Azúcar: día único = rango de 1 día */
    private fun applyDiaSeleccionado(fecha: String) = applyRangoSeleccionado(fecha, fecha)


    // --------- Disponibilidad (DÍA) ---------
    private fun consultarDisponibilidadAmbienteDelDia(ambienteId: Int, fecha: String) {
        RetrofitClient.instance.disponibilidadAmbienteDia(
            ambienteId = ambienteId,
            fecha = fecha
        ).enqueue(object : Callback<DisponibilidadResponse> {
            override fun onResponse(call: Call<DisponibilidadResponse>, response: Response<DisponibilidadResponse>) {
                binding.pbDisponibilidad.visibility = View.GONE
                if (!response.isSuccessful || response.body() == null) {
                    jornadasLibresDelDiaAmbiente = emptySet()
                    pintarBadges(neutral = true)
                    setBadgesEnabled(false)
                    setTextDisponibilidad("Sin datos de disponibilidad")
                    setTextSeleccion()
                    return
                }

                val dia = response.body()!!.dias.firstOrNull()
                jornadasLibresDelDiaAmbiente = dia?.libre_en?.map { it.trim() }?.toSet() ?: emptySet()

                Log.d(TAG, "Ambiente DIA libres=$jornadasLibresDelDiaAmbiente")

                // Mantener solo selecciones que siguen libres
                jornadasSeleccionadas.retainAll(jornadasLibresDelDiaAmbiente)

                // UI
                pintarBadges(neutral = false)
                setBadgesEnabled(true)
                setTextDisponibilidadByLibres()
                setTextSeleccion()
            }
            override fun onFailure(call: Call<DisponibilidadResponse>, t: Throwable) {
                binding.pbDisponibilidad.visibility = View.GONE
                jornadasLibresDelDiaAmbiente = emptySet()
                pintarBadges(neutral = true)
                setBadgesEnabled(false)
                setTextDisponibilidad("Sin datos de disponibilidad")
                setTextSeleccion()
                Toast.makeText(this@SolicitudReservas, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // --------- Disponibilidad (RANGO) ---------
    private fun intentarActualizarDisponibilidad(ambiente: ModeloAmbientes?) {
        if (ambiente == null) return
        if (fechaInicioSeleccionada.isEmpty() || fechaFinSeleccionada.isEmpty()) return

        // Si es un solo día, usamos el endpoint de día
        if (fechaInicioSeleccionada == fechaFinSeleccionada) {
            consultarDisponibilidadAmbienteDelDia(ambiente.id, fechaInicioSeleccionada)
            return
        }

        RetrofitClient.instance.disponibilidadAmbiente(
            ambienteId = ambiente.id,
            desde = fechaInicioSeleccionada,
            hasta = fechaFinSeleccionada
        ).enqueue(object : Callback<DisponibilidadResponse> {
            override fun onResponse(call: Call<DisponibilidadResponse>, response: Response<DisponibilidadResponse>) {
                binding.pbDisponibilidad.visibility = View.GONE
                if (!response.isSuccessful || response.body() == null) {
                    jornadasLibresEnTodoElRango = emptySet()
                    jornadasLibresDelDiaAmbiente = emptySet()
                    pintarBadges(neutral = true)
                    setBadgesEnabled(false)
                    setTextDisponibilidad("Sin datos de disponibilidad")
                    setTextSeleccion()
                    return
                }

                val body = response.body()!!
                disponibilidadPorDia.clear()
                body.dias.forEach { d -> disponibilidadPorDia[d.fecha] = d.libre_en }

                jornadasLibresEnTodoElRango = calcularInterseccion(body.dias.map { it.libre_en })
                jornadasLibresDelDiaAmbiente = emptySet() // es rango

                // Limpiar selecciones que ya no aplican al rango
                jornadasSeleccionadas.retainAll(jornadasLibresEnTodoElRango)

                pintarBadges(neutral = false)
                setBadgesEnabled(true)

                val todas = setOf("Mañana","Tarde","Noche")
                val msg = when {
                    jornadasLibresEnTodoElRango.isEmpty() -> "Sin jornadas libres en TODO el rango"
                    jornadasLibresEnTodoElRango == todas  -> "Libre TODO el rango (todas las jornadas)"
                    else -> "Libre en TODO el rango: ${jornadasLibresEnTodoElRango.joinToString()}"
                }
                setTextDisponibilidad(msg)
                setTextSeleccion()
            }
            override fun onFailure(call: Call<DisponibilidadResponse>, t: Throwable) {
                binding.pbDisponibilidad.visibility = View.GONE
                jornadasLibresEnTodoElRango = emptySet()
                jornadasLibresDelDiaAmbiente = emptySet()
                pintarBadges(neutral = true)
                setBadgesEnabled(false)
                setTextDisponibilidad("Sin datos de disponibilidad")
                setTextSeleccion()
                Toast.makeText(this@SolicitudReservas, "Error de red al consultar disponibilidad: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Intersección para el rango
    private fun calcularInterseccion(listas: List<List<String>>): Set<String> {
        if (listas.isEmpty()) return emptySet()
        var inter = listas.first().map { it.trim() }.toSet()
        listas.drop(1).forEach { l -> inter = inter.intersect(l.map { it.trim() }.toSet()) }
        return inter
    }

    // --------- Click de badge ---------
    private fun onBadgeClick(jornada: String, view: TextView) {
        val libres = jornadasDisponiblesActuales()
        if (libres.isNotEmpty() && jornada !in libres) {
            Toast.makeText(this, "La jornada '$jornada' no está libre", Toast.LENGTH_SHORT).show()
            return
        }
        if (jornadasSeleccionadas.contains(jornada)) {
            jornadasSeleccionadas.remove(jornada)
            setBadgeSelected(view, selected = false)
        } else {
            if (jornadasSeleccionadas.size >= 2) {
                Toast.makeText(this, "Máximo 2 jornadas permitidas", Toast.LENGTH_SHORT).show()
                return
            }
            jornadasSeleccionadas.add(jornada)
            setBadgeSelected(view, selected = true)
        }
        // Refresca habilitados y texto tras cualquier cambio
        setBadgesEnabled(true)
        pintarBadges(neutral = false)
        setTextSeleccion()
    }

    // --------- Pintado y habilitado ---------
    private fun pintarBadges(neutral: Boolean) {
        if (neutral) {
            paint(binding.badgeManana, R.drawable.bg_badge_neutral)
            paint(binding.badgeTarde,  R.drawable.bg_badge_neutral)
            paint(binding.badgeNoche,  R.drawable.bg_badge_neutral)
            return
        }
        val libres = jornadasDisponiblesActuales()
        fun bgFor(j: String): Int {
            val seleccionado = j in jornadasSeleccionadas
            val libre = j in libres
            return when {
                !libre       -> R.drawable.bg_badge_bad
                seleccionado -> R.drawable.bg_badge_ok
                else         -> R.drawable.bg_badge_neutral
            }
        }
        paint(binding.badgeManana, bgFor("Mañana"))
        paint(binding.badgeTarde,  bgFor("Tarde"))
        paint(binding.badgeNoche,  bgFor("Noche"))
    }

    private fun setBadgesEnabled(enabledFromData: Boolean) {
        val libres = jornadasDisponiblesActuales()
        val maxReached = jornadasSeleccionadas.size >= 2

        fun apply(label: String, v: TextView) {
            val available = label in libres
            val selected  = label in jornadasSeleccionadas
            val enabled = when {
                !enabledFromData        -> false
                !available              -> false
                maxReached && !selected -> false
                else                    -> true
            }
            v.isEnabled = enabled
            v.alpha = if (enabled) 1f else .5f
        }

        apply("Mañana", binding.badgeManana)
        apply("Tarde",  binding.badgeTarde)
        apply("Noche",  binding.badgeNoche)
    }

    private fun setBadgeSelected(view: TextView, selected: Boolean) {
        view.setBackgroundResource(if (selected) R.drawable.bg_badge_ok else R.drawable.bg_badge_neutral)
    }
    private fun paint(v: TextView, bg: Int) { v.setBackgroundResource(bg) }

    // --------- Textos auxiliares ---------
    private fun setTextDisponibilidad(msg: String?) {
        binding.tvEstadoRango.text = msg ?: ""
    }

    private fun setTextDisponibilidadByLibres() {
        val libres = jornadasDisponiblesActuales()
        val todas = setOf("Mañana","Tarde","Noche")
        val msg = when {
            libres.isEmpty() -> "Disponibilidad: sin jornadas libres"
            libres == todas  -> "Disponibilidad: libre todo el día"
            else             -> "Disponibilidad: libre en ${libres.joinToString()}"
        }
        setTextDisponibilidad(msg)
    }

    private fun setTextSeleccion() {
        val txt = if (jornadasSeleccionadas.isEmpty())
            "Tu selección: (ninguna)"
        else
            "Tu selección: ${jornadasSeleccionadas.joinToString()}"

        // Si existe tvSeleccionDia en tu XML lo usa; si no, hace fallback debajo del estado
        val tvSel = binding.root.findViewById<TextView?>(R.id.tvSeleccionDia)
        if (tvSel != null) {
            tvSel.text = txt
        } else {
            val firstLine = binding.tvEstadoRango.text.toString().lineSequence().firstOrNull().orEmpty()
            binding.tvEstadoRango.text = firstLine + "\n" + txt
        }
    }

    // --------- Helpers ---------
    private fun setHorasPorJornadas(codes: List<String>) {
        fun rango(code: String): Pair<String, String>? = when (code.uppercase(Locale.getDefault())) {
            "M" -> "08:00:00" to "12:00:00"
            "T" -> "13:00:00" to "17:00:00"
            "N" -> "18:00:00" to "22:00:00"
            else -> null
        }
        val rangos = codes.mapNotNull(::rango)
        if (rangos.isEmpty()) return
        val inicio = rangos.minOf { it.first }
        val fin    = rangos.maxOf { it.second }
        horaInicioSeleccionada = inicio
        horaFinSeleccionada = fin
        binding.tvHoraInicio.text = horaInicioSeleccionada
        binding.tvHoraFin.text = horaFinSeleccionada
    }

    private fun codeToLabel(j: String) = when (j.uppercase(Locale.getDefault())) {
        "M" -> "Mañana"; "T" -> "Tarde"; "N" -> "Noche"; else -> j
    }

    private fun jornadasDisponiblesActuales(): Set<String> =
        if (fechaInicioSeleccionada.isNotEmpty()
            && fechaInicioSeleccionada == fechaFinSeleccionada
            && jornadasLibresDelDiaAmbiente.isNotEmpty()
        ) jornadasLibresDelDiaAmbiente
        else jornadasLibresEnTodoElRango
}
