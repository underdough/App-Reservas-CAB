package com.amkj.appreservascab.Modelos

data class Notificacion(
    val id: Int,
    val tipo_reserva: String,
    val reserva_id: Int,
    val usuario_id: Int,         // El admin que recibe la notificación
    val solicitante_id: Int,     // Quien hizo la reserva
    val accion: String,
    val titulo: String,
    val mensaje: String,
    val datos_adicionales: String?, // JSON u objeto, lo trataremos como String inicialmente
    val leida: Int,              // 0 o 1
    val fecha_creacion: String,  // Puede llegar como string desde PHP
    val fecha_leida: String?     // Puede ser null
)
