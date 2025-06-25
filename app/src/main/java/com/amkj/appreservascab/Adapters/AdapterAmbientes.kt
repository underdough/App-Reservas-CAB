package com.amkj.appreservascab.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.amkj.appreservascab.Modelos.ModeloAmbientes
import com.amkj.appreservascab.R
import com.amkj.appreservascab.databinding.ItemAmbienteBinding
import com.amkj.appreservascab.databinding.ItemEquipoBinding
import com.bumptech.glide.Glide

class AdapterAmbientes(
    private var listaAmbientes: List<ModeloAmbientes>
): RecyclerView.Adapter<AdapterAmbientes.AmbienteViewHolder>() {

    inner class AmbienteViewHolder(val binding: ItemAmbienteBinding) :
        ViewHolder(binding.root)
//    val tv_nombre_buscar = itemView.findViewById<TextView>(R.id.tv_nombre_buscar)

//    class ViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
//        val tv_nombre_buscar = itemView.findViewById<TextView>(R.id.tv_nombre_buscar)
//
//    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AmbienteViewHolder {
//        val vista= LayoutInflater.from(parent.context).inflate(R.layout.item_rv_ambientes, parent, false)
    val binding = ItemAmbienteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return AmbienteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdapterAmbientes.AmbienteViewHolder, position: Int) {
//        val usuario= listaAmbientes[position]
//        holder.tv_nombre_buscar.text=usuario.nombre
        val ambiente = listaAmbientes[position]
        holder.binding.tvNombreAmbiente.text = ambiente.nombre
        holder.binding.tvDescripcion.text = ambiente.descripcion
        // URL completa (si solo guardas el nombre o ruta en la BD)
        val urlImagen = "http://192.168.1.23/phpGestionReservas/" + ambiente.imagen

        Glide.with(holder.itemView.context)
            .load(urlImagen)
            .placeholder(R.drawable.imagen_error) // mientras carga
            .error(R.drawable.imagen_error)       // si falla
            .into(holder.binding.ivAmbiente)
    }

    override fun getItemCount(): Int {
        return listaAmbientes.size
    }

    fun filtrar(listaFiltrado:ArrayList<ModeloAmbientes>){
        this.listaAmbientes=listaFiltrado
        notifyDataSetChanged()
    }

}