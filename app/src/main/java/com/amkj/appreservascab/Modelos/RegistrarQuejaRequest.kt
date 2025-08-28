package com.amkj.appreservascab.Modelos

data class RegistrarQuejaRequest(
    val usuario_id: Int,
    val rol: String,
    val asunto: String,
    val descripcion: String,
    val estado: String = "pendiente",
    val imagen_url: String? = null
)
