package com.amkj.appreservascab.Modelos

data class ModeloUsuarios(
    val id: String,
    val correo: String?,
    val contrasena: String?,
    val nombre: String,
    val rol: String,
    val bloque: String?
)

    object ModeloDatosUsuarios {
        val datos: List<ModeloUsuarios> = listOf(
            ModeloUsuarios(
                id = "1", correo = "laura.gomez@gmail.com", contrasena = "laura123", nombre = "Laura Gómez", "aprendiz", "a2"),
            ModeloUsuarios(id = "2", correo = "carlos.mendez@gmail.com", contrasena = "carlos456", nombre = "Carlos Méndez", rol = "instructor", "a3"),
            ModeloUsuarios(id = "3", correo = "admin@gmail.com", contrasena = "admin2024", nombre = "Administrador", rol = "administrador", "a4"),
            ModeloUsuarios(id = "3", correo = "maria.ortiz@gmail.com", contrasena = "maria789", nombre = "María Ortiz", rol = "aprendiz", "b2")
        )
    }

enum class roles{
    administrador,
    aprendiz,
    instructor
}