package com.amkj.appreservascab.Modelos

data class ModeloReserva(
    val usuario_id: Int,
    val ambiente_id: Int,
    val fecha_hora_inicio: String,
    val fecha_hora_fin: String,
    val motivo: String = "Reserva de ambiente",
    val jornadas: String,
    val id: Int = 0,
    val ambiente_nombre: String? = null,
    val ambiente_imagen: String? = null,
    val estado: String? = null

)
