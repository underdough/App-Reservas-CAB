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
import com.amkj.appreservascab.Modelos.ReservasQuery
import com.amkj.appreservascab.databinding.ActivityInstruMisReservasBinding
import com.amkj.appreservascab.servicios.RetrofitClient
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InstruMisReservas : AppCompatActivity() {

    private lateinit var binding: ActivityInstruMisReservasBinding
    private lateinit var adapter: AdapterReservas

    private val reservasUnificadas = mutableListOf<ModeloReserva>()
    private var listaReservasOriginal = listOf<ModeloReserva>()

    private var usuarioId = -1
    private var rolUsuario: String = ""

    // ==== LOG UTILITIES ====
    private val TAG_RESERVAS = "MIS_RESERVAS"
    private val gsonPretty: Gson = GsonBuilder().setPrettyPrinting().create()
    private fun Any.toJson(): String = try { gsonPretty.toJson(this) } catch (_: Exception) { this.toString() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInstruMisReservasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Insets (barra de estado / navegación)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Preferencias
        val sharedPref = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
        usuarioId = sharedPref.getInt("id", -1)
        rolUsuario = (sharedPref.getString("rol", "") ?: "").lowercase().trim()

        Log.d(TAG_RESERVAS, "usuarioId(leído)= $usuarioId, rol(leído)= '$rolUsuario'")
        if (usuarioId == -1) {
            Toast.makeText(this, "No se pudo obtener el ID del usuario", Toast.LENGTH_SHORT).show()
            return
        }
        if (rolUsuario.isBlank()) {
            rolUsuario = "aprendiz" // fallback sensato
            Log.w(TAG_RESERVAS, "rol vacío en SharedPrefs → usando '$rolUsuario'")
        }

        // Recycler
        adapter = AdapterReservas(reservasUnificadas) { reserva ->
            eliminarReserva(reserva)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Cargar data
        cargarReservas()

        // UI listeners
        binding.ibVolver.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarReservas(newText.orEmpty())
                return true
            }
        })
    }

    /** Body estándar para ambos endpoints, acorde al backend actual (sin instructor_id). */
    private fun buildParamsForRole(): ReservasQuery {
        return ReservasQuery(
            usuario_id = usuarioId,
            rol = rolUsuario
        )
    }

    private fun cargarReservas() {
        reservasUnificadas.clear()

        val paramsAmb = buildParamsForRole()
        Log.d(TAG_RESERVAS, "➡️ obtenerReservas(ambientes) payload=$paramsAmb")

        RetrofitClient.instance.obtenerReservas(paramsAmb)
            .enqueue(object : Callback<List<ModeloReserva>> {
                override fun onResponse(
                    call: Call<List<ModeloReserva>>,
                    response: Response<List<ModeloReserva>>
                ) {
                    Log.d(TAG_RESERVAS, "⬅️ ambientes code=${response.code()} ok=${response.isSuccessful}")
                    if (!response.isSuccessful) {
                        Log.e(TAG_RESERVAS, "ambientes.errorBody=${response.errorBody()?.string()}")
                    }

                    val body = response.body().orEmpty()
                    Log.d(TAG_RESERVAS, "ambientes.size=${body.size}")
                    if (body.isNotEmpty()) {
                        Log.d(TAG_RESERVAS, "ambientes[0]=\n${body.first().toJson()}")
                        val reservasAmb = body.map { it.copy(tipo_reserva = "ambiente") }
                        reservasUnificadas.addAll(reservasAmb)
                    } else {
                        Log.w(TAG_RESERVAS, "ambientes vacío")
                    }

                    // Pinta lo que haya y continúa con equipos
                    listaReservasOriginal = reservasUnificadas.toList()
                    adapter.notifyDataSetChanged()
                    cargarReservasEquipos()
                }

                override fun onFailure(call: Call<List<ModeloReserva>>, t: Throwable) {
                    Log.e(TAG_RESERVAS, "❌ ambientes fail=${t.message}", t)
                    Toast.makeText(this@InstruMisReservas, "Fallo conexión (ambientes)", Toast.LENGTH_SHORT).show()

                    // Aún si falla, mantenemos la lista actual y pasamos a equipos
                    listaReservasOriginal = reservasUnificadas.toList()
                    adapter.notifyDataSetChanged()
                    cargarReservasEquipos()
                }
            })
    }

    private fun cargarReservasEquipos() {
        val paramsEq = buildParamsForRole()
        Log.d(TAG_RESERVAS, "➡️ obtenerReservasEquipo payload=$paramsEq")

        RetrofitClient.instance.obtenerReservasEquipo(paramsEq)
            .enqueue(object : Callback<List<ModeloReservaEquipo>> {
                override fun onResponse(
                    call: Call<List<ModeloReservaEquipo>>,
                    response: Response<List<ModeloReservaEquipo>>
                ) {
                    Log.d(TAG_RESERVAS, "⬅️ equipos code=${response.code()} ok=${response.isSuccessful}")
                    if (!response.isSuccessful) {
                        Log.e(TAG_RESERVAS, "equipos.errorBody=${response.errorBody()?.string()}")
                    }

                    val body = response.body().orEmpty()
                    Log.d(TAG_RESERVAS, "equipos.size=${body.size}")
                    if (body.isNotEmpty()) {
                        Log.d(TAG_RESERVAS, "equipos[0]=\n${body.first().toJson()}")
                        val reservasEq = body.map {
                            ModeloReserva(
                                id_reserva = it.id_reserva,
                                usuario_id = it.usuario_id,
                                ambiente_id = it.equipo_id, // mapeo para unificar
                                fecha_hora_inicio = it.fecha ?: "Sin fecha",
                                fecha_hora_fin = it.fecha_fin ?: "Sin fecha",
                                motivo = it.motivo,
                                jornadas = it.jornadas,
                                ambiente_nombre = it.equipo_nombre,
                                ambiente_imagen = it.equipo_imagen,
                                estado = it.estado,
                                tipo_reserva = "equipo"
                            )


                        }
                        // Ajusta si tienes una constante pública en RetrofitClient
                        fun apiBase(): String = "https://84fc134e56d5.ngrok-free.app/phpGestionReservas/"

                        // Normaliza: si viene null/relativa, arma absoluta. Si viene absoluta, la deja.
                        fun normalizeImageUrl(raw: String?, id: Int?): String? {
                            if (raw.isNullOrBlank()) {
                                return if (id != null) apiBase() + "imagenesEquipos/${id}.jpg" else null
                            }
                            return if (raw.startsWith("http", true)) raw
                            else apiBase() + raw.trimStart('/')
                        }
                        if (reservasEq.isNotEmpty()) {
                            Log.d(TAG_RESERVAS, "img(equipo)=${reservasEq.first().ambiente_imagen}")
                        }

                        reservasUnificadas.addAll(reservasEq)
                    } else {
                        Log.w(TAG_RESERVAS, "equipos vacío")
                    }

                    // Actualiza y pinta
                    listaReservasOriginal = reservasUnificadas.toList()
                    adapter.notifyDataSetChanged()

                    if (reservasUnificadas.isEmpty()) {
                        val msg = when (rolUsuario) {
                            "admin", "administrador" -> "No hay reservas registradas"
                            "instructor" -> "No tienes reservas registradas"
                            else -> "No tienes reservas registradas"
                        }
                        Toast.makeText(this@InstruMisReservas, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<ModeloReservaEquipo>>, t: Throwable) {
                    Log.e(TAG_RESERVAS, "❌ equipos fail=${t.message}", t)
                    Toast.makeText(this@InstruMisReservas, "Fallo conexión (equipos)", Toast.LENGTH_SHORT).show()

                    listaReservasOriginal = reservasUnificadas.toList()
                    adapter.notifyDataSetChanged()
                }
            })
    }

    private fun filtrarReservas(query: String) {
        val filtro = query.lowercase()
        val filtradas = listaReservasOriginal.filter {
            (it.ambiente_nombre?.lowercase()?.contains(filtro) == true) ||
                    (it.jornadas?.lowercase()?.contains(filtro) == true) ||
                    (it.estado?.lowercase()?.contains(filtro) == true) ||
                    (it.fecha_hora_inicio?.lowercase()?.contains(filtro) == true)
        }
        reservasUnificadas.clear()
        reservasUnificadas.addAll(filtradas)
        adapter.notifyDataSetChanged()
    }

    private fun eliminarReserva(reserva: ModeloReserva) {
        Log.d(TAG_RESERVAS, "🗑️ eliminarReserva() reserva=\n${reserva.toJson()}")

        val body = EliminarReservaRequest(
            reserva_id = (reserva.id_reserva ?: -1),
            tipo_reserva = reserva.tipo_reserva ?: "ambiente"
        )
        Log.d(TAG_RESERVAS, "➡️ eliminarReserva payload=\n${body.toJson()}")

        RetrofitClient.instance.eliminarReserva(body)
            .enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(
                    call: Call<Map<String, String>>,
                    response: Response<Map<String, String>>
                ) {
                    Log.d(TAG_RESERVAS, "⬅️ eliminarReserva code=${response.code()} ok=${response.isSuccessful}")
                    val raw = response.body()?.toString() ?: "null"
                    Log.d(TAG_RESERVAS, "body=$raw")

                    if (!response.isSuccessful) {
                        Log.e(TAG_RESERVAS, "errorBody=${response.errorBody()?.string()}")
                        Toast.makeText(this@InstruMisReservas, "Error al eliminar", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val mensaje = response.body()?.get("mensaje") ?: ""
                    val okEliminada = mensaje.contains("eliminada", ignoreCase = true)

                    if (okEliminada) {
                        Toast.makeText(this@InstruMisReservas, "Reserva eliminada", Toast.LENGTH_SHORT).show()
                        reservasUnificadas.remove(reserva)
                        listaReservasOriginal = listaReservasOriginal.filter { it.id_reserva != reserva.id_reserva }
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@InstruMisReservas, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    Log.e(TAG_RESERVAS, "❌ eliminarReserva fail=${t.message}", t)
                    Toast.makeText(this@InstruMisReservas, "Fallo al eliminar: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
