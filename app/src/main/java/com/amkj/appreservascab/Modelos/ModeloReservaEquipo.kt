package com.amkj.appreservascab.Modelos

data class ModeloReservaEquipo(
    val marca: String,
    val modelo: String,
    val nombre: String,
    val fecha: String,
    val jornadas: List<String>
)
