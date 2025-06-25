package com.amkj.appreservascab.Modelos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModeloAmbientes(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val imagen: String
) : Parcelable
