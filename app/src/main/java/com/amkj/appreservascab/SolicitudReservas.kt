package com.amkj.appreservascab

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
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

    // Memoria de disponibilidad por día (fecha -> libre_en)
    private val disponibilidadPorDia: MutableMap<String, List<String>> = mutableMapOf()

    // Jornadas libres en TODOS los días del rango
    private var jornadasLibresEnTodoElRango: Set<String> = emptySet()

    // Jornadas seleccionadas por el usuario (máx 2)
    private val jornadasSeleccionadas: MutableSet<String> = linkedSetOf()

    private val TAG = "SolicitudReservasAmb"

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

        // Estado inicial badges
        pintarBadges(neutral = true)
        setBadgesEnabled(false)
        binding.pbDisponibilidad.visibility = View.GONE
        binding.tvEstadoRango.text = "Selecciona fecha inicio y fin para ver disponibilidad"

        // Pickers de fecha/hora
        binding.btnCalendarioInicio.setOnClickListener {
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    fechaInicioSeleccionada = String.format("%04d-%02d-%02d", y, m + 1, d)
                    binding.tvFechaInicio.text = fechaInicioSeleccionada
                    // Reset UI de disponibilidad
                    jornadasSeleccionadas.clear()
                    pintarBadges(neutral = true)
                    setBadgesEnabled(false)
                    binding.pbDisponibilidad.visibility = View.VISIBLE
                    binding.tvEstadoRango.text = "Consultando disponibilidad..."
                    intentarActualizarDisponibilidad(ambiente)
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

        binding.btnCalendarioFin.setOnClickListener {
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    fechaFinSeleccionada = String.format("%04d-%02d-%02d", y, m + 1, d)
                    binding.tvFechaFin.text = fechaFinSeleccionada
                    // Reset UI de disponibilidad
                    jornadasSeleccionadas.clear()
                    pintarBadges(neutral = true)
                    setBadgesEnabled(false)
                    binding.pbDisponibilidad.visibility = View.VISIBLE
                    binding.tvEstadoRango.text = "Consultando disponibilidad..."
                    intentarActualizarDisponibilidad(ambiente)
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

        // Clicks de badges
        binding.badgeManana.setOnClickListener { onBadgeClick("Mañana", binding.badgeManana) }
        binding.badgeTarde.setOnClickListener  { onBadgeClick("Tarde",  binding.badgeTarde) }
        binding.badgeNoche.setOnClickListener  { onBadgeClick("Noche",  binding.badgeNoche) }

        // Guardar
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

            // Validar vs disponibilidad del rango (intersección)
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

            // Validación puntual (día inicio) para compatibilidad con tu backend actual
            val datos = SolicitudDisponibilidadRequest(
                ambiente_id = ambiente.id,
                fecha = fechaInicioSeleccionada,
                jornadas = reserva.jornadas ?: ""
            )

            Log.d(TAG, "Validando DISP puntual con: $datos")
            RetrofitClient.instance.validarDisponibilidadAmbiente(datos)
                .enqueue(object : Callback<Map<String, Boolean>> {
                    override fun onResponse(
                        call: Call<Map<String, Boolean>>,
                        response: Response<Map<String, Boolean>>
                    ) {
                        val disponible = response.body()?.get("disponible") ?: false
                        Log.d(TAG, "Resp validarDisponibilidadAmbiente=$disponible, code=${response.code()}")

                        if (!disponible) {
                            Toast.makeText(this@SolicitudReservas, "Este ambiente ya tiene una reserva aceptada en esa jornada (día de inicio)", Toast.LENGTH_SHORT).show()
                            return
                        }

                        RetrofitClient.instance.guardarReserva(reserva)
                            .enqueue(object : Callback<ResponseBody> {
                                override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                                    Log.d(TAG, "GuardarReserva code=${resp.code()}")
                                    if (resp.isSuccessful) {
                                        Toast.makeText(this@SolicitudReservas, "Reserva exitosa", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        Toast.makeText(this@SolicitudReservas, "No se pudo guardar la reserva", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                    Log.e(TAG, "Error al guardar", t)
                                    Toast.makeText(this@SolicitudReservas, "Error al guardar: ${t.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }

                    override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                        Log.e(TAG, "Error validarDisponibilidadAmbiente", t)
                        Toast.makeText(this@SolicitudReservas, "Error de red al validar disponibilidad", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        binding.ibAtras.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    /**
     * Consulta la disponibilidad del RANGO y actualiza la UI (badges) según intersección.
     */
    private fun intentarActualizarDisponibilidad(ambiente: ModeloAmbientes?) {
        if (ambiente == null) return
        if (fechaInicioSeleccionada.isEmpty() || fechaFinSeleccionada.isEmpty()) return

        Log.d(TAG, "Consultando disponibilidadAmbiente rango: $fechaInicioSeleccionada..$fechaFinSeleccionada (ambiente ${ambiente.id})")

        RetrofitClient.instance.disponibilidadAmbiente(
            ambienteId = ambiente.id,
            desde = fechaInicioSeleccionada,
            hasta = fechaFinSeleccionada
        ).enqueue(object : Callback<DisponibilidadResponse> {
            override fun onResponse(
                call: Call<DisponibilidadResponse>,
                response: Response<DisponibilidadResponse>
            ) {
                binding.pbDisponibilidad.visibility = View.GONE

                if (!response.isSuccessful) {
                    Log.w(TAG, "Resp disponibilidadAmbiente no OK: code=${response.code()}")
                    Toast.makeText(this@SolicitudReservas, "No fue posible obtener disponibilidad", Toast.LENGTH_SHORT).show()
                    binding.tvEstadoRango.text = "Sin datos de disponibilidad"
                    pintarBadges(neutral = true)
                    setBadgesEnabled(false)
                    return
                }

                val body = response.body()
                if (body == null) {
                    Log.w(TAG, "Resp disponibilidadAmbiente sin body")
                    Toast.makeText(this@SolicitudReservas, "No fue posible obtener disponibilidad", Toast.LENGTH_SHORT).show()
                    binding.tvEstadoRango.text = "Sin datos de disponibilidad"
                    pintarBadges(neutral = true)
                    setBadgesEnabled(false)
                    return
                }

                // (fecha -> libre_en)
                disponibilidadPorDia.clear()
                body.dias.forEach { d ->
                    disponibilidadPorDia[d.fecha] = d.libre_en
                }

                // Intersección de jornadas libres en TODOS los días
                jornadasLibresEnTodoElRango = calcularInterseccion(body.dias.map { it.libre_en })
                Log.d(TAG, "Jornadas libres en TODO el rango: $jornadasLibresEnTodoElRango")

                // Limpiar selecciones que ya no apliquen
                jornadasSeleccionadas.retainAll(jornadasLibresEnTodoElRango)

                // Actualiza UI de badges
                pintarBadges(neutral = false)
                setBadgesEnabled(true)

                val todas = setOf("Mañana", "Tarde", "Noche")
                binding.tvEstadoRango.text = when {
                    jornadasLibresEnTodoElRango.isEmpty() -> "Sin jornadas libres en TODO el rango"
                    jornadasLibresEnTodoElRango == todas  -> "Libre TODO el rango (todas las jornadas)"
                    else -> "Libre en TODO el rango: ${jornadasLibresEnTodoElRango.joinToString()}"
                }
            }

            override fun onFailure(call: Call<DisponibilidadResponse>, t: Throwable) {
                binding.pbDisponibilidad.visibility = View.GONE
                Log.e(TAG, "Error disponibilidadAmbiente", t)
                Toast.makeText(this@SolicitudReservas, "Error de red al consultar disponibilidad: ${t.message}", Toast.LENGTH_SHORT).show()
                binding.tvEstadoRango.text = "Sin datos de disponibilidad"
                pintarBadges(neutral = true)
                setBadgesEnabled(false)
            }
        })
    }

    // Intersección de listas de jornadas
    private fun calcularInterseccion(listas: List<List<String>>): Set<String> {
        if ( listas.isEmpty() ) return emptySet()
        var inter = listas.first().map { it.trim() }.toSet()
        listas.drop(1).forEach { l ->
            inter = inter.intersect(l.map { it.trim() }.toSet())
        }
        return inter
    }

    /** Badges: selección y pintado **/
    private fun onBadgeClick(jornada: String, view: TextView) {
        // Solo se puede seleccionar si está libre en TODO el rango
        if (jornadasLibresEnTodoElRango.isNotEmpty() && jornada !in jornadasLibresEnTodoElRango) {
            Toast.makeText(this, "La jornada '$jornada' no está libre en TODO el rango", Toast.LENGTH_SHORT).show()
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
    }

    private fun pintarBadges(neutral: Boolean) {
        if (neutral) {
            paint(binding.badgeManana, R.drawable.bg_badge_neutral)
            paint(binding.badgeTarde,  R.drawable.bg_badge_neutral)
            paint(binding.badgeNoche,  R.drawable.bg_badge_neutral)
            return
        }
        fun bgFor(j: String, view: TextView): Int {
            val seleccionado = j in jornadasSeleccionadas
            val libre = j in jornadasLibresEnTodoElRango
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
        binding.badgeManana.isEnabled = enabledFromData && ("Mañana" in jornadasLibresEnTodoElRango)
        binding.badgeTarde.isEnabled  = enabledFromData && ("Tarde"  in jornadasLibresEnTodoElRango)
        binding.badgeNoche.isEnabled  = enabledFromData && ("Noche"  in jornadasLibresEnTodoElRango)

        binding.badgeManana.alpha = if (binding.badgeManana.isEnabled) 1f else .5f
        binding.badgeTarde.alpha  = if (binding.badgeTarde.isEnabled) 1f else .5f
        binding.badgeNoche.alpha  = if (binding.badgeNoche.isEnabled) 1f else .5f
    }

    private fun setBadgeSelected(view: TextView, selected: Boolean) {
        view.setBackgroundResource(if (selected) R.drawable.bg_badge_ok else R.drawable.bg_badge_neutral)
    }

    private fun paint(v: TextView, bg: Int) { v.setBackgroundResource(bg) }
}
