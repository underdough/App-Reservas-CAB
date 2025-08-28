package com.amkj.appreservascab.Modelos

data class ValidacionRequestAmbiente(
    val ambiente_id: Int,
    val fecha: String,   // "YYYY-MM-DD"
    val jornada: String
)
