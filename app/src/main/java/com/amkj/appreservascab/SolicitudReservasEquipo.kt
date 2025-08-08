package com.amkj.appreservascab

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amkj.appreservascab.Modelos.*
import com.amkj.appreservascab.databinding.ActivitySolicitudReservasEquipoBinding
import com.amkj.appreservascab.servicios.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SolicitudReservasEquipo : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitudReservasEquipoBinding
    private var fechaSeleccionada: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitudReservasEquipoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("EquipoReservaDebug", "Pantalla cargada correctamente")

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainlySolicitudReservasEquipo) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val equipo = intent.getParcelableExtra<ModeloEquipos>("equipo")
        val sharedPref = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
        val nombreUsuario = sharedPref.getString("nombre", "") ?: ""

        if (equipo != null) {
            Log.d("EquipoReservaDebug", "Equipo recibido: ${equipo.descripcion} (${equipo.id})")
            binding.tvInfoMarca.text = equipo.marca
            binding.tvInfoReferencia.text = equipo.modelo
            binding.tvNombreEquipo.text = equipo.descripcion
            binding.tvNomUser.text = nombreUsuario
        } else {
            Log.e("EquipoReservaDebug", "No se recibió equipo en el intent")
        }

        binding.btnCalendario.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                fechaSeleccionada = String.format("%04d-%02d-%02d", year, month + 1, day)
                binding.tvFechaSeleccionada.text = fechaSeleccionada
                Log.d("EquipoReservaDebug", "Fecha seleccionada: $fechaSeleccionada")
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        val checkBoxes = listOf(binding.checkManana, binding.checkTarde, binding.checkNoche)
        checkBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, _ ->
                if (checkBoxes.count { it.isChecked } > 2) {
                    checkBox.isChecked = false
                    Toast.makeText(this, "Máximo 2 jornadas permitidas", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnGuardar.setOnClickListener {
            Log.d("EquipoReservaDebug", "Botón guardar presionado")

            if (equipo == null || fechaSeleccionada.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                Log.e("EquipoReservaDebug", "Campos faltantes: equipo=${equipo != null}, fecha=$fechaSeleccionada")
                return@setOnClickListener
            }

            val jornadas = mutableListOf<String>()
            if (binding.checkManana.isChecked) jornadas.add("Mañana")
            if (binding.checkTarde.isChecked) jornadas.add("Tarde")
            if (binding.checkNoche.isChecked) jornadas.add("Noche")

            if (jornadas.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos una jornada", Toast.LENGTH_SHORT).show()
                Log.e("EquipoReservaDebug", "Ninguna jornada seleccionada")
                return@setOnClickListener
            }

            val usuarioId = sharedPref.getInt("id", -1)
            Log.d("EquipoReservaDebug", "ID usuario: $usuarioId")

            val reservaRequest = ReservaEquipoRequest(
                elemento_id = equipo.id,
                usuario_id = usuarioId,
                fecha = fechaSeleccionada,
                jornadas = jornadas.joinToString(", "),
                motivo = "Reserva de equipo",
                estado = "pendiente"
            )

            val datos = ValidacionEquipoRequest(
                equipo_id = equipo.id,
                fecha = fechaSeleccionada,
                jornadas = reservaRequest.jornadas
            )

            val gson = com.google.gson.Gson()
            Log.d("EquipoReservaDebug", "Solicitud Validación JSON: ${gson.toJson(datos)}")

            RetrofitClient.instance.validarDisponibilidadEquipo(datos)
                .enqueue(object : Callback<Map<String, Boolean>> {
                    override fun onResponse(call: Call<Map<String, Boolean>>, response: Response<Map<String, Boolean>>) {
                        Log.d("EquipoReservaDebug", "Código respuesta validación: ${response.code()}")
                        Log.d("EquipoReservaDebug", "Cuerpo validación: ${response.body()}")
                        Log.d("EquipoReservaDebug", "ErrorBody validación: ${response.errorBody()?.string()}")

                        val disponible = response.body()?.get("disponible") ?: false
                        if (!disponible) {
                            Toast.makeText(
                                this@SolicitudReservasEquipo,
                                "Este equipo ya tiene una reserva aceptada en esa jornada",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("EquipoReservaDebug", "Resultado validación: NO DISPONIBLE")
                            return
                        }

                        Log.d("EquipoReservaDebug", "Resultado validación: DISPONIBLE")
                        Log.d("EquipoReservaDebug", "ReservaEquipo JSON a enviar: ${gson.toJson(reservaRequest)}")

                        RetrofitClient.instance.guardarReservaEquipo(reservaRequest)
                            .enqueue(object : Callback<ResponseBody> {
                                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                    val bodyString = response.body()?.string()
                                    Log.d("EquipoReservaDebug", "Código guardarReservaEquipo: ${response.code()}")
                                    Log.d("EquipoReservaDebug", "Body guardarReservaEquipo: $bodyString")

                                    if (response.isSuccessful) {
                                        Toast.makeText(
                                            this@SolicitudReservasEquipo,
                                            "Reserva enviada con éxito",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.d("EquipoReservaDebug", "Reserva enviada correctamente")
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this@SolicitudReservasEquipo,
                                            "Error al guardar reserva",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.e("EquipoReservaDebug", "Error HTTP guardarReservaEquipo: ${response.code()} - $bodyString")
                                    }
                                }

                                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                    Log.e("EquipoReservaDebug", "Fallo en guardar reserva: ${t.message}", t)
                                    Toast.makeText(
                                        this@SolicitudReservasEquipo,
                                        "Error al guardar: ${t.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                    }

                    override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                        Log.e("EquipoReservaDebug", "Fallo en validación: ${t.message}", t)
                        Toast.makeText(
                            this@SolicitudReservasEquipo,
                            "Error de red al validar disponibilidad",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }

        binding.ibAtras.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
