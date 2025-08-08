package com.amkj.appreservascab.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amkj.appreservascab.Modelos.ModeloAmbientes
import com.amkj.appreservascab.Modelos.ModeloEquipos
import com.amkj.appreservascab.databinding.ItemBusquedaBinding
import com.bumptech.glide.Glide

sealed class ItemBusqueda {
    data class Ambiente(val data: ModeloAmbientes) : ItemBusqueda()
    data class Equipo(val data: ModeloEquipos) : ItemBusqueda()
}

class AdapterBusqueda(
    private val lista: List<ItemBusqueda>,
    private val onItemClick: (ItemBusqueda) -> Unit
) : RecyclerView.Adapter<AdapterBusqueda.ViewHolder>() {

    inner class ViewHolder(val binding: ItemBusquedaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ItemBusqueda) {
            when (item) {
                is ItemBusqueda.Ambiente -> {
                    binding.tvNombre.text = item.data.nombre
                    val url = "https://cf7e2811433e.ngrok-free.app/phpGestionReservas/${item.data.imagen}"
                    Glide.with(binding.root.context).load(url).into(binding.ivRecurso)
                }
                is ItemBusqueda.Equipo -> {
                    binding.tvNombre.text = "${item.data.marca} ${item.data.modelo}"
                    val url = "https://cf7e2811433e.ngrok-free.app/phpGestionReservas/${item.data.imagen}"
                    Glide.with(binding.root.context).load(url).into(binding.ivRecurso)
                }
            }
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBusquedaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(lista[position])
    }
}
