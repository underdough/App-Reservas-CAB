package com.amkj.appreservascab.Modelos

data class ValidacionResponse(
    val ok: Boolean,
    val disponible: Boolean,
    val mensaje: String? = null
)
