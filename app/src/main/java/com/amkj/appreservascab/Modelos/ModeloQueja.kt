package com.amkj.appreservascab.Modelos

import com.google.gson.annotations.SerializedName

data class ModeloQueja(
    @SerializedName("id_queja")
    val id_queja: Int = 0,

    @SerializedName("usuario_id")
    val usuario_id: Int = 0,

    @SerializedName("rol")
    val rol: String = "",

    @SerializedName("asunto")
    val asunto: String = "",

    @SerializedName("descripcion")
    val descripcion: String = "",

    @SerializedName("estado")
    val estado: String = "",

    // Puede venir vacío; lo dejamos nullable
    @SerializedName("imagen_url")
    val imagen_url: String? = null,

    @SerializedName("fecha_creacion")
    val fecha_creacion: String = ""
)
