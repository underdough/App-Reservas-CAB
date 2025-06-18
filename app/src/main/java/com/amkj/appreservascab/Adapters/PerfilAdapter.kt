package com.amkj.appreservascab.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amkj.appreservascab.R
import com.amkj.appreservascab.Modelos.ModeloUsuarios  // Ajusta el import según la ubicación real de tu modelo

class PerfilAdapter(
    private val listaUsuarios: List<ModeloUsuarios>
) : RecyclerView.Adapter<PerfilAdapter.PerfilViewHolder>() {

    inner class PerfilViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNomUser: TextView = itemView.findViewById(R.id.tvNomUser)
//        val tvTelefono: TextView = itemView.findViewById(R.id.tvInfoTelefono)
        val tvCorreo: TextView = itemView.findViewById(R.id.tvInfoCorreo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PerfilViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_info_perfil, parent, false) // ✅ ESTE DEBE SER EL LAYOUT CORRECTO
        return PerfilViewHolder(view)

    }

    override fun onBindViewHolder(holder: PerfilViewHolder, position: Int) {
        val usuario = listaUsuarios[position]
        holder.tvNomUser.text = usuario.nombre
//        holder.tvTelefono.text = usuario.bloque ?: "Bloque no asignado"
        holder.tvCorreo.text = usuario.correo
    }

    override fun getItemCount(): Int = listaUsuarios.size
}
