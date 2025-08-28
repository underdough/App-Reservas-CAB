package com.amkj.appreservascab.Modelos

import com.google.gson.annotations.SerializedName

data class ReservaDetalle(
    val id: Int,
    val usuario: String?,             // puede venir null si no hay join
    val inicio: String,               // "YYYY-MM-DD HH:MM:SS"
    val fin: String,                  // "
    val estado: String,               // "aprobada"/"pendiente"
    @SerializedName("jornadas_estimada") val jornadasEstimada: List<String> = emptyList(),
    @SerializedName("jornadas_texto")    val jornadasTexto: String? = null

)
