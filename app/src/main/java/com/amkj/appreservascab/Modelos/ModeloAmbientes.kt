package com.amkj.appreservascab.Modelos


data class ModeloAmbientes(
    val id: String? = null,
    var nombre:String,
    var descripcion:String,
    var imagen: String,
    var disponible: Boolean = true
)