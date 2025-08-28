package com.amkj.appreservascab

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amkj.appreservascab.Modelos.*
import com.amkj.appreservascab.databinding.ActivitySolicitudReservasEquipoBinding
import com.amkj.appreservascab.models.DisponibilidadResponse
import com.amkj.appreservascab.servicios.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class SolicitudReservasEquipo : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitudReservasEquipoBinding

    // --- Modo día único (compat) ---
    private var fechaSeleccionada: String = ""
    private var jornadasLibresDelDia: Set<String> = emptySet()

    // --- Modo rango ---
    private var fechaDesdeSeleccionada: String = ""
    private var fechaHastaSeleccionada: String = ""
    private var jornadasLibresEnTodoElRango: Set<String> = emptySet()

    // Selección (máx 2)
    private val jornadasSeleccionadas: MutableSet<String> = linkedSetOf()

    // Preselección desde el calendario (labels)
    private var jornadasPreseleccionadasLabels: List<String>? = null

    private val TAG = "EquipoReservaDebug"
    private var esAdmin: Boolean = false

    // Recibir fecha o rango + 1..2 jornadas
    private val seleccionarFechaJornada =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val data = res.data ?: return@registerForActivityResult

            val fechaDia  = data.getStringExtra(CalendarioDisponibilidadFragment.EXTRA_FECHA) // compat
            val desde     = data.getStringExtra(CalendarioDisponibilidadFragment.EXTRA_FECHA_DESDE)
            val hasta     = data.getStringExtra(CalendarioDisponibilidadFragment.EXTRA_FECHA_HASTA)
            val codesList = data.getStringArrayListExtra(CalendarioDisponibilidadFragment.EXTRA_JORNADAS_LIST)

            val equipo = intent.getParcelableExtra<ModeloEquipos>("equipo")
            if (equipo == null) {
                Toast.makeText(this, "Equipo no reconocido", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            // Limpia selección previa
            jornadasSeleccionadas.clear()
            jornadasPreseleccionadasLabels = (codesList ?: arrayListOf()).map(::codeToLabel)

            if (!desde.isNullOrEmpty() && !hasta.isNullOrEmpty()) {
                // ===== RANGO =====
                fechaDesdeSeleccionada = desde
                fechaHastaSeleccionada = hasta
                fechaSeleccionada = "" // desactivar día único
                binding.tvFechaSeleccionada.text = "$fechaDesdeSeleccionada → $fechaHastaSeleccionada"

                consultarDisponibilidadRango(equipo.id, fechaDesdeSeleccionada, fechaHastaSeleccionada) {
                    // Aplica preselección (1–2 labels) respetando intersección
                    jornadasPreseleccionadasLabels?.forEach { l ->
                        if (jornadasSeleccionadas.size < 2 && l in jornadasLibresEnTodoElRango) {
                            jornadasSeleccionadas += l
                        }
                    }
                    jornadasPreseleccionadasLabels = null
                    pintarBadges(neutral = false)
                    setBadgesEnabled(true)
                    setTextDisponibilidadByLibres()
                    setTextSeleccion()
                }

            } else if (!fechaDia.isNullOrEmpty()) {
                // ===== DÍA ÚNICO =====
                fechaSeleccionada = fechaDia
                binding.tvFechaSeleccionada.text = fechaSeleccionada
                fechaDesdeSeleccionada = ""
                fechaHastaSeleccionada = ""

                consultarDisponibilidadDelDia(equipo.id, fechaSeleccionada) {
                    jornadasPreseleccionadasLabels?.forEach { l ->
                        if (jornadasSeleccionadas.size < 2 && l in jornadasLibresDelDia) {
                            jornadasSeleccionadas += l
                        }
                    }
                    jornadasPreseleccionadasLabels = null
                    pintarBadges(neutral = false)
                    setBadgesEnabled(true)
                    setTextDisponibilidadByLibres()
                    setTextSeleccion()
                }
            } else {
                Toast.makeText(this, "No se recibió fecha válida", Toast.LENGTH_SHORT).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitudReservasEquipoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainlySolicitudReservasEquipo) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val equipo = intent.getParcelableExtra<ModeloEquipos>("equipo")
        val sharedPref = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
        val nombreUsuario = sharedPref.getString("nombre", "") ?: ""
        esAdmin = (sharedPref.getString("rol", "") ?: "").equals("admin", ignoreCase = true)

        if (equipo != null) {
            binding.tvInfoMarca.text = equipo.marca
            binding.tvInfoReferencia.text = equipo.modelo
            binding.tvNombreEquipo.text = equipo.descripcion
            binding.tvNomUser.text = nombreUsuario
        } else {
            Log.e(TAG, "No se recibió equipo en el intent")
        }

        // Abrir calendario (ahora con rango + jornadas en el mismo fragment)
        binding.btnCalendario.setOnClickListener {
            val id = equipo?.id ?: 0
            if (id == 0) {
                Toast.makeText(this, "Equipo no reconocido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val i = Intent(this, DisponibilidadActivity::class.java).apply {
                putExtra("tipo", "equipo")
                putExtra("recursoId", id)
                putExtra("returnResult", true) // <- importante para recibir rango+jornadas
            }
            seleccionarFechaJornada.launch(i)
        }

        // Badges (máx 2)
        binding.badgeManana.setOnClickListener { onBadgeClick("Mañana", binding.badgeManana) }
        binding.badgeTarde.setOnClickListener  { onBadgeClick("Tarde",  binding.badgeTarde) }
        binding.badgeNoche.setOnClickListener  { onBadgeClick("Noche",  binding.badgeNoche) }

        // Guardar
        binding.btnGuardar.setOnClickListener {
            if (equipo == null) {
                Toast.makeText(this, "Equipo no reconocido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (jornadasSeleccionadas.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos una jornada", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usuarioId = sharedPref.getInt("id", -1)

            // == RANGO ==
            if (fechaDesdeSeleccionada.isNotEmpty() && fechaHastaSeleccionada.isNotEmpty()) {
                // Verifica que la selección esté dentro de la intersección del rango
                if (jornadasLibresEnTodoElRango.isNotEmpty() &&
                    !jornadasSeleccionadas.all { it in jornadasLibresEnTodoElRango }) {
                    Toast.makeText(this, "Hay jornadas no libres en TODO el rango", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                val dias = fechasEntre(fechaDesdeSeleccionada, fechaHastaSeleccionada)
                if (dias.isEmpty()) {
                    Toast.makeText(this, "Rango inválido", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                guardarReservaEquipoRango(
                    dias = dias,
                    equipoId = equipo.id,
                    usuarioId = usuarioId,
                    jornadasCsv = jornadasSeleccionadas.joinToString(", ")
                )
                return@setOnClickListener
            }

            // == DÍA ÚNICO ==
            if (fechaSeleccionada.isEmpty()) {
                Toast.makeText(this, "Selecciona una fecha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (jornadasLibresDelDia.isNotEmpty() &&
                !jornadasSeleccionadas.all { it in jornadasLibresDelDia }) {
                Toast.makeText(this, "Hay jornadas no libres para la fecha seleccionada", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val reservaRequest = ReservaEquipoRequest(
                elemento_id = equipo.id,
                usuario_id = usuarioId,
                fecha = fechaSeleccionada,
                jornadas = jornadasSeleccionadas.joinToString(", "),
                motivo = "Reserva de equipo",
                estado = "pendiente"
            )

            val datos = ValidacionEquipoRequest(
                equipo_id = equipo.id,
                fecha = fechaSeleccionada,
                jornadas = reservaRequest.jornadas
            )

            RetrofitClient.instance.validarDisponibilidadEquipo(datos)
                .enqueue(object : Callback<ValidacionEquipoResponse> {
                    override fun onResponse(
                        call: Call<ValidacionEquipoResponse>,
                        response: Response<ValidacionEquipoResponse>
                    ) {
                        val body = response.body()
                        if (!response.isSuccessful || body == null) {
                            Toast.makeText(this@SolicitudReservasEquipo, "No se pudo validar disponibilidad", Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "validarDisponibilidadEquipo code=${response.code()}")
                            return
                        }
                        if (body.disponible != true) {
                            val bloqueadas = body.detalle?.filterValues { it != "libre" }?.keys?.joinToString()
                            val msg = if (!bloqueadas.isNullOrEmpty())
                                "Jornadas no disponibles: $bloqueadas"
                            else "Este equipo ya tiene una reserva aceptada en esa(s) jornada(s)"
                            Toast.makeText(this@SolicitudReservasEquipo, msg, Toast.LENGTH_LONG).show()
                            return
                        }

                        RetrofitClient.instance.guardarReservaEquipo(reservaRequest)
                            .enqueue(object : Callback<ResponseBody> {
                                override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                                    if (resp.isSuccessful) {
                                        Toast.makeText(this@SolicitudReservasEquipo, "Reserva enviada con éxito", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        Toast.makeText(this@SolicitudReservasEquipo, "Error al guardar reserva", Toast.LENGTH_SHORT).show()
                                        Log.e(TAG, "guardarReservaEquipo code=${resp.code()}")
                                    }
                                }
                                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                    Toast.makeText(this@SolicitudReservasEquipo, "Error al guardar: ${t.message}", Toast.LENGTH_SHORT).show()
                                    Log.e(TAG, "guardarReservaEquipo onFailure", t)
                                }
                            })
                    }

                    override fun onFailure(call: Call<ValidacionEquipoResponse>, t: Throwable) {
                        Toast.makeText(this@SolicitudReservasEquipo, "Error de red al validar disponibilidad: ${t.message}", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "validarDisponibilidadEquipo onFailure", t)
                    }
                })
        }

        binding.ibAtras.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Estado inicial UI
        pintarBadges(neutral = true)
        setBadgesEnabled(false)
        setTextDisponibilidad("Selecciona fecha o rango para ver disponibilidad")
        setTextSeleccion()
    }

    // ================== DISPONIBILIDAD ==================

    /** Día único */
    private fun consultarDisponibilidadDelDia(elementoId: Int, fecha: String, after: () -> Unit) {
        binding.pbDisponibilidad.visibility = View.VISIBLE
        setTextDisponibilidad("Consultando disponibilidad...")

        RetrofitClient.instance.disponibilidadEquipoDia(elementoId, fecha)
            .enqueue(object : Callback<DisponibilidadEquipoDiaResponse> {
                override fun onResponse(
                    call: Call<DisponibilidadEquipoDiaResponse>,
                    response: Response<DisponibilidadEquipoDiaResponse>
                ) {
                    binding.pbDisponibilidad.visibility = View.GONE

                    if (!response.isSuccessful || response.body() == null) {
                        jornadasLibresDelDia = emptySet()
                        jornadasSeleccionadas.clear()
                        pintarBadges(neutral = true)
                        setBadgesEnabled(false)
                        setTextDisponibilidad("Sin datos de disponibilidad")
                        setTextSeleccion()
                        after()
                        return
                    }

                    val dia = response.body()!!.dias.firstOrNull()
                    jornadasLibresDelDia = dia?.libre_en?.map { it.trim() }?.toSet() ?: emptySet()

                    // Limpia selección incompatible
                    jornadasSeleccionadas.retainAll(jornadasLibresDelDia)

                    // UI
                    pintarBadges(neutral = false)
                    setBadgesEnabled(true)
                    setTextDisponibilidadByLibres()
                    setTextSeleccion()
                    after()
                }

                override fun onFailure(p0: Call<DisponibilidadEquipoDiaResponse>, t: Throwable) {
                    binding.pbDisponibilidad.visibility = View.GONE
                    jornadasLibresDelDia = emptySet()
                    jornadasSeleccionadas.clear()
                    pintarBadges(neutral = true)
                    setBadgesEnabled(false)
                    setTextDisponibilidad("Sin datos de disponibilidad")
                    setTextSeleccion()
                    Toast.makeText(this@SolicitudReservasEquipo, "Error de red al consultar disponibilidad", Toast.LENGTH_SHORT).show()
                    after()
                }
            })
    }

    /** Rango: usa disponibilidadEquipoRango y calcula intersección (M/T/N libres en TODOS los días) */
    private fun consultarDisponibilidadRango(elementoId: Int, desde: String, hasta: String, after: () -> Unit) {
        binding.pbDisponibilidad.visibility = View.VISIBLE
        setTextDisponibilidad("Consultando disponibilidad del rango...")

        RetrofitClient.servicioApi.disponibilidadEquipoRango(elementoId, desde, hasta)
            .enqueue(object : retrofit2.Callback<DisponibilidadRangoEquipoResponse> {
                override fun onResponse(
                    call: retrofit2.Call<DisponibilidadRangoEquipoResponse>,
                    response: retrofit2.Response<DisponibilidadRangoEquipoResponse>
                ) {
                    binding.pbDisponibilidad.visibility = View.GONE
                    val body = response.body()
                    if (!response.isSuccessful || body?.ok != true) {
                        jornadasLibresEnTodoElRango = emptySet()
                        jornadasSeleccionadas.clear()
                        pintarBadges(neutral = true)
                        setBadgesEnabled(false)
                        setTextDisponibilidad("Sin datos de disponibilidad")
                        setTextSeleccion()
                        after()
                        return
                    }

                    val mapa = body.mapa // Map<String(fecha), EstadoDiaCod(M,T,N)>
                    var inter: Set<String>? = null
                    for ((_, est) in mapa) {
                        val libresDia = buildSet {
                            if (est.M == "libre") add("Mañana")
                            if (est.T == "libre") add("Tarde")
                            if (est.N == "libre") add("Noche")
                        }
                        inter = if (inter == null) libresDia else inter!!.intersect(libresDia)
                    }
                    jornadasLibresEnTodoElRango = inter ?: emptySet()

                    jornadasSeleccionadas.retainAll(jornadasLibresEnTodoElRango)

                    pintarBadges(neutral = false)
                    setBadgesEnabled(true)
                    setTextDisponibilidadByLibres()
                    setTextSeleccion()
                    after()
                }

                override fun onFailure(p0: Call<DisponibilidadRangoEquipoResponse>, t: Throwable) {
                    binding.pbDisponibilidad.visibility = View.GONE
                    jornadasLibresEnTodoElRango = emptySet()
                    jornadasSeleccionadas.clear()
                    pintarBadges(neutral = true)
                    setBadgesEnabled(false)
                    setTextDisponibilidad("Sin datos de disponibilidad")
                    setTextSeleccion()
                    Toast.makeText(this@SolicitudReservasEquipo, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
                    after()
                }
            })
    }

    // ================== BADGES ==================

    private fun onBadgeClick(jornada: String, view: TextView) {
        val libres = jornadasDisponiblesActuales()
        if (libres.isNotEmpty() && jornada !in libres) {
            Toast.makeText(this, "La jornada '$jornada' no está disponible", Toast.LENGTH_SHORT).show()
            return
        }

        if (jornadasSeleccionadas.contains(jornada)) {
            jornadasSeleccionadas.remove(jornada)
            setBadgeSelected(view, false)
        } else {
            if (jornadasSeleccionadas.size >= 2) {
                Toast.makeText(this, "Máximo 2 jornadas permitidas", Toast.LENGTH_SHORT).show()
                return
            }
            jornadasSeleccionadas.add(jornada)
            setBadgeSelected(view, true)
        }
        setBadgesEnabled(true)
        setTextSeleccion()
    }

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

    // ================== TEXTOS ==================

    private fun setTextDisponibilidad(msg: String?) { binding.tvEstadoDia.text = msg ?: "" }

    private fun setTextDisponibilidadByLibres() {
        val libres = jornadasDisponiblesActuales()
        val todas  = setOf("Mañana","Tarde","Noche")
        val msg = when {
            libres.isEmpty() -> "Disponibilidad: sin jornadas libres"
            libres == todas  -> "Disponibilidad: libre todo el día"
            else             -> "Disponibilidad: ${libres.joinToString()}"
        }
        setTextDisponibilidad(msg)
    }

    private fun setTextSeleccion() {
        val txt = if (jornadasSeleccionadas.isEmpty())
            "Tu selección: (ninguna)"
        else
            "Tu selección: ${jornadasSeleccionadas.joinToString()}"
        val tvSel = binding.root.findViewById<View?>(R.id.tvSeleccionDia) as? TextView
        if (tvSel != null) tvSel.text = txt
        else binding.tvEstadoDia.text = binding.tvEstadoDia.text.toString().lineSequence().firstOrNull().orEmpty() + "\n" + txt
    }

    // ================== HELPERS ==================

    private fun jornadasDisponiblesActuales(): Set<String> =
        if (fechaDesdeSeleccionada.isNotEmpty() && fechaHastaSeleccionada.isNotEmpty())
            jornadasLibresEnTodoElRango
        else
            jornadasLibresDelDia

    private fun codeToLabel(j: String) = when (j.uppercase(Locale.getDefault())) {
        "M" -> "Mañana"; "T" -> "Tarde"; "N" -> "Noche"; else -> j
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fechasEntre(desde: String, hasta: String): List<String> {
        val f = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val start = LocalDate.parse(desde, f)
        val end   = LocalDate.parse(hasta, f)
        val out = mutableListOf<String>()
        var d = start
        while (!d.isAfter(end)) {
            out += d.format(f)
            d = d.plusDays(1)
        }
        return out
    }

    // Crea reservas día a día (valida y guarda)
    private fun guardarReservaEquipoRango(
        dias: List<String>,
        equipoId: Int,
        usuarioId: Int,
        jornadasCsv: String
    ) {
        if (dias.isEmpty()) return

        fun step(index: Int) {
            if (index >= dias.size) {
                Toast.makeText(this, "Reservas creadas para el rango", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            val fecha = dias[index]

            val valida = ValidacionEquipoRequest(
                equipo_id = equipoId,
                fecha = fecha,
                jornadas = jornadasCsv
            )

            RetrofitClient.instance.validarDisponibilidadEquipo(valida)
                .enqueue(object : Callback<ValidacionEquipoResponse> {
                    override fun onResponse(call: Call<ValidacionEquipoResponse>, response: Response<ValidacionEquipoResponse>) {
                        val ok = response.isSuccessful && response.body()?.disponible == true
                        if (!ok) {
                            Toast.makeText(this@SolicitudReservasEquipo, "Día $fecha no disponible; se omite.", Toast.LENGTH_SHORT).show()
                            step(index + 1)
                            return
                        }
                        val req = ReservaEquipoRequest(
                            elemento_id = equipoId,
                            usuario_id = usuarioId,
                            fecha = fecha,
                            jornadas = jornadasCsv,
                            motivo = "Reserva de equipo",
                            estado = "pendiente"
                        )
                        RetrofitClient.instance.guardarReservaEquipo(req)
                            .enqueue(object : Callback<ResponseBody> {
                                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                    step(index + 1)
                                }
                                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                    step(index + 1)
                                }
                            })
                    }
                    override fun onFailure(call: Call<ValidacionEquipoResponse>, t: Throwable) {
                        step(index + 1)
                    }
                })
        }
        step(0)
    }

    // (opcional) admins ven quién reservó
    private fun cargarReservasDeUsuariosAdmin(elementoId: Int, fecha: String) {
        val tv = binding.root.findViewById<TextView?>(R.id.tvReservasAdmin) ?: return
        // Implementa tu llamada si ya tienes el endpoint:
        // RetrofitClient.instance.reservasEquipoDia(elementoId, fecha) { ... }
        tv.visibility = View.GONE
    }
}
