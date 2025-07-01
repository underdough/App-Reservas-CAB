package com.amkj.appreservascab.Adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amkj.appreservascab.Modelos.ModeloReservaEquipo
import com.amkj.appreservascab.databinding.ItemReservaBinding
import com.bumptech.glide.Glide
import com.amkj.appreservascab.R

class AdapterReservasEquipo(private val reservas: List<ModeloReservaEquipo>) :
    RecyclerView.Adapter<AdapterReservasEquipo.ReservaViewHolder>() {

    inner class ReservaViewHolder(val binding: ItemReservaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reserva: ModeloReservaEquipo) {
            // Mostrar nombre del equipo
            binding.tvNombreAmbiente.text = "Equipo: ${reserva.equipo_nombre ?: "Desconocido"}"

            // Mostrar fecha de reserva
            binding.tvFecha.text = "Fecha: ${reserva.fecha}"

            // Mostrar jornadas
            binding.tvJornadas.text = "Jornadas: ${reserva.jornadas}"

            // Cargar imagen con Glide
            val url = "http://192.168.0.9:80/phpGestionReservas/${reserva.equipo_imagen ?: ""}"
            Glide.with(binding.root.context)
                .load(url)
                .placeholder(R.drawable.placeholder_portatil)
                .error(R.drawable.imagen_error)
                .into(binding.ivAmbiente)
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
