package com.amkj.appreservascab.Modelos

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize

data class ModeloUsuarios(
    val id: Int = 0,
    val correo: String?,
    val contrasena: String? = null,
    val nombre: String,
    val rol: String,
    val telefono: String? = null
    ) : Parcelable
