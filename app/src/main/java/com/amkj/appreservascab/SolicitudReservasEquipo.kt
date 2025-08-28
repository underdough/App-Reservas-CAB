package com.amkj.appreservascab

import android.app.Activity
import android.app.DatePickerDialog
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
import com.amkj.appreservascab.databinding.ActivitySolicitudReservasEquipoBinding
import com.amkj.appreservascab.models.DisponibilidadResponse
import com.amkj.appreservascab.servicios.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SolicitudReservasEquipo : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitudReservasEquipoBinding
    private var fechaSeleccionada: String = ""

    // Disponibilidad del día (lo que devuelve el backend)
    private var jornadasLibresDelDia: Set<String> = emptySet()

    // Selección del usuario (máximo 2)
    private val jornadasSeleccionadas: MutableSet<String> = linkedSetOf()

    // Preselecciones que llegan desde el calendario (labels)
    private var jornadasPreseleccionadasLabels: List<String>? = null

    private val TAG = "EquipoReservaDebug"

    // ¿Es admin? (extra)
    private var esAdmin: Boolean = false

    // Launcher para abrir el calendario y recibir fecha + 1..2 jornadas
    private val seleccionarFechaJornada =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val data = res.data ?: return@registerForActivityResult

            val fecha = data.getStringExtra(CalendarioDisponibilidadFragment.EXTRA_FECHA) ?: return@registerForActivityResult
            val codes = data.getStringArrayListExtra(CalendarioDisponibilidadFragment.EXTRA_JORNADAS_LIST)
            val fallbackCode = data.getStringExtra(CalendarioDisponibilidadFragment.EXTRA_JORNADA) // compat

            val labels = when {
                !codes.isNullOrEmpty() -> codes.map { codeToLabel(it) }
                !fallbackCode.isNullOrEmpty() -> listOf(codeToLabel(fallbackCode!!))
                else -> emptyList()
            }

            fechaSeleccionada = fecha
            binding.tvFechaSeleccionada.text = fechaSeleccionada
            jornadasPreseleccionadasLabels = labels // aplicar tras cargar disponibilidad

            val equipo = intent.getParcelableExtra<ModeloEquipos>("equipo")
            if (equipo != null) {
                consultarDisponibilidadDelDia(equipo.id, fechaSeleccionada)
            } else {
                Toast.makeText(this, "Equipo no reconocido", Toast.LENGTH_SHORT).show()
            }
        }

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

        // Abrir calendario mensual para elegir fecha + 1..2 jornadas
        binding.btnCalendario.setOnClickListener {
            val id = equipo?.id ?: 0
            if (id == 0) {
                Toast.makeText(this, "Equipo no reconocido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val i = Intent(this, DisponibilidadActivity::class.java).apply {
                putExtra("tipo", "equipo")
                putExtra("recursoId", id)
            }
            seleccionarFechaJornada.launch(i)
        }

        // Badges (máximo 2, respetando disponibilidad)
        binding.badgeManana.setOnClickListener { onBadgeClick("Mañana", binding.badgeManana) }
        binding.badgeTarde.setOnClickListener  { onBadgeClick("Tarde",  binding.badgeTarde) }
        binding.badgeNoche.setOnClickListener  { onBadgeClick("Noche",  binding.badgeNoche) }

        // Guardar
        binding.btnGuardar.setOnClickListener {
            if (equipo == null || fechaSeleccionada.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (jornadasSeleccionadas.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos una jornada", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (jornadasLibresDelDia.isNotEmpty() && !jornadasSeleccionadas.all { it in jornadasLibresDelDia }) {
                Toast.makeText(this, "Hay jornadas no libres para la fecha seleccionada", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val usuarioId = sharedPref.getInt("id", -1)
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

            // Validar disponibilidad puntual y guardar
            RetrofitClient.instance.validarDisponibilidadEquipo(datos)
                .enqueue(object : Callback<ValidacionEquipoResponse> {

                    override fun onResponse(
                        call: Call<ValidacionEquipoResponse>,
                        response: Response<ValidacionEquipoResponse>
                    ) {
                        val body = response.body()
                        if (!response.isSuccessful || body == null) {
                            Toast.makeText(this@SolicitudReservasEquipo, "No se pudo validar disponibilidad", Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "validarDisponibilidadEquipo !isSuccessful code=${response.code()}")
                            return
                        }

                        val disponible = body.disponible == true
                        Log.d(TAG, "validarDisponibilidadEquipo disponible=$disponible detalle=${body.detalle}")

                        if (!disponible) {
                            // Mensaje más claro con detalle si viene
                            val bloqueadas = body.detalle?.filterValues { it != "libre" }?.keys?.joinToString()
                            val msg = if (!bloqueadas.isNullOrEmpty())
                                "Jornadas no disponibles: $bloqueadas"
                            else
                                "Este equipo ya tiene una reserva aceptada en esa(s) jornada(s)"
                            Toast.makeText(this@SolicitudReservasEquipo, msg, Toast.LENGTH_LONG).show()
                            return
                        }

                        // Guardar
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

        // Estado inicial de badges y textos
        pintarBadges(neutral = true)
        setBadgesEnabled(false)
        setTextSeleccion() // “Tu selección:”
        setTextDisponibilidad(null) // “Disponibilidad:”
    }

    /** Badge click (máximo 2) */
    private fun onBadgeClick(jornada: String, view: TextView) {
        if (!jornadasLibresDelDia.contains(jornada)) {
            Toast.makeText(this, "La jornada '$jornada' no está disponible ese día", Toast.LENGTH_SHORT).show()
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
        setTextSeleccion()
    }

    /** Consulta disponibilidad de 1 día y actualiza UI. */
// REEMPLAZA COMPLETO:
    private fun consultarDisponibilidadDelDia(elementoId: Int, fecha: String) {
        binding.pbDisponibilidad.visibility = View.VISIBLE
        setTextDisponibilidad("Consultando disponibilidad...")

        RetrofitClient.instance.disponibilidadEquipoDia(
            elementoId = elementoId,
            fecha = fecha
        ).enqueue(object : Callback<DisponibilidadEquipoDiaResponse> {

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
                    setTextDisponibilidad("Sin datos de disponibilidad") // OJO: msg = "..." si usas named arg
                    setTextSeleccion()
                    return
                }

                val body = response.body()!!
                val dia = body.dias.firstOrNull()
                // OJO: ahora es libreEn (camelCase), no libre_en
                jornadasLibresDelDia = dia?.libre_en?.map { it.trim() }?.toSet() ?: emptySet()

                // Aplica preselección (si vino desde el calendario)
                jornadasPreseleccionadasLabels?.let { pres ->
                    jornadasSeleccionadas.clear()
                    pres.forEach { label ->
                        if (jornadasSeleccionadas.size < 2 && jornadasLibresDelDia.contains(label)) {
                            jornadasSeleccionadas.add(label)
                        }
                    }
                    jornadasPreseleccionadasLabels = null
                } ?: run {
                    // Si no vino nada preseleccionado, limpia lo que ya no esté libre
                    jornadasSeleccionadas.retainAll(jornadasLibresDelDia)
                }

                // UI
                pintarBadges(neutral = false)
                setBadgesEnabled(true)
                setTextDisponibilidadByLibres()
                setTextSeleccion()

                // Si tienes el extra de admin, puedes dejar esta llamada
                if (esAdmin) {
                    cargarReservasDeUsuariosAdmin(elementoId, fecha)
                }
            }

            override fun onFailure(
                call: Call<DisponibilidadEquipoDiaResponse>,
                t: Throwable
            ) {
                binding.pbDisponibilidad.visibility = View.GONE
                jornadasLibresDelDia = emptySet()
                jornadasSeleccionadas.clear()
                pintarBadges(neutral = true)
                setBadgesEnabled(false)
                setTextDisponibilidad("Sin datos de disponibilidad")
                setTextSeleccion()
                Toast.makeText(this@SolicitudReservasEquipo, "Error de red al consultar disponibilidad", Toast.LENGTH_SHORT).show()
            }
        })
    }


    /** Textos auxiliares */
    private fun setTextDisponibilidad(msg: String?) {
        binding.tvEstadoDia.text = msg ?: ""
    }

    private fun setTextDisponibilidadByLibres() {
        val todas = setOf("Mañana","Tarde","Noche")
        val msg = when {
            jornadasLibresDelDia.isEmpty() -> "Disponibilidad: sin jornadas libres"
            jornadasLibresDelDia == todas -> "Disponibilidad: libre todo el día"
            else -> "Disponibilidad: ${jornadasLibresDelDia.joinToString()}"
        }
        setTextDisponibilidad(msg)
    }

    private fun setTextSeleccion() {
        val txt = if (jornadasSeleccionadas.isEmpty())
            "Tu selección: (ninguna)"
        else
            "Tu selección: ${jornadasSeleccionadas.joinToString()}"
        // Usa un TextView en tu layout para esto. Si no lo tienes,
        // puedes concatenarlo a tvEstadoDia con un salto de línea:
        // binding.tvEstadoDia.text = (binding.tvEstadoDia.text.toString().lineSequence().firstOrNull() ?: "") + "\n" + txt
        // Recomiendo añadir un TextView:
        // <TextView android:id=\"@+id/tvSeleccionDia\" .../>
        if (binding.root.findViewById<View?>(R.id.tvSeleccionDia) is TextView) {
            (binding.root.findViewById(R.id.tvSeleccionDia) as TextView).text = txt
        } else {
            // fallback: debajo del estado
            binding.tvEstadoDia.text = binding.tvEstadoDia.text.toString().split("\n").firstOrNull().orEmpty() + "\n" + txt
        }
    }

    /** Pintado de badges */
    private fun pintarBadges(neutral: Boolean) {
        if (neutral) {
            paint(binding.badgeManana, R.drawable.bg_badge_neutral)
            paint(binding.badgeTarde,  R.drawable.bg_badge_neutral)
            paint(binding.badgeNoche,  R.drawable.bg_badge_neutral)
            return
        }
        fun bgFor(j: String, view: TextView): Int {
            val seleccionado = jornadasSeleccionadas.contains(j)
            val libre = jornadasLibresDelDia.contains(j)
            return when {
                !libre -> R.drawable.bg_badge_bad
                seleccionado -> R.drawable.bg_badge_ok
                else -> R.drawable.bg_badge_neutral
            }
        }
        paint(binding.badgeManana, bgFor("Mañana", binding.badgeManana))
        paint(binding.badgeTarde,  bgFor("Tarde",  binding.badgeTarde))
        paint(binding.badgeNoche,  bgFor("Noche",  binding.badgeNoche))
    }

    private fun setBadgesEnabled(enabledFromData: Boolean) {
        // Regla: si ya seleccionaste 2, solo esos 2 quedan habilitados (para poder deseleccionar).
        val maxReached = jornadasSeleccionadas.size >= 2

        fun apply(label: String, v: TextView) {
            val available = jornadasLibresDelDia.contains(label)
            val selected  = jornadasSeleccionadas.contains(label)

            val enabled = when {
                !enabledFromData        -> false                   // sin datos → todo deshabilitado
                !available              -> false                   // no está libre → deshabilitado
                maxReached && !selected -> false                   // tope 2 alcanzado → solo los seleccionados quedan habilitados
                else                    -> true                    // libre y (no tope, o seleccionado)
            }

            v.isEnabled = enabled
            v.alpha = if (enabled) 1f else .5f
        }

        apply("Mañana", binding.badgeManana)
        apply("Tarde",  binding.badgeTarde)
        apply("Noche",  binding.badgeNoche)
    }


    private fun setBadgeSelected(view: TextView, selected: Boolean) {
        val bg = if (selected) R.drawable.bg_badge_ok else R.drawable.bg_badge_neutral
        view.setBackgroundResource(bg)
    }

    private fun paint(v: TextView, bg: Int) { v.setBackgroundResource(bg) }

    private fun codeToLabel(j: String) = when (j.uppercase(Locale.getDefault())) {
        "M" -> "Mañana"
        "T" -> "Tarde"
        "N" -> "Noche"
        else -> j
    }

    // -------- EXTRA: admins ven quién reservó (ver paso 3) ----------
    private fun cargarReservasDeUsuariosAdmin(elementoId: Int, fecha: String) {
        // Si agregas el TextView:
        // <TextView android:id="@+id/tvReservasAdmin" ... android:visibility="gone"/>
        val tv = binding.root.findViewById<TextView?>(R.id.tvReservasAdmin) ?: return

        // Requiere un endpoint (abajo te dejo la interfaz). Descomenta cuando lo tengas:
        /*
        RetrofitClient.instance.reservasEquipoDia(elementoId, fecha)
            .enqueue(object: Callback<ReservasEquipoDiaResponse> {
                override fun onResponse(
                    call: Call<ReservasEquipoDiaResponse>,
                    response: Response<ReservasEquipoDiaResponse>
                ) {
                    if (!response.isSuccessful || response.body() == null) {
                        tv.visibility = View.GONE
                        return
                    }
                    val listado = response.body()!!.reservas
                    if (listado.isEmpty()) {
                        tv.visibility = View.GONE
                    } else {
                        val texto = listado.joinToString(separator = "\n") {
                            "- ${it.usuario} (${it.jornada})"
                        }
                        tv.text = "Reservas del día:\n$texto"
                        tv.visibility = View.VISIBLE
                    }
                }
                override fun onFailure(call: Call<ReservasEquipoDiaResponse>, t: Throwable) {
                    tv.visibility = View.GONE
                }
            })
         */
    }
}
