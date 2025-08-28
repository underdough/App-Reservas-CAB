package com.amkj.appreservascab.models

data class DisponibilidadResponse(
    val recurso: Recurso,
    val dias: List<DiaDisponibilidad>,
    val meta: Meta
)
data class Recurso(val tipo: String, val id: Int)
data class DiaDisponibilidad(
    val fecha: String,
    val ocupado_por: List<String>,
    val libre_en: List<String>,
    val estado_dia: String
)
data class Meta(
    val rango: Rango,
    val jornadas_definidas: List<String>,
    val sabados_permitidos: Boolean
)
data class Rango(val desde: String, val hasta: String)

