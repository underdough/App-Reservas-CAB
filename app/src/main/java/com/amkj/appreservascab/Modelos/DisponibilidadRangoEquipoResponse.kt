package com.amkj.appreservascab.Modelos

// models/DisponibilidadRangoEquipoResponse.kt
data class DisponibilidadRangoEquipoResponse(
    val ok: Boolean,
    val mapa: Map<String, EstadosDiaEquipo> = emptyMap()
)

data class EstadosDiaEquipo(
    val M: String,  // "libre" | "pendiente" | "ocupado"
    val T: String,
    val N: String
)

