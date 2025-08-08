package com.amkj.appreservascab

import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.amkj.appreservascab.Adaptadores.AdapterReservas
import com.amkj.appreservascab.Modelos.EliminarReservaRequest
import com.amkj.appreservascab.Modelos.ModeloReserva
import com.amkj.appreservascab.Modelos.ModeloReservaEquipo
import com.amkj.appreservascab.databinding.ActivityInstruMisReservasBinding
import com.amkj.appreservascab.servicios.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InstruMisReservas : AppCompatActivity() {

    private lateinit var binding: ActivityInstruMisReservasBinding
    private lateinit var adapter: AdapterReservas
    private var reservasUnificadas = mutableListOf<ModeloReserva>()
    private var listaReservasOriginal = listOf<ModeloReserva>()
    private var usuarioId = -1

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
        usuarioId = sharedPref.getInt("id", -1)
        if (usuarioId == -1) {
            Toast.makeText(this, "No se pudo obtener el ID del usuario", Toast.LENGTH_SHORT).show()
            return
        }

        adapter = AdapterReservas(reservasUnificadas) { reserva ->
            eliminarReserva(reserva)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        cargarReservas()

        // Botón volver
        binding.ibVolver.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Búsqueda
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarReservas(newText.orEmpty())
                return true
            }
        })
    }

    private fun cargarReservas() {
        reservasUnificadas.clear()

        RetrofitClient.instance.obtenerReservas(mapOf("usuario_id" to usuarioId))
            .enqueue(object : Callback<List<ModeloReserva>> {
                override fun onResponse(call: Call<List<ModeloReserva>>, response: Response<List<ModeloReserva>>) {
                    if (response.isSuccessful && response.body() != null) {
                        val reservasAmbientes = response.body()!!.map {
                            it.copy(tipo_reserva = "ambiente")
                        }
                        reservasUnificadas.addAll(reservasAmbientes)
                    }
                    cargarReservasEquipos()
                }

                override fun onFailure(call: Call<List<ModeloReserva>>, t: Throwable) {
                    Toast.makeText(this@InstruMisReservas, "Fallo conexión (ambientes)", Toast.LENGTH_SHORT).show()
                    cargarReservasEquipos()
                }
            })
    }

    private fun cargarReservasEquipos() {
        RetrofitClient.instance.obtenerReservasEquipo(mapOf("usuario_id" to usuarioId))
            .enqueue(object : Callback<List<ModeloReservaEquipo>> {
                override fun onResponse(call: Call<List<ModeloReservaEquipo>>, response: Response<List<ModeloReservaEquipo>>) {
                    if (response.isSuccessful && response.body() != null) {
                        val reservasEquipo = response.body()!!.map {
                            ModeloReserva(
                                id_reserva = it.id_reserva,
                                usuario_id = it.usuario_id,
                                ambiente_id = it.equipo_id,
                                fecha_hora_inicio = it.fecha ?: "Sin fecha",
                                fecha_hora_fin = it.fecha ?: "Sin fecha",
                                motivo = it.motivo,
                                jornadas = it.jornadas,
                                ambiente_nombre = it.equipo_nombre,
                                ambiente_imagen = it.equipo_imagen,
                                estado = it.estado,
                                tipo_reserva = "equipo"
                            )
                        }
                        reservasUnificadas.addAll(reservasEquipo)
                        listaReservasOriginal = reservasUnificadas.toList() // respaldo para búsqueda
                        adapter.notifyDataSetChanged()

                        if (reservasUnificadas.isEmpty()) {
                            Toast.makeText(this@InstruMisReservas, "No tienes reservas registradas", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<List<ModeloReservaEquipo>>, t: Throwable) {
                    Toast.makeText(this@InstruMisReservas, "Fallo conexión (equipos)", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun filtrarReservas(query: String) {
        val filtro = query.lowercase()
        val filtradas = listaReservasOriginal.filter {
            it.ambiente_nombre?.lowercase()?.contains(filtro) == true ||
                    it.jornadas?.lowercase()?.contains(filtro) == true ||
                    it.estado?.lowercase()?.contains(filtro) == true ||
                    it.fecha_hora_inicio?.lowercase()?.contains(filtro) == true
        }
        reservasUnificadas.clear()
        reservasUnificadas.addAll(filtradas)
        adapter.notifyDataSetChanged()
    }

    private fun eliminarReserva(reserva: ModeloReserva) {
        val body = EliminarReservaRequest(
            reserva_id = reserva.id_reserva ?: -1,
            tipo_reserva = reserva.tipo_reserva ?: "ambiente"
        )

        RetrofitClient.instance.eliminarReserva(body)
            .enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                    if (response.isSuccessful && response.body()?.get("mensaje") == "Reserva eliminada correctamente") {
                        Toast.makeText(this@InstruMisReservas, "Reserva eliminada", Toast.LENGTH_SHORT).show()
                        reservasUnificadas.remove(reserva)
                        listaReservasOriginal = listaReservasOriginal.filter { it.id_reserva != reserva.id_reserva }
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@InstruMisReservas, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    Toast.makeText(this@InstruMisReservas, "Fallo al eliminar: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
