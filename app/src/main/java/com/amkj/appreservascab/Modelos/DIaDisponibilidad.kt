package com.amkj.appreservascab.Modelos

import com.google.gson.annotations.SerializedName

data class DiaDisponibilidad(
    val fecha: String,
    @SerializedName("libre_en") val libre_en: List<String>,
    @SerializedName("ocupado_por") val ocupado_por: List<String> = emptyList(),
    @SerializedName("estado_dia") val estado_dia: String? = null
)

data class DisponibilidadResponse(
    val dias: List<DiaDisponibilidad>
)
