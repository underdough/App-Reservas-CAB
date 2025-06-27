package com.amkj.appreservascab.Modelos

// Ajusta el package según tu proyecto

/**
 * Data class que representa una notificación del sistema de reservas
 */
data class Notificacion(
    val id: Int,
    val idUsuario: Int,
    val nombreUsuario: String,
    val avatarUsuario: String?, // URL del avatar, puede ser null
    val tipoNotificacion: TipoNotificacion,
    val mensaje: String,
    val descripcionCompleta: String?, // Descripción adicional si es necesaria
    val fechaCreacion: Long, // Timestamp en milisegundos
    val esLeida: Boolean = false,
    val idReserva: Int? = null, // ID de la reserva relacionada (si aplica)
    val datosAdicionales: String? = null // JSON con información extra si es necesario
)

/**
 * Enum que define los diferentes tipos de notificaciones
 */
enum class TipoNotificacion(val icono: Int, val descripcion: String) {
    SOLICITUD_RESERVA(
        icono = android.R.drawable.ic_dialog_email, // Temporal - reemplaza con tu icono
        descripcion = "solicitó un ambiente"
    ),
    RESERVA_CONFIRMADA(
        icono = android.R.drawable.ic_dialog_info, // Temporal - reemplaza con tu icono
        descripcion = "confirmó la reserva"
    ),
    RESERVA_CANCELADA(
        icono = android.R.drawable.ic_dialog_alert, // Temporal - reemplaza con tu icono
        descripcion = "canceló la reserva de un ambiente"
    ),
    RESERVA_MODIFICADA(
        icono = android.R.drawable.ic_menu_edit, // Temporal - reemplaza con tu icono
        descripcion = "modificó una reserva"
    ),
    MENSAJE_SISTEMA(
        icono = android.R.drawable.ic_dialog_info, // Temporal - reemplaza con tu icono
        descripcion = "mensaje del sistema"
    )
}

/**
 * Extension functions para facilitar el manejo de tiempo
 */
fun Notificacion.obtenerTiempoTranscurrido(): String {
    val ahora = System.currentTimeMillis()
    val diferencia = ahora - this.fechaCreacion

    return when {
        diferencia < 60 * 1000 -> "ahora"
        diferencia < 60 * 60 * 1000 -> {
            val minutos = (diferencia / (60 * 1000)).toInt()
            "hace ${minutos}m"
        }
        diferencia < 24 * 60 * 60 * 1000 -> {
            val horas = (diferencia / (60 * 60 * 1000)).toInt()
            "hace ${horas}h"
        }
        diferencia < 7 * 24 * 60 * 60 * 1000 -> {
            val dias = (diferencia / (24 * 60 * 60 * 1000)).toInt()
            "hace ${dias}d"
        }
        else -> {
            val semanas = (diferencia / (7 * 24 * 60 * 60 * 1000)).toInt()
            "hace ${semanas}sem"
        }
    }
}

/**
 * Extension function para formatear el mensaje completo
 */
fun Notificacion.obtenerMensajeFormateado(): String {
    return "$nombreUsuario ${tipoNotificacion.descripcion}"
}