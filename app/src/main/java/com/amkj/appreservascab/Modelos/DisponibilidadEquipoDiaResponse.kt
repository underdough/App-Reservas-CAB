package com.amkj.appreservascab.Modelos

import com.amkj.appreservascab.models.DiaDisponibilidad

data class DisponibilidadEquipoDiaResponse(
    val ok: Boolean,
    val dias: List<DiaDisponibilidad> = emptyList(),
    val reservas: List<ReservaDetalle>? = null
)
