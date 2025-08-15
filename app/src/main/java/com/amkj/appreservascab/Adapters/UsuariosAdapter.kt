package com.amkj.appreservascab.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amkj.appreservascab.Modelos.ModeloUsuarios
import com.amkj.appreservascab.R

class UsuariosAdapter(
    private val items: MutableList<ModeloUsuarios>,
    private val onEdit: (ModeloUsuarios) -> Unit
) : RecyclerView.Adapter<UsuariosAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNomUser)
        val tvCorreo: TextView = itemView.findViewById(R.id.tvInfoCorreo)
        val tvTelefono: TextView = itemView.findViewById(R.id.tvInfoTelefono)
        val tvRol: TextView = itemView.findViewById(R.id.tvInfoRol)
        val btnPerfil: ImageButton = itemView.findViewById(R.id.btnPerfil)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.activity_info_perfil, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val u = items[position]
        holder.tvNombre.text = u.nombre ?: "(Sin nombre)"
        holder.tvCorreo.text = u.correo ?: "(Sin correo)"
        holder.tvTelefono.text = "${u.telefono ?: "Sin teléfono"}"
        holder.tvRol.text = "${u.rol ?: "desconocido"}"
        holder.btnPerfil.setOnClickListener { onEdit(u) }
    }

    override fun getItemCount() = items.size

    fun updateItem(updated: ModeloUsuarios) {
        val idx = items.indexOfFirst { it.id == updated.id }
        if (idx != -1) {
            items[idx] = updated
            notifyItemChanged(idx)
        }
    }
}
