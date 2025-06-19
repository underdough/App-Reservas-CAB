package com.amkj.appreservascab.Modelos

data class ModeloEquipos (
    val id: String? = null,
    val marca: String,
    val modelo: String,
    val descripcion: String,
    val imagen: String, // Puede ser una URL o base64
    val disponible: Boolean = true
)