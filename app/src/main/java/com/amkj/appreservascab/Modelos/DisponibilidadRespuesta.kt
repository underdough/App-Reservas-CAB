package com.amkj.appreservascab.Modelos

data class DisponibilidadRespuesta(
    val ok: Boolean,
    val tipo: String,
    val recurso_id: Int,
    val inicio: String,
    val fin: String,
    val mapa: Map<String, DiaEstado>
)
