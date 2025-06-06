package com.amkj.appreservascab.Admin

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object AdminRegistrarUsuario {
    private lateinit var prefs: SharedPreferences
    private const val PREFS_USERS = "UsuariosPrefs"
    private const val KEY_USERS = "RegistrarUsuarios"

    val usuariosad = mutableListOf(
        AdminRegistrarUsuarios("admin01", "123456"),
    )

    fun iniciar(context: Context) {
        prefs = context.getSharedPreferences(PREFS_USERS, Context.MODE_PRIVATE)
        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        val usuariosGuardados = prefs.getStringSet(KEY_USERS, null)
        if (usuariosGuardados != null) {
            usuariosad.clear()
            for (usuario in usuariosGuardados) {
                val partes = usuario.split(":")
                if (partes.size == 2) {
                    usuariosad.add(AdminRegistrarUsuarios(partes[0], partes[1]))
                }
            }
        } else {
            guardarUsuarios()
        }
    }



//    fun agregarUsuario(nombre: String, contrasena: String) {
//        usuariosad.add(AdminRegistrarUsuarios(nombre, contrasena))
//        guardarUsuarios()
//    }

    fun guardarUsuarios() {
        prefs.edit {
            val usuariosSet = usuariosad.map { "${it.correo}:${it.contrasena}" }.toSet()
            putStringSet(KEY_USERS, usuariosSet)
        }
    }

//    fun eliminarUsuarios{
//        val pos = adapterPosition
//        if (pos != RecyclerView.NO_POSITION) {
//            // al presionar el botón eliminar aparece un alert dialog para confirmar la eliminación del post
//            AlertDialog.Builder(itemView.context)
//                .setTitle("Confirmar eliminación")
//                .setMessage("¿Estás seguro de que deseas eliminar este post?")
//                .setPositiveButton("Eliminar") { dialog, _ ->
//                    // notifica y borra dentro del sistema, eliminando las posición en la que se encuentra el post
//                    post.removeAt(pos)
//                    notifyItemRemoved(pos)
//                    notifyItemRangeChanged(pos, post.size)
//                    Toast.makeText(itemView.context, "Usuario eliminado correctamente", Toast.LENGTH_SHORT).show()
//                }
//                .setNegativeButton("Cancelar", null)
//                .show()
//    }
}

data class AdminRegistrarUsuarios(val correo: String, val contrasena: String)
