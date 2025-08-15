package com.amkj.appreservascab.Usuarios

import android.content.Context
import android.content.SharedPreferences

object SesionUsuarioPrefs {

    // Usa el mismo archivo que el resto de la app
    private const val PREFS_NAME = "UsuariosPrefs"

    // Claves canónicas
    private const val KEY_ID = "id"
    private const val KEY_ID_ALT = "id_usuario"     // compat
    private const val KEY_CORREO = "correo"
    private const val KEY_CORREO_ALT = "email"      // compat
    private const val KEY_NOMBRE = "nombre"
    private const val KEY_ROL = "rol"
    private const val KEY_TEL = "telefono"
    private const val KEY_TEL_ALT = "celular"       // compat
    private const val KEY_DOC = "num_documento"     // si lo manejas

    private lateinit var prefs: SharedPreferences

    fun iniciar(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** Guarda TODO al iniciar sesión */
    fun guardarSesion(
        correo: String,
        nombre: String,
        documento: String,
        rol: String,
        id: Int,
        telefono: String
    ) {
        prefs.edit().apply {
            putInt(KEY_ID, id)
            putInt(KEY_ID_ALT, id) // compat
            putString(KEY_NOMBRE, nombre)
            putString(KEY_CORREO, correo)
            putString(KEY_CORREO_ALT, correo) // compat
            putString(KEY_TEL, telefono)
            putString(KEY_TEL_ALT, telefono)  // compat
            putString(KEY_ROL, rol)
            putString(KEY_DOC, documento)
        }.apply()
    }

    /** Llama esto tras editar perfil para mantener sesión sincronizada */
    fun updatePerfil(
        nombre: String? = null,
        correo: String? = null,
        telefono: String? = null,
        rol: String? = null
    ) {
        prefs.edit().apply {
            nombre?.let { putString(KEY_NOMBRE, it) }
            correo?.let {
                putString(KEY_CORREO, it)
                putString(KEY_CORREO_ALT, it) // compat
            }
            telefono?.let {
                putString(KEY_TEL, it)
                putString(KEY_TEL_ALT, it) // compat
            }
            rol?.let { putString(KEY_ROL, it) }
        }.apply()
    }

    // Getters con fallback entre claves canónicas y alias
    fun obtenerId(): Int = prefs.getInt(KEY_ID, prefs.getInt(KEY_ID_ALT, -1))
    fun obtenerCorreo(): String = prefs.getString(KEY_CORREO, prefs.getString(KEY_CORREO_ALT, "")) ?: ""
    fun obtenerNombre(): String = prefs.getString(KEY_NOMBRE, "") ?: ""
    fun obtenerRol(): String = prefs.getString(KEY_ROL, "") ?: ""
    fun obtenerTel(): String = prefs.getString(KEY_TEL, prefs.getString(KEY_TEL_ALT, "")) ?: ""
    fun obtenerDocumento(): String = prefs.getString(KEY_DOC, "") ?: ""
}
