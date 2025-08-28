package com.amkj.appreservascab.Modelos



/**
 * Data class que representa una notificación del sistema de reservas
 */
data class  Notificacion(
    val id: Int,
    val idUsuario: Int,
    val nombreUsuario: String,
    val avatarUsuario: String?,
    val tipoNotificacion: TipoNotificacion,
    val mensaje: String,
    val descripcionCompleta: String?,
    val fechaCreacion: Long,
    val esLeida: Boolean = false,
    val idReserva: Int? = null,
    val datosAdicionales: String? = null // JSON con información extra si es necesario
)

/**
 * Enum que define los diferentes tipos de notificaciones
 */
enum class TipoNotificacion(val icono: Int, val descripcion: String) {
    SOLICITUD_RESERVA(
        icono = android.R.drawable.ic_dialog_email, // Temporal, toca remplazari conos
        descripcion = "solicitó un ambiente"
    ),
    RESERVA_CONFIRMADA(
        icono = android.R.drawable.ic_dialog_info, // Temporal -
        descripcion = "confirmó la reserva"
    ),
    RESERVA_CANCELADA(
        icono = android.R.drawable.ic_dialog_alert, // Temporal
        descripcion = "canceló la reserva de un ambiente"
    ),
    RESERVA_MODIFICADA(
        icono = android.R.drawable.ic_menu_edit, // Temporal - reemplaza con tu icono
        descripcion = "modificó una reserva"
    ),
    MENSAJE_SISTEMA(
        icono = android.R.drawable.ic_dialog_info, // Temporal
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