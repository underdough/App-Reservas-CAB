package com.amkj.appreservascab.Adaptadores

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amkj.appreservascab.databinding.ItemEquipoBinding
import com.amkj.appreservascab.Modelos.ModeloEquipos
import com.amkj.appreservascab.R
import com.amkj.appreservascab.SolicitudReservasEquipo
import com.bumptech.glide.Glide

class AdapterEquipos(
    private var listaEquipos: List<ModeloEquipos>,
    private val onItemClick: (ModeloEquipos) -> Unit
) : RecyclerView.Adapter<AdapterEquipos.EquipoViewHolder>() {

    inner class EquipoViewHolder(val binding: ItemEquipoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(equipo: ModeloEquipos) {
            binding.tvMarca.text = equipo.marca
            binding.tvModelo.text = equipo.modelo
            binding.tvDescripcion.text = equipo.descripcion

            val url = "https://cf7e2811433e.ngrok-free.app/phpGestionReservas/${equipo.imagen}"
            Glide.with(binding.root.context)
                .load(url)
                .placeholder(R.drawable.imagen_error)
                .error(R.drawable.imagen_error)
                .into(binding.ivEquipo)

            binding.root.setOnClickListener {
                onItemClick(equipo)
            }

            binding.ibirReservas.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, SolicitudReservasEquipo::class.java)
                intent.putExtra("equipo", equipo)
                context.startActivity(intent)
            }
        }
    }
    fun filtrar(query: String) {
        val filtrada = listaEquiposOriginal.filter {
            it.marca.contains(query, ignoreCase = true) || it.modelo.contains(query, ignoreCase = true)
        }
        listaEquipos = filtrada
        notifyDataSetChanged()
    }

    private var listaEquiposOriginal: List<ModeloEquipos> = listOf()

    fun setListaCompleta(lista: List<ModeloEquipos>) {
        listaEquiposOriginal = lista
        listaEquipos = lista
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipoViewHolder {
        val binding = ItemEquipoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EquipoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EquipoViewHolder, position: Int) {
        holder.bind(listaEquipos[position])
    }

    override fun getItemCount() = listaEquipos.size
}
