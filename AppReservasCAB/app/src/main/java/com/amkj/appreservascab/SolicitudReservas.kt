package com.amkj.appreservascab

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amkj.appreservascab.Modelos.*
import com.amkj.appreservascab.databinding.ActivitySolicitudReservasBinding
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
            rol = sharedPref.getString("rol", "Rol no asignado") ?: "Rol no asignado"
        )

        binding.tvSolicitante.text = usuario.nombre
        val usuarioId = usuario.id
        val calendar = Calendar.getInstance()

        binding.btnCalendarioInicio.setOnClickListener {
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    fechaInicioSeleccionada = String.format("%04d-%02d-%02d", y, m + 1, d)
                    binding.tvFechaInicio.text = fechaInicioSeleccionada
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

        val checkBoxes = listOf(binding.checkManana, binding.checkTarde, binding.checkNoche)
        checkBoxes.forEach { cb ->
            cb.setOnCheckedChangeListener { _, _ ->
                if (checkBoxes.count { it.isChecked } > 2) {
                    cb.isChecked = false
                    Toast.makeText(this, "Solo puedes seleccionar hasta 2 jornadas", Toast.LENGTH_SHORT).show()
                }
            }
        }

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

            val jornadas = mutableListOf<String>()
            if (binding.checkManana.isChecked) jornadas.add("Mañana")
            if (binding.checkTarde.isChecked) jornadas.add("Tarde")
            if (binding.checkNoche.isChecked) jornadas.add("Noche")

            if (jornadas.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos una jornada", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reserva = ModeloReserva(
                usuario_id = usuarioId,
                ambiente_id = ambiente.id,
                fecha_hora_inicio = fechaHoraInicio,
                fecha_hora_fin = fechaHoraFin,
                motivo = "Reserva de ambiente",
                jornadas = jornadas.joinToString(", "),
                id = 0,
                ambiente_nombre = ambiente.nombre,
                ambiente_imagen = null
            )

            val datos = SolicitudDisponibilidadRequest(
                ambiente_id = ambiente.id,
                fecha = fechaInicioSeleccionada,
                jornadas = reserva.jornadas ?: ""
            )

            RetrofitClient.instance.validarDisponibilidadAmbiente(datos)
                .enqueue(object : Callback<Map<String, Boolean>> {
                    override fun onResponse(
                        call: Call<Map<String, Boolean>>,
                        response: Response<Map<String, Boolean>>
                    ) {
                        val disponible = response.body()?.get("disponible") ?: false
                        if (!disponible) {
                            Toast.makeText(this@SolicitudReservas, "Este ambiente ya tiene una reserva aceptada en esa jornada", Toast.LENGTH_SHORT).show()
                            return
                        }

                        RetrofitClient.instance.guardarReserva(reserva)
                            .enqueue(object : Callback<ResponseBody> {
                                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                    if (response.isSuccessful) {
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

                    override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                        Toast.makeText(this@SolicitudReservas, "Error de red al validar disponibilidad", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
