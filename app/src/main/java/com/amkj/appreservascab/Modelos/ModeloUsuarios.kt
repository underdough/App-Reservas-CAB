package com.amkj.appreservascab.Modelos

    data class ModeloUsuarios(
        var id: String,
        var correo: String,
        var contrasena: String,
        var nombre: String,
        var rol: String,
    )

    object ModeloDatosUsuarios {
        val datos: List<ModeloUsuarios> = listOf(
            ModeloUsuarios(
                id = "1", correo = "laura.gomez@gmail.com", contrasena = "laura123", nombre = "Laura Gómez", "aprendiz"),
            ModeloUsuarios(id = "2", correo = "carlos.mendez@gmail.com", contrasena = "carlos456", nombre = "Carlos Méndez", rol = "instructor"),
            ModeloUsuarios(id = "3", correo = "admin@gmail.com", contrasena = "admin2024", nombre = "Administrador", rol = "administrador"),
            ModeloUsuarios(id = "3", correo = "maria.ortiz@gmail.com", contrasena = "maria789", nombre = "María Ortiz", rol = "aprendiz")
        )
    }

enum class roles{
    administrador,
    aprendiz,
    instructor
}