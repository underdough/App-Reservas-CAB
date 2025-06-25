package com.amkj.appreservascab.Modelos

data class RespuestaInsertarAmbiente(
    val success: Boolean,
    val message: String?,
    val error: String? = null
)
