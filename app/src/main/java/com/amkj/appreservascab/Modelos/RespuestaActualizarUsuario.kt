package com.amkj.appreservascab.Modelos

data class RespuestaActualizarUsuario(
    val ok: Boolean,
    val mensaje: String? = null,
    val usuario: ModeloUsuarios? = null
)
