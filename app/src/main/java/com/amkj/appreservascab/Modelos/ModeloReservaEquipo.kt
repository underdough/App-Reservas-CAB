package com.amkj.appreservascab.Modelos

import com.google.gson.annotations.SerializedName

data class ModeloReservaEquipo(
    @SerializedName("id") val id_reserva: Int? = null, // ← CORREGIDO
    val equipo_id: Int = 0, // ← puedes dejar 0 si lo asignas manualmente
    val usuario_id: Int,
    val elemento_id: Int,
    @SerializedName("fecha_hora_inicio") val fecha: String,
    val motivo: String,
    val jornadas: String,
    val estado: String,
    val equipo_nombre: String?,
    val equipo_imagen: String?
)



//data class ModeloReservaEquipo(
//    val equipo_id: Int,               // ID del equipo en la app (no se guarda en DB)
//    val usuario_id: Int,              // ID del usuario que reservó
//    val elemento_id: Int,             // ID real del equipo en la DB
//    val fecha: String,                // Fecha de la reserva
//    val motivo: String,               // Motivo de la reserva
//    val jornadas: String,             // Jornadas seleccionadas
//    val estado: String,               // Estado de la reserva: pendiente/aprobada/rechazada
//    val equipo_nombre: String?,       // Nombre del equipo (puede ser null si no se obtiene)
//    val equipo_imagen: String?        // URL o nombre del archivo imagen (también puede ser null)
//)
