package com.amkj.appreservascab

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.amkj.appreservascab.Adapters.AdaptadorNotificaciones
import com.amkj.appreservascab.Modelos.Notificacion
import com.amkj.appreservascab.Modelos.TipoNotificacion
import com.amkj.appreservascab.Repository.NotificacionesRepository
import kotlinx.coroutines.launch

class ActividadNotificaciones : AppCompatActivity() {

    // Elementos de la interfaz
    private var btnRegresar: ImageButton? = null
    private var txtTituloNotificaciones: TextView? = null
    private var recyclerNotificaciones: RecyclerView? = null
    private var contenedorEstadoVacio: LinearLayout? = null
    private var swipeRefresh: SwipeRefreshLayout? = null
    private var contenedorCargando: LinearLayout? = null
    private var contenedorError: LinearLayout? = null
    private var btnReintentar: TextView? = null

    // Adaptador y repositorio
    private var adaptadorNotificaciones: AdaptadorNotificaciones? = null
    private val notificacionesRepository = NotificacionesRepository()

    // ID del usuario actual - obtenlo de SharedPreferences, Intent, etc.
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_notificaciones)

            // Obtener ID del usuario (ajusta según tu implementación)
            userId = obtenerIdUsuarioActual()

            inicializarVistas()
            configurarRecyclerView()
            configurarListeners()
            configurarSwipeRefresh()
            cargarNotificaciones()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al cargar la actividad: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun inicializarVistas() {
        try {
            btnRegresar = findViewById(R.id.btn_regresar)
            txtTituloNotificaciones = findViewById(R.id.txt_titulo_notificaciones)
            recyclerNotificaciones = findViewById(R.id.recycler_notificaciones)
            contenedorEstadoVacio = findViewById(R.id.contenedor_estado_vacio)
            swipeRefresh = findViewById(R.id.swipe_refresh)
            contenedorCargando = findViewById(R.id.contenedor_cargando)
            contenedorError = findViewById(R.id.contenedor_error)
            btnReintentar = findViewById(R.id.btn_reintentar)
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Error al inicializar vistas: ${e.message}")
        }
    }

    private fun configurarRecyclerView() {
        try {
            adaptadorNotificaciones = AdaptadorNotificaciones { notificacion ->
                alHacerClickNotificacion(notificacion)
            }

            recyclerNotificaciones?.apply {
                layoutManager = LinearLayoutManager(this@ActividadNotificaciones)
                adapter = adaptadorNotificaciones
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al configurar RecyclerView", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarListeners() {
        btnRegresar?.setOnClickListener {
            finish()
        }

        btnReintentar?.setOnClickListener {
            cargarNotificaciones()
        }
    }

    private fun configurarSwipeRefresh() {
        swipeRefresh?.setOnRefreshListener {
            cargarNotificaciones()
        }
    }

    private fun cargarNotificaciones() {
        if (userId == 0) {
            mostrarError("Usuario no identificado")
            return
        }

        mostrarCargando()

        lifecycleScope.launch {
            try {
                val resultado = notificacionesRepository.obtenerNotificaciones(userId)

                resultado.fold(
                    onSuccess = { notificaciones ->
                        mostrarNotificaciones(notificaciones)
                    },
                    onFailure = { error ->
                        mostrarError("Error al cargar notificaciones: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                mostrarError("Error inesperado: ${e.message}")
            } finally {
                swipeRefresh?.isRefreshing = false
            }
        }
    }

    private fun mostrarNotificaciones(notificaciones: List<Notificacion>) {
        try {
            ocultarTodosLosEstados()

            if (notificaciones.isEmpty()) {
                mostrarEstadoVacio()
            } else {
                recyclerNotificaciones?.visibility = View.VISIBLE
                adaptadorNotificaciones?.submitList(notificaciones)
            }

            actualizarTitulo(notificaciones)
        } catch (e: Exception) {
            e.printStackTrace()
            mostrarError("Error al mostrar notificaciones")
        }
    }

    private fun mostrarCargando() {
        ocultarTodosLosEstados()
        contenedorCargando?.visibility = View.VISIBLE
        txtTituloNotificaciones?.text = "Cargando notificaciones..."
    }

    private fun mostrarError(mensaje: String) {
        ocultarTodosLosEstados()
        contenedorError?.visibility = View.VISIBLE
        txtTituloNotificaciones?.text = "Error"
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    private fun mostrarEstadoVacio() {
        ocultarTodosLosEstados()
        contenedorEstadoVacio?.visibility = View.VISIBLE
        txtTituloNotificaciones?.text = "Notificaciones"
    }

    private fun ocultarTodosLosEstados() {
        recyclerNotificaciones?.visibility = View.GONE
        contenedorEstadoVacio?.visibility = View.GONE
        contenedorCargando?.visibility = View.GONE
        contenedorError?.visibility = View.GONE
    }

    private fun actualizarTitulo(notificaciones: List<Notificacion>) {
        try {
            val noLeidas = notificaciones.count { !it.esLeida }
            txtTituloNotificaciones?.text = if (noLeidas > 0) {
                "Notificaciones ($noLeidas)"
            } else {
                "Notificaciones"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            txtTituloNotificaciones?.text = "Notificaciones"
        }
    }

    private fun alHacerClickNotificacion(notificacion: Notificacion) {
        try {
            // Marcar como leída en el servidor si no lo está
            if (!notificacion.esLeida) {
                marcarComoLeida(notificacion)
            }

            // Manejar la acción según el tipo de notificación
            when (notificacion.tipoNotificacion) {
                TipoNotificacion.SOLICITUD_RESERVA -> {
                    abrirDetallesReserva(notificacion.idReserva)
                }
                TipoNotificacion.RESERVA_CONFIRMADA -> {
                    mostrarMensaje("Reserva confirmada por ${notificacion.nombreUsuario}")
                }
                TipoNotificacion.RESERVA_CANCELADA -> {
                    mostrarMensaje("Reserva cancelada por ${notificacion.nombreUsuario}")
                }
                TipoNotificacion.RESERVA_MODIFICADA -> {
                    abrirDetallesReserva(notificacion.idReserva)
                }
                TipoNotificacion.MENSAJE_SISTEMA -> {
                    mostrarMensaje(notificacion.descripcionCompleta ?: notificacion.mensaje)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mostrarMensaje("Error al procesar notificación")
        }
    }

    private fun marcarComoLeida(notificacion: Notificacion) {
        lifecycleScope.launch {
            try {
                val resultado = notificacionesRepository.marcarComoLeida(notificacion.id)

                resultado.fold(
                    onSuccess = {
                        // Actualizar localmente la notificación
                        adaptadorNotificaciones?.marcarComoLeida(notificacion)
                        // Actualizar el título con el nuevo conteo
                        adaptadorNotificaciones?.currentList?.let { actualizarTitulo(it) }
                    },
                    onFailure = { error ->
                        // Log del error, pero no mostrar al usuario ya que es secundario
                        println("Error al marcar como leída: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                println("Error al marcar como leída: ${e.message}")
            }
        }
    }

    private fun abrirDetallesReserva(idReserva: Int?) {
        if (idReserva != null) {
            // Implementar navegación a detalles de reserva
            mostrarMensaje("Abriendo detalles de reserva #$idReserva")
            // Intent hacia ActividadDetallesReserva, por ejemplo
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun obtenerIdUsuarioActual(): Int {
        // Implementa según tu sistema de autenticación
        // Ejemplos:

        // Desde SharedPreferences:
        // val prefs = getSharedPreferences("user", Context.MODE_PRIVATE)
        // return prefs.getInt("user_id", 0)

        // Desde Intent:
        // return intent.getIntExtra("user_id", 0)

        // Desde una sesión global:
        // return SessionManager.getCurrentUserId()

        // Por ahora, retorna un valor por defecto - CAMBIA ESTO
        return 1 // Temporal - reemplaza con tu lógica real
    }
}