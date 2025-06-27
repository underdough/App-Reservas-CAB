package com.amkj.appreservascab.Adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amkj.appreservascab.Modelos.ModeloReserva
import com.amkj.appreservascab.databinding.ItemReservaBinding

class AdapterReservas(private val reservas: List<ModeloReserva>) :
    RecyclerView.Adapter<AdapterReservas.ReservaViewHolder>() {

    inner class ReservaViewHolder(val binding: ItemReservaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reserva: ModeloReserva) {
            binding.tvNombreAmbiente.text = reserva.ambiente_nombre
            binding.tvFecha.text = "${reserva.fecha_hora_inicio} - ${reserva.fecha_hora_fin}"
            binding.tvJornadas.text = reserva.jornadas
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservaViewHolder {
        val binding = ItemReservaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReservaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReservaViewHolder, position: Int) {
        holder.bind(reservas[position])
    }

    override fun getItemCount(): Int = reservas.size
}
