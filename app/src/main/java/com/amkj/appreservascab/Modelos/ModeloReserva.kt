package com.amkj.appreservascab.Modelos

data class ModeloReserva(
    val ambiente_id: Int,
    val ambiente_nombre: String,
    val usuario_id: Int,
    val fecha_hora_inicio: String,
    val fecha_hora_fin: String,
    val jornadas: String,
    val motivo: String? = null,           // ✅ NUEVO
    val estado: String = "pendiente"
)

