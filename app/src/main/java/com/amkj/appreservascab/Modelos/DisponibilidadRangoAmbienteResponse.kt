package com.amkj.appreservascab.Modelos

data class DisponibilidadRangoAmbienteResponse(
    val ok: Boolean,
    val mapa: Map<String, EstadoDiaCod> = emptyMap()
)

data class EstadoDiaCod(
    val M: String, // "libre" | "pendiente" | "ocupado"
    val T: String,
    val N: String
)
