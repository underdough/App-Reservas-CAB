package com.amkj.appreservascab

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amkj.appreservascab.Modelos.ModeloAmbientes
import com.amkj.appreservascab.Modelos.ModeloReserva
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
    private var fechaSeleccionada: String = ""

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
        val usuarioId = sharedPref.getInt("id", -1)
        val solicitanteNombre = sharedPref.getString("nombre", "") ?: ""
        binding.tvSolicitante.text = solicitanteNombre

        val checkBoxes = listOf(binding.checkMaAna, binding.checkTarde, binding.checkNoche)

        checkBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, _ ->
                if (checkBoxes.count { it.isChecked } > 2) {
                    checkBox.isChecked = false
                    Toast.makeText(this, "Solo puedes seleccionar hasta 2 jornadas", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnCalendario.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)
                    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    fechaSeleccionada = formatter.format(selectedDate.time)
                    binding.tvFechaSeleccionada.text = "Reserva la fecha: $fechaSeleccionada"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.minDate = System.currentTimeMillis()
            datePicker.show()
        }

        binding.btnGuardar.setOnClickListener {
            if (ambiente == null || fechaSeleccionada.isEmpty()) {
                Toast.makeText(this, "Faltan campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jornadas = mutableListOf<String>()
            if (binding.checkMaAna.isChecked) jornadas.add("Mañana")
            if (binding.checkTarde.isChecked) jornadas.add("Tarde")
            if (binding.checkNoche.isChecked) jornadas.add("Noche")

            if (jornadas.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos una jornada", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reserva = ModeloReserva(
                ambiente_id = ambiente.id,
                ambiente_nombre = ambiente.nombre,
                usuario_id = usuarioId,
                fecha_hora_inicio = "$fechaSeleccionada 08:00:00",
                fecha_hora_fin = "$fechaSeleccionada 18:00:00",
                jornadas = jornadas.joinToString(", ")
            )

            RetrofitClient.instance.guardarReserva(reserva)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                        val rawBody = response.body()?.string()
                        val errorBody = response.errorBody()?.string()


                        Log.d("ReservaDebug", "ID de usuario enviado: $usuarioId")

                        Log.d("ReservaDebug", "Código: ${response.code()}")
                        Log.d("ReservaDebug", "Body: $rawBody")
                        Log.d("ReservaDebug", "ErrorBody: $errorBody")

                        if (response.isSuccessful) {
                            Toast.makeText(this@SolicitudReservas, "Reserva exitosa", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@SolicitudReservas, "Error en el servidor", Toast.LENGTH_SHORT).show()
                        }


                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(this@SolicitudReservas, "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                        Log.e("ReservaDebug", "Excepción: ${t.message}", t)
                    }
                })
        }

        binding.ibAtras.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
