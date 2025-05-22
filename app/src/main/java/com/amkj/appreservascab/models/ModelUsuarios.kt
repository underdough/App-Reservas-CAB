package com.amkj.appreservascab.models

data class ModeloUsuarios(
    var id:Int,
    var numDocumento: String,
    var contrasena: String,
    var nombre: String,
    var correo:String,
    var rol: String,
    var bloque:String
)