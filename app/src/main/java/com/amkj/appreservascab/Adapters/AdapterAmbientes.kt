package com.amkj.appreservascab.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amkj.appreservascab.Modelos.ModeloAmbientes
import com.amkj.appreservascab.R

class AdapterAmbientes(
    var listaAmbientes: ArrayList<ModeloAmbientes>
): RecyclerView.Adapter<AdapterAmbientes.ViewHolder>() {

    class ViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        val tv_nombre_buscar = itemView.findViewById<TextView>(R.id.tv_nombre_buscar)

    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val vista= LayoutInflater.from(parent.context).inflate(R.layout.item_rv_ambientes, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: AdapterAmbientes.ViewHolder, position: Int) {
        val usuario= listaAmbientes[position]
        holder.tv_nombre_buscar.text=usuario.ambiente
    }

    override fun getItemCount(): Int {
        return listaAmbientes.size
    }

    fun filtrar(listaFiltrado:ArrayList<ModeloAmbientes>){
        this.listaAmbientes=listaFiltrado
        notifyDataSetChanged()
    }

}