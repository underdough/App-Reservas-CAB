package com.amkj.appreservascab

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amkj.appreservascab.databinding.ActivitySolicitudReservasBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SolicitudReservas : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitudReservasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitudReservasBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainlySolicitudReservas)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPref = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
        val solicitante = sharedPref.getString("nombre", null)

        binding.tvSolicitante.text = solicitante


        val checkbBoxes = listOf(
            binding.checkMaAna,
            binding.checkTarde,
            binding.checkNoche
        )

        for (checkBox in checkbBoxes) {
            checkBox.setOnCheckedChangeListener {_, _ ->
                val seleccionadas = checkbBoxes.count {it.isChecked}
                if (seleccionadas > 2){
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
            TODO()
        }

        binding.btnCancelar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }



}