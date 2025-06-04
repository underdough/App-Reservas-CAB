package com.amkj.appreservascab.Usuarios

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object UsuariosPrefs {

    private lateinit var prefs: SharedPreferences
    private const val PREFS_USERS = "UsuariosPrefs"
    private const val KEY_USERS = "RegistrarUsuarios"

    val usuarios = mutableListOf(
        AdminRegistrarUsuarios("pepin", "123456"),
    )

    fun iniciar(context: Context) {
        prefs = context.getSharedPreferences(PREFS_USERS, Context.MODE_PRIVATE)
        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        val usuariosGuardados = prefs.getStringSet(KEY_USERS, null)
        if (usuariosGuardados != null) {
            usuarios.clear()
            for (usuario in usuariosGuardados) {
                val partes = usuario.split(":")
                if (partes.size == 2) {
                    usuarios.add(AdminRegistrarUsuarios(partes[0], partes[1]))
                }
            }
        } else {
            guardarUsuarios()
        }
    }


    fun guardarUsuarios() {
        prefs.edit {
            val usuariosSet = usuarios.map { "${it.correo}:${it.contrasena}" }.toSet()
            putStringSet(KEY_USERS, usuariosSet)
        }
    }

}

data class AdminRegistrarUsuarios(val correo: String, val contrasena: String)

