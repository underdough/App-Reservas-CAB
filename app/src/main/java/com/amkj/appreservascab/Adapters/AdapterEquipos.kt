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
    private val listaEquipos: List<ModeloEquipos>,
    private val onItemClick: (ModeloEquipos) -> Unit
) : RecyclerView.Adapter<AdapterEquipos.EquipoViewHolder>() {

    inner class EquipoViewHolder(val binding: ItemEquipoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(equipo: ModeloEquipos) {
            binding.tvMarca.text = equipo.marca
            binding.tvModelo.text = equipo.modelo
            binding.tvDescripcion.text = equipo.descripcion

            val url = "http://192.168.1.23:80/phpGestionReservas/${equipo.imagen}"
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipoViewHolder {
        val binding = ItemEquipoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EquipoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EquipoViewHolder, position: Int) {
        holder.bind(listaEquipos[position])
    }

    override fun getItemCount() = listaEquipos.size
}
