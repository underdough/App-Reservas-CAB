package com.amkj.appreservascab

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amkj.appreservascab.Modelos.ModeloAmbientes
import com.amkj.appreservascab.Modelos.ModeloEquipos
import com.amkj.appreservascab.Modelos.ModeloReserva
import com.amkj.appreservascab.Modelos.ModeloReservaEquipo
import com.amkj.appreservascab.databinding.ActivitySolicitudReservasBinding
import com.amkj.appreservascab.databinding.ActivitySolicitudReservasEquipoBinding
import com.amkj.appreservascab.servicios.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import okhttp3.ResponseBody

class SolicitudReservasEquipo : AppCompatActivity() {
    private lateinit var binding: ActivitySolicitudReservasEquipoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitudReservasEquipoBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainlySolicitudReservasEquipo)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val equipo = intent.getParcelableExtra<ModeloEquipos>("equipo")
        binding.tvInfoMarca.text = equipo?.marca
        binding.tvInfoReferencia.text= equipo?.modelo



        val sharedPref = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
        val solicitante = sharedPref.getString("nombre", null)


        binding.tvNomUser.text = solicitante


        val checkbBoxes = listOf(
            binding.checkMaAna,
            binding.checkTarde,
            binding.checkNoche
        )

        for (checkBox in checkbBoxes) {
            checkBox.setOnCheckedChangeListener { _, _ ->
                val seleccionadas = checkbBoxes.count { it.isChecked }
                if (seleccionadas > 1) {
                    checkBox.isChecked = false
                    Toast.makeText(
                        this,
                        "Solo puedes seleccionar hasta 2 jornadas",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.btnCalendario.setOnClickListener {
            val calendar = Calendar.getInstance()

            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val fechaSeleccionada = Calendar.getInstance()
                    fechaSeleccionada.set(year, month, dayOfMonth)

                    // Formatea la fecha seleccionada
                    val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaFormateada = formatoFecha.format(fechaSeleccionada.time)

                    // Muestra la fecha seleccionada en un TextView (puedes crear uno si deseas)
                    binding.tvFechaSeleccionada.text = "Reserva la fecha: $fechaFormateada"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Deshabilitar fechas anteriores
            datePicker.datePicker.minDate = System.currentTimeMillis()

            datePicker.show()
        }


        binding.btnGuardar.setOnClickListener {
            val equipo = intent.getParcelableExtra<ModeloEquipos>("marca")
            val solicitante = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
                .getString("nombre", "") ?: ""
            val fecha =
                binding.tvFechaSeleccionada.text.toString().removePrefix("Reserva la fecha: ")
                    .trim()

            val jornadas = mutableListOf<String>()
            if (binding.checkMaAna.isChecked) jornadas.add("Mañana")
            if (binding.checkTarde.isChecked) jornadas.add("Tarde")
            if (binding.checkNoche.isChecked) jornadas.add("Noche")

            if (fecha.isEmpty() || jornadas.isEmpty() || equipo == null) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reserva = ModeloReservaEquipo(
                marca  = equipo.marca,
                modelo = equipo.modelo,
                nombre = solicitante,
                fecha = fecha,
                jornadas = jornadas,
            )

            val call = RetrofitClient.instance.guardarReservaEquipo(reserva)
            call.enqueue(object : retrofit2.Callback<ResponseBody> {
                override fun onResponse(
                    call: retrofit2.Call<ResponseBody>,
                    response: retrofit2.Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@SolicitudReservasEquipo,
                            "Reserva exitosa",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@SolicitudReservasEquipo,
                            "Error en el servidor",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Log.d("ReservaDebug", "Cuerpo: ${response.body()}")
                    Log.d("ReservaDebug", "Código: ${response.code()}")
                    Log.d("ReservaDebug", "ErrorBody: ${response.errorBody()?.string()}")

                }

                override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@SolicitudReservasEquipo, "Fallo: ${t.message}", Toast.LENGTH_LONG)
                        .show()
                }
            })


        }
        binding.ibAtras.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


    }
}