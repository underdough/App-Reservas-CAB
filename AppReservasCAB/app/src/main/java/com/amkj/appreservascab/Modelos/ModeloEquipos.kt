package com.amkj.appreservascab.Modelos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModeloEquipos(
    val id: Int,
    val marca: String,
    val modelo: String,
    val descripcion: String,
    val imagen: String
) : Parcelable

