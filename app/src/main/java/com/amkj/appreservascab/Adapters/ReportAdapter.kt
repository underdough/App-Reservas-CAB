package com.amkj.appreservascab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amkj.appreservascab.Modelos.ReportItem

class ReportAdapter(private val tipoFijo: String? = null)
    : RecyclerView.Adapter<ReportAdapter.VH>() {

    private val items = mutableListOf<ReportItem>()

    fun submit(data: List<ReportItem>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    inner class VH(v: View): RecyclerView.ViewHolder(v) {
        val tvPos: TextView = v.findViewById(R.id.tvPos)
        val tvNombre: TextView = v.findViewById(R.id.tvNombre)
        val tvTipo: TextView = v.findViewById(R.id.tvTipo)
        val tvTotal: TextView = v.findViewById(R.id.tvTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reporte, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvPos.text = "#${position + 1}"
        holder.tvNombre.text = item.nombre_recurso

        // Si se definió un tipo fijo en el fragment, úsalo; de lo contrario, usa el del backend
        holder.tvTipo.text = tipoFijo ?: when (item.tipo.lowercase()) {
            "ambiente" -> "Ambiente"
            "equipo" -> "Equipo"
            else -> item.tipo
        }

        holder.tvTotal.text = item.total.toString()
    }
}
