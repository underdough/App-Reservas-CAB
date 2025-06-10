package com.amkj.appreservascab.Modelos

data class ModeloUsuarios(
    var id:Int,
    var correo:String,
    var contrasena: String,
    var nombre: String,
    var rol: String,
    var bloque:String
)

object ModeloDatosUsuarios{
    val datos: List<ModeloUsuarios> = listOf(

    )
}