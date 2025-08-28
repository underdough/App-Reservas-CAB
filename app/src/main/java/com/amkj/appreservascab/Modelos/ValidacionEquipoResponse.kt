package com.amkj.appreservascab.Modelos

import com.google.gson.annotations.SerializedName

data class ValidacionEquipoResponse(
    val ok: Boolean? = null,
    val disponible: Boolean? = null,
    val error: String? = null,
    @SerializedName("jornadas_consultadas") val jornadasConsultadas: List<String>? = null,
    val detalle: Map<String, String>? = null // p.ej. {"Mañana":"libre","Noche":"ocupado"}
)
