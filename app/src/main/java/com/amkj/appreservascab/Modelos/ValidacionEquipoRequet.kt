package com.amkj.appreservascab.Modelos

data class ValidacionEquipoRequest(
    val equipo_id: Int,
    val fecha: String,
    val jornadas: String
)
