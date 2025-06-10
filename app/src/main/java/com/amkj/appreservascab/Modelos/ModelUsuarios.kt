package com.amkj.appreservascab.Modelos

data class ModeloUsuarios(
    var id:Int,
    var correo:String,
    var contrasena: String,
    var nombre: String,
    var rol: String,
    var bloque:String
)

object ModeloDatosUsuarios {
    val datos: List<ModeloUsuarios> = listOf(
        ModeloUsuarios(id = 1, correo = "laura.gomez@gmail.com", contrasena = "laura123", nombre = "Laura Gómez", rol = "aprendiz", bloque = "B1"),
        ModeloUsuarios(id = 2, correo = "carlos.mendez@gmail.com", contrasena = "carlos456", nombre = "Carlos Méndez", rol = "instructor", bloque = "A2"),
        ModeloUsuarios(id = 3, correo = "admin@gmail.com", contrasena = "admin2024", nombre = "Administrador", rol = "administrador", bloque = "C1"),
        ModeloUsuarios(id = 4, correo = "maria.ortiz@gmail.com", contrasena = "maria789", nombre = "María Ortiz", rol = "aprendiz", bloque = "B3")
    )
}
