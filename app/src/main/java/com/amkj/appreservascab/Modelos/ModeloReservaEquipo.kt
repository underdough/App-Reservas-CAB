package com.amkj.appreservascab.Modelos



data class ModeloReservaEquipo(
    val equipo_id: Int,               // ID del equipo en la app (no se guarda en DB)
    val usuario_id: Int,              // ID del usuario que reservó
    val elemento_id: Int,             // ID real del equipo en la DB
    val fecha: String,                // Fecha de la reserva
    val motivo: String,               // Motivo de la reserva
    val jornadas: String,             // Jornadas seleccionadas
    val estado: String,               // Estado de la reserva: pendiente/aprobada/rechazada
    val equipo_nombre: String?,       // Nombre del equipo (puede ser null si no se obtiene)
    val equipo_imagen: String?        // URL o nombre del archivo imagen (también puede ser null)
)
