package com.amkj.appreservascab.Modelos

data class ReservaEquipoRequest(
    val elemento_id: Int,
    val usuario_id: Int,
    val fecha: String,
    val jornadas: String,
    val motivo: String,
    val estado: String = "pendiente"
)
