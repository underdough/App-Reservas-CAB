package com.amkj.appreservascab

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.amkj.appreservascab.Adaptadores.AdapterReservas
import com.amkj.appreservascab.Modelos.ModeloReserva
import com.amkj.appreservascab.Modelos.ModeloReservaEquipo
import com.amkj.appreservascab.databinding.ActivityInstruMisReservasBinding
import com.amkj.appreservascab.servicios.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InstruMisReservas : AppCompatActivity() {

    private lateinit var binding: ActivityInstruMisReservasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInstruMisReservasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPref = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
        val usuarioId = sharedPref.getInt("id", -1)

        if (usuarioId == -1) {
            Toast.makeText(this, "No se pudo obtener el ID del usuario", Toast.LENGTH_SHORT).show()
            return
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        val reservasUnificadas = mutableListOf<ModeloReserva>()

        // ✅ Llamada a ambientes
        RetrofitClient.instance.obtenerReservas(mapOf("usuario_id" to usuarioId))
            .enqueue(object : Callback<List<ModeloReserva>> {
                override fun onResponse(
                    call: Call<List<ModeloReserva>>,
                    response: Response<List<ModeloReserva>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        reservasUnificadas.addAll(response.body()!!)
                    } else {
                        Toast.makeText(this@InstruMisReservas, "Error al obtener reservas de ambientes", Toast.LENGTH_SHORT).show()
                    }
                    cargarReservasEquipos(usuarioId, reservasUnificadas)
                }

                override fun onFailure(call: Call<List<ModeloReserva>>, t: Throwable) {
                    Toast.makeText(this@InstruMisReservas, "Fallo conexión (ambientes): ${t.message}", Toast.LENGTH_SHORT).show()
                    cargarReservasEquipos(usuarioId, reservasUnificadas)
                }
            })

        binding.ibVolver.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun cargarReservasEquipos(usuarioId: Int, listaReservas: MutableList<ModeloReserva>) {
        val body = HashMap<String, Int>() // ✅ evitar tipo genérico inválido
        body["usuario_id"] = usuarioId

        RetrofitClient.instance.obtenerReservasEquipo(body)
            .enqueue(object : Callback<List<ModeloReservaEquipo>> {
                override fun onResponse(
                    call: Call<List<ModeloReservaEquipo>>,
                    response: Response<List<ModeloReservaEquipo>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val reservasEquipoConvertidas = response.body()!!.map {
                            ModeloReserva(
                                usuario_id = it.usuario_id,
                                ambiente_id = it.equipo_id, // usamos equipo_id como ambiente_id por compatibilidad
                                fecha_hora_inicio = it.fecha ?: "Sin fecha",
                                fecha_hora_fin = it.fecha ?: "Sin fecha",
                                motivo = it.motivo,
                                jornadas = it.jornadas,
                                ambiente_nombre = it.equipo_nombre,
                                ambiente_imagen = it.equipo_imagen,
                                estado = it.estado
                            )
                        }

                        listaReservas.addAll(reservasEquipoConvertidas)
                    } else {
                        Toast.makeText(this@InstruMisReservas, "Error al obtener reservas de equipos", Toast.LENGTH_SHORT).show()
                    }

                    // ✅ Mostrar todas las reservas (ambientes + equipos)
                    if (listaReservas.isNotEmpty()) {
                        binding.recyclerView.adapter = AdapterReservas(listaReservas)
                    } else {
                        Toast.makeText(this@InstruMisReservas, "No tienes reservas registradas", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<ModeloReservaEquipo>>, t: Throwable) {
                    Toast.makeText(this@InstruMisReservas, "Fallo conexión (equipos): ${t.message}", Toast.LENGTH_SHORT).show()

                    // Mostrar solo lo que ya se tenga
                    if (listaReservas.isNotEmpty()) {
                        binding.recyclerView.adapter = AdapterReservas(listaReservas)
                    } else {
                        Toast.makeText(this@InstruMisReservas, "No tienes reservas registradas", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }
}
