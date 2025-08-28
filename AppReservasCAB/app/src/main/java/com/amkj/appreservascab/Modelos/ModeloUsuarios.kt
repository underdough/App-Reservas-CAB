package com.amkj.appreservascab.Modelos

data class ModeloUsuarios(
    val id: Int = 0,
    val correo: String?,
    val contrasena: String?,
    val nombre: String,
    val rol: String,
    )
