package com.amkj.appreservascab.Modelos

data class SolicitudDisponibilidadRequest(
    val ambiente_id: Int,
    val fecha: String,
    val jornadas: String
)
