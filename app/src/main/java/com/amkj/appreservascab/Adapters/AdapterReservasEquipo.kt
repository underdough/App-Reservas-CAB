package com.amkj.appreservascab.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amkj.appreservascab.Adaptadores.AdapterReservas
import com.amkj.appreservascab.Modelos.ModeloReserva
import com.amkj.appreservascab.Modelos.ModeloReservaEquipo
import com.amkj.appreservascab.databinding.ItemReservaBinding
import com.amkj.appreservascab.databinding.ItemReservaEquipoBinding

class AdapterReservasEquipo(private val reservasEquipo: List<ModeloReservaEquipo>) :
    RecyclerView.Adapter<AdapterReservasEquipo.ReservaViewHolder>() {

    inner class ReservaViewHolder(val binding: ItemReservaEquipoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reservaEquipo: ModeloReservaEquipo) {
            binding.tvMarca.text = reservaEquipo.marca
            binding.tvModelo.text = reservaEquipo.modelo
            binding.tvNombre.text = reservaEquipo.nombre
            binding.tvFecha.text = reservaEquipo.fecha
            binding.tvJornadas.text = reservaEquipo.jornadas.joinToString(", ")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservaViewHolder {
        val binding = ItemReservaEquipoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReservaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReservaViewHolder, position: Int) {
        holder.bind(reservasEquipo[position])
    }

    override fun getItemCount(): Int = reservasEquipo.size
}
