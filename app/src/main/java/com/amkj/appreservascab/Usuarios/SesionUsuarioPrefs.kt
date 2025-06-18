package com.amkj.appreservascab.Usuarios

import android.content.Context
import android.content.SharedPreferences

object SesionUsuarioPrefs {

    private const val PREFS_NAME = "SesionUsuario"
    private const val KEY_CORREO = "correo"
    private const val KEY_NOMBRE = "nombre"
    private const val KEY_DOCUMENTO = "num_documento"
    private const val KEY_ROL = "rol"
    private const val KEY_ID= "id"
    private const val KEY_BLOQUE= "bloque"

    private lateinit var prefs: SharedPreferences

    fun iniciar(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun guardarSesion(correo: String, nombre: String, documento: String, rol: String, id:Int) {
        prefs.edit().apply {
            putString(KEY_CORREO, correo)
            putString(KEY_NOMBRE, nombre)
            putString(KEY_DOCUMENTO, documento)
            putString(KEY_ROL, rol)
            putString(KEY_ID, id.toString())
            apply()
        }
    }

    fun obtenerCorreo(): String = prefs.getString(KEY_CORREO, "") ?: ""
    fun obtenerNombre(): String = prefs.getString(KEY_NOMBRE, "") ?: ""
    fun obtenerDocumento(): String = prefs.getString(KEY_DOCUMENTO, "") ?: ""
    fun obtenerRol(): String = prefs.getString(KEY_ROL, "") ?: ""
    fun obtenerId():String=prefs.getString(KEY_ID, "") ?:""
    fun obtenerBloque():String=prefs.getString(KEY_ID, "") ?:""
}
