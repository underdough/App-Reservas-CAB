package com.amkj.appreservascab.Modelos

data class RespuestaInsertarEquipo (
    val success: Boolean,
    val message: String?,
    val error: String? = null
)