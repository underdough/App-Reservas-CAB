package com.amkj.appreservascab

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
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

    // Jornadas libres para la FECHA seleccionada (desde backend)
    private var jornadasLibresDelDia: Set<String> = emptySet()

    // Jornadas que el usuario ha seleccionado (máximo 2)
    private val jornadasSeleccionadas: MutableSet<String> = linkedSetOf()

    private val TAG = "EquipoReservaDebug"

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

        if (equipo != null) {
            binding.tvInfoMarca.text = equipo.marca
            binding.tvInfoReferencia.text = equipo.modelo
            binding.tvNombreEquipo.text = equipo.descripcion
            binding.tvNomUser.text = nombreUsuario
        } else {
            Log.e(TAG, "No se recibió equipo en el intent")
        }

        // Calendario → consulta disponibilidad
        binding.btnCalendario.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    fechaSeleccionada = String.format("%04d-%02d-%02d", year, month + 1, day)
                    binding.tvFechaSeleccionada.text = fechaSeleccionada
                    if (equipo != null) consultarDisponibilidadDelDia(equipo.id, fechaSeleccionada)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Clicks de badges (selección manual, máx 2, respetando disponibilidad)
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

            // Validar vs disponibilidad del día
            if (jornadasLibresDelDia.isNotEmpty() && !jornadasSeleccionadas.all { jornadasLibresDelDia.contains(it) }) {
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

            val gson = com.google.gson.Gson()
            Log.d(TAG, "Solicitud Validación JSON: ${gson.toJson(datos)}")

            // validación de disponibilidad (tu endpoint actual)
            RetrofitClient.instance.validarDisponibilidadEquipo(datos)
                .enqueue(object : Callback<Map<String, Boolean>> {
                    override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) {
                        val disponible = response.body()?.get("disponible") ?: false
                        if (!disponible) {
                            Toast.makeText(this@SolicitudReservasEquipo, "Este equipo ya tiene una reserva aceptada en esa jornada", Toast.LENGTH_SHORT).show()
                            return
                        }

                        RetrofitClient.instance.guardarReservaEquipo(reservaRequest)
                            .enqueue(object : Callback<ResponseBody> {
                                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                    if (response.isSuccessful) {
                                        Toast.makeText(this@SolicitudReservasEquipo, "Reserva enviada con éxito", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        Toast.makeText(this@SolicitudReservasEquipo, "Error al guardar reserva", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                    Toast.makeText(this@SolicitudReservasEquipo, "Error al guardar: ${t.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                    override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                        Toast.makeText(this@SolicitudReservasEquipo, "Error de red al validar disponibilidad", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        binding.ibAtras.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Estado inicial de badges
        pintarBadges(neutral = true)
        setBadgesEnabled(false)
    }

    /** Cuando se toca un badge */
    private fun onBadgeClick(jornada: String, view: TextView) {
        // Si no está habilitado (no libre), ignorar
        if (!jornadasLibresDelDia.contains(jornada)) {
            Toast.makeText(this, "La jornada '$jornada' no está disponible ese día", Toast.LENGTH_SHORT).show()
            return
        }

        if (jornadasSeleccionadas.contains(jornada)) {
            // deseleccionar
            jornadasSeleccionadas.remove(jornada)
            setBadgeSelected(view, selected = false)
        } else {
            // seleccionar con tope 2
            if (jornadasSeleccionadas.size >= 2) {
                Toast.makeText(this, "Máximo 2 jornadas permitidas", Toast.LENGTH_SHORT).show()
                return
            }
            jornadasSeleccionadas.add(jornada)
            setBadgeSelected(view, selected = true)
        }
    }

    /** Consulta disponibilidad de un único día y actualiza UI (badges) */
    private fun consultarDisponibilidadDelDia(elementoId: Int, fecha: String) {
        binding.pbDisponibilidad.visibility = View.VISIBLE
        binding.tvEstadoDia.text = "Consultando disponibilidad..."

        RetrofitClient.instance.disponibilidadEquipoDia(
            elementoId = elementoId,
            fecha = fecha
        ).enqueue(object : Callback<DisponibilidadResponse> {
            override fun onResponse(call: Call<DisponibilidadResponse>, response: Response<DisponibilidadResponse>) {
                binding.pbDisponibilidad.visibility = View.GONE

                if (!response.isSuccessful || response.body() == null) {
                    Toast.makeText(this@SolicitudReservasEquipo, "No fue posible obtener disponibilidad", Toast.LENGTH_SHORT).show()
                    jornadasLibresDelDia = emptySet()
                    jornadasSeleccionadas.clear()
                    pintarBadges(neutral = true)
                    setBadgesEnabled(false)
                    binding.tvEstadoDia.text = "Sin datos de disponibilidad"
                    return
                }

                val body = response.body()!!
                val dia = body.dias.firstOrNull()
                jornadasLibresDelDia = dia?.libre_en?.map { it.trim() }?.toSet() ?: emptySet()

                // Limpia selecciones que ya no estén libres
                jornadasSeleccionadas.retainAll(jornadasLibresDelDia)

                // Actualiza UI
                pintarBadges(neutral = false)
                setBadgesEnabled(true)

                val todas = setOf("Mañana","Tarde","Noche")
                binding.tvEstadoDia.text = when {
                    jornadasLibresDelDia.isEmpty() -> "Ocupado todo el día"
                    jornadasLibresDelDia == todas -> "Libre todo el día"
                    else -> "Libre en: ${jornadasLibresDelDia.joinToString()}"
                }
            }

            override fun onFailure(call: Call<DisponibilidadResponse>, t: Throwable) {
                binding.pbDisponibilidad.visibility = View.GONE
                Toast.makeText(this@SolicitudReservasEquipo, "Error de red al consultar disponibilidad", Toast.LENGTH_SHORT).show()
                jornadasLibresDelDia = emptySet()
                jornadasSeleccionadas.clear()
                pintarBadges(neutral = true)
                setBadgesEnabled(false)
                binding.tvEstadoDia.text = "Sin datos de disponibilidad"
            }
        })
    }

    /** Pinta badges según disponibilidad y selección.
     *  - neutral=true => todos grises
     *  - si no, libres en verde (si además están seleccionados, igual verde), ocupados en rojo
     */
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

    /** Habilita/deshabilita clicks acorde a disponibilidad */
    private fun setBadgesEnabled(enabledFromData: Boolean) {
        // Solo habilitamos click si la jornada está libre y hay datos
        binding.badgeManana.isEnabled = enabledFromData && jornadasLibresDelDia.contains("Mañana")
        binding.badgeTarde.isEnabled  = enabledFromData && jornadasLibresDelDia.contains("Tarde")
        binding.badgeNoche.isEnabled  = enabledFromData && jornadasLibresDelDia.contains("Noche")
        // opacos si están deshabilitados
        binding.badgeManana.alpha = if (binding.badgeManana.isEnabled) 1f else .5f
        binding.badgeTarde.alpha  = if (binding.badgeTarde.isEnabled) 1f else .5f
        binding.badgeNoche.alpha  = if (binding.badgeNoche.isEnabled) 1f else .5f
    }

    /** Cambia a “seleccionado” (verde) o “no seleccionado” (neutral) el badge tocado */
    private fun setBadgeSelected(view: TextView, selected: Boolean) {
        val bg = if (selected) R.drawable.bg_badge_ok else R.drawable.bg_badge_neutral
        view.setBackgroundResource(bg)
    }

    private fun paint(v: TextView, bg: Int) {
        v.setBackgroundResource(bg)
    }
}
