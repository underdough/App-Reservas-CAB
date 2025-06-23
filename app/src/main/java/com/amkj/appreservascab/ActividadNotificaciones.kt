package com.amkj.appreservascab

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amkj.appreservascab.Adapters.AdaptadorNotificaciones
import com.amkj.appreservascab.Modelos.Notificacion
import com.amkj.appreservascab.Modelos.TipoNotificacion

class ActividadNotificaciones : AppCompatActivity() {

    // Elementos de la interfaz
    private var btnRegresar: ImageButton? = null
    private var txtTituloNotificaciones: TextView? = null
    private var recyclerNotificaciones: RecyclerView? = null
    private var contenedorEstadoVacio: LinearLayout? = null

    // Adaptador
    private var adaptadorNotificaciones: AdaptadorNotificaciones? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_notificaciones)

            inicializarVistas()
            configurarRecyclerView()
            configurarListeners()
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
            // Si el adaptador no existe, usar datos simples
            Toast.makeText(this, "Adaptador no disponible, usando vista simple", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarListeners() {
        btnRegresar?.setOnClickListener {
            finish()
        }
    }

    private fun cargarNotificaciones() {
        try {
            // Crear datos de prueba
            val notificacionesPrueba = crearNotificacionesPrueba()
            mostrarNotificaciones(notificacionesPrueba)
        } catch (e: Exception) {
            e.printStackTrace()
            // Mostrar estado vacío si hay error
            mostrarEstadoVacio()
        }
    }

    private fun mostrarNotificaciones(notificaciones: List<Notificacion>) {
        try {
            if (notificaciones.isEmpty()) {
                mostrarEstadoVacio()
            } else {
                // Mostrar lista de notificaciones
                recyclerNotificaciones?.visibility = View.VISIBLE
                contenedorEstadoVacio?.visibility = View.GONE
                adaptadorNotificaciones?.submitList(notificaciones)
            }

            // Actualizar título con número de notificaciones no leídas
            actualizarTitulo(notificaciones)
        } catch (e: Exception) {
            e.printStackTrace()
            mostrarEstadoVacio()
        }
    }

    private fun mostrarEstadoVacio() {
        recyclerNotificaciones?.visibility = View.GONE
        contenedorEstadoVacio?.visibility = View.VISIBLE
        txtTituloNotificaciones?.text = "Notificaciones"
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
            // Marcar como leída si no lo está
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
        try {
            adaptadorNotificaciones?.marcarComoLeida(notificacion)
            adaptadorNotificaciones?.currentList?.let { actualizarTitulo(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun abrirDetallesReserva(idReserva: Int?) {
        if (idReserva != null) {
            mostrarMensaje("Abriendo detalles de reserva #$idReserva")
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    // Función temporal para crear datos de prueba
    private fun crearNotificacionesPrueba(): List<Notificacion> {
        return try {
            val ahora = System.currentTimeMillis()

            listOf(
                Notificacion(
                    id = 1,
                    idUsuario = 101,
                    nombreUsuario = "Kevin chen",
                    avatarUsuario = null,
                    tipoNotificacion = TipoNotificacion.SOLICITUD_RESERVA,
                    mensaje = "solicitó un ambiente",
                    descripcionCompleta = null,
                    fechaCreacion = ahora - (4 * 60 * 60 * 1000),
                    esLeida = false,
                    idReserva = 1001
                ),
                Notificacion(
                    id = 2,
                    idUsuario = 102,
                    nombreUsuario = "María",
                    avatarUsuario = null,
                    tipoNotificacion = TipoNotificacion.RESERVA_CANCELADA,
                    mensaje = "canceló la reserva de un ambiente",
                    descripcionCompleta = null,
                    fechaCreacion = ahora - (4 * 60 * 60 * 1000),
                    esLeida = false,
                    idReserva = 1002
                ),
                Notificacion(
                    id = 3,
                    idUsuario = 103,
                    nombreUsuario = "Felipe",
                    avatarUsuario = null,
                    tipoNotificacion = TipoNotificacion.RESERVA_CONFIRMADA,
                    mensaje = "reservó el ambiente e4",
                    descripcionCompleta = null,
                    fechaCreacion = ahora - (4 * 60 * 60 * 1000),
                    esLeida = true,
                    idReserva = 1003
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}