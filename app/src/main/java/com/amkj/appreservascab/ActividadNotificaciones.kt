package com.amkj.appreservascab

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amkj.appreservascab.Adapters.NotificacionesAdapter
import com.amkj.appreservascab.databinding.ActivityNotificacionesBinding
import com.amkj.appreservascab.Modelos.Notificacion
import com.amkj.appreservascab.servicios.RetrofitClient
import kotlinx.coroutines.launch

class ActividadNotificaciones : AppCompatActivity() {

    private lateinit var binding: ActivityNotificacionesBinding
    private lateinit var adapter: NotificacionesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerNotificaciones.layoutManager = LinearLayoutManager(this)

        // Botón regresar
        binding.btnRegresar.setOnClickListener {
            finish()
        }

        cargarNotificaciones()

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            cargarNotificaciones()
        }
    }

    private fun cargarNotificaciones() {
        val sharedPref = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
        val usuarioId = sharedPref.getInt("id", -1)
        Log.d("UsuarioPrefs", "ID del usuario guardado: $usuarioId")

        if (usuarioId == -1) {
            Toast.makeText(this, "No se pudo obtener el usuario", Toast.LENGTH_SHORT).show()
            return
        }

        mostrarCargando()

        lifecycleScope.launch {
            try {
                val respuesta = RetrofitClient.instance.obtenerNotificaciones(
                    mapOf("usuario_id" to usuarioId)
                )

                Log.d("NotificacionesDebug", "Cantidad de notificaciones recibidas: ${respuesta.size}")
                respuesta.forEach {
                    Log.d("NotificacionesDebug", "Notificación -> ID: ${it.id}, Mensaje: ${it.mensaje}")
                }

                if (respuesta.isNotEmpty()) {
                    adapter = NotificacionesAdapter(respuesta) { notificacion ->
                        mostrarDialogoAcciones(notificacion)
                    }
                    binding.recyclerNotificaciones.adapter = adapter
                    mostrarContenido()
                } else {
                    mostrarVacio()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("NotificacionesDebug", "Error cargando notificaciones", e)
                mostrarError()
            }
        }
    }

    private fun mostrarDialogoAcciones(notificacion: Notificacion) {
        val sharedPref = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
        val rolUsuario = sharedPref.getString("rol", "") ?: ""

        val adminId = sharedPref.getInt("id", -1)

        val opciones = if (rolUsuario == "admin") {
            arrayOf("Marcar como leída", "Aceptar reserva", "Rechazar reserva")
        } else {
            arrayOf("Marcar como leída")
        }

        AlertDialog.Builder(this)
            .setTitle("Acciones para la notificación")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> marcarComoLeida(notificacion.id)
                    1 -> if (rolUsuario == "admin") {
                        actualizarEstadoReserva(
                            notificacion.tipo_reserva,
                            notificacion.reserva_id,
                            "aprobada",
                            adminId
                        )
                    }
                    2 -> if (rolUsuario == "admin") {
                        actualizarEstadoReserva(
                            notificacion.tipo_reserva,
                            notificacion.reserva_id,
                            "rechazada",
                            adminId
                        )
                    }
                }
            }
            .show()
    }




    private fun marcarComoLeida(idNotificacion: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.marcarNotificacionLeida(mapOf("id" to idNotificacion))
                if (response.isSuccessful) {
                    Log.d("Notif", "Marcada como leída")
                    cargarNotificaciones()
                } else {
                    Toast.makeText(this@ActividadNotificaciones, "No se pudo marcar como leída", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ActividadNotificaciones, "Error al marcar como leída", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarEstadoReserva(tipo: String, reservaId: Int, nuevoEstado: String, adminId: Int) {
        lifecycleScope.launch {
            try {
                Log.d("ActualizarEstado", "Enviando datos: tipo=$tipo, reservaId=$reservaId, nuevoEstado=$nuevoEstado, aprobado_por=$adminId")

                val respuesta = RetrofitClient.instance.actualizarEstadoReserva(
                    mapOf(
                        "tipo_reserva" to tipo,
                        "reserva_id" to reservaId,
                        "nuevo_estado" to nuevoEstado,
                        "aprobado_por" to adminId
                    )
                )

                Log.d("ActualizarEstado", "Respuesta recibida: $respuesta")

                Toast.makeText(
                    this@ActividadNotificaciones,
                    respuesta["mensaje"] ?: "Estado actualizado",
                    Toast.LENGTH_SHORT
                ).show()

                cargarNotificaciones()

            } catch (e: Exception) {
                Log.e("ActualizarEstado", "Error actualizando estado", e)
                Toast.makeText(this@ActividadNotificaciones, "Fallo al actualizar estado", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun mostrarCargando() {
        binding.contenedorCargando.visibility = View.VISIBLE
        binding.contenedorEstadoVacio.visibility = View.GONE
        binding.contenedorError.visibility = View.GONE
        binding.recyclerNotificaciones.visibility = View.GONE
    }

    private fun mostrarContenido() {
        binding.contenedorCargando.visibility = View.GONE
        binding.contenedorEstadoVacio.visibility = View.GONE
        binding.contenedorError.visibility = View.GONE
        binding.recyclerNotificaciones.visibility = View.VISIBLE
    }

    private fun mostrarVacio() {
        binding.contenedorCargando.visibility = View.GONE
        binding.contenedorEstadoVacio.visibility = View.VISIBLE
        binding.contenedorError.visibility = View.GONE
        binding.recyclerNotificaciones.visibility = View.GONE

        Toast.makeText(this, "No hay notificaciones nuevas", Toast.LENGTH_SHORT).show()
    }

    private fun mostrarError() {
        binding.contenedorCargando.visibility = View.GONE
        binding.contenedorEstadoVacio.visibility = View.GONE
        binding.contenedorError.visibility = View.VISIBLE
        binding.recyclerNotificaciones.visibility = View.GONE

        binding.btnReintentar.setOnClickListener {
            cargarNotificaciones()
        }
    }
}
