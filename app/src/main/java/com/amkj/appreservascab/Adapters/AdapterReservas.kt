package com.amkj.appreservascab.Adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amkj.appreservascab.Modelos.ModeloReserva
import com.amkj.appreservascab.R
import com.amkj.appreservascab.databinding.ItemReservaBinding
import com.bumptech.glide.Glide

class AdapterReservas(private val reservas: List<ModeloReserva>) :
    RecyclerView.Adapter<AdapterReservas.ReservaViewHolder>() {

    inner class ReservaViewHolder(val binding: ItemReservaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reserva: ModeloReserva) {
            binding.tvNombreAmbiente.text = "Ambiente: ${reserva.ambiente_nombre ?: "Desconocido"}"
            binding.tvFecha.text = "${reserva.fecha_hora_inicio} - ${reserva.fecha_hora_fin}"
            binding.tvJornadas.text = reserva.jornadas
            binding.tvEstado.text = "Estado: ${reserva.estado ?: "Desconocido"}"
            if (!reserva.ambiente_imagen.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(reserva.ambiente_imagen)
                    .placeholder(R.drawable.placeholder_ambiente)
                    .error(R.drawable.placeholder_ambiente)
                    .into(binding.ivAmbiente)
            } else {
                binding.ivAmbiente.setImageResource(R.drawable.placeholder_ambiente)
            }


            val imagenUrl = reserva.ambiente_imagen
            if (!imagenUrl.isNullOrEmpty()) {
                val url = "http://192.168.1.5/phpGestionReservas/imagenesAmbientes/$imagenUrl"
                Glide.with(binding.ivAmbiente.context)
                    .load(url)
                    .placeholder(R.drawable.placeholder_ambiente)
                    .into(binding.ivAmbiente)
            } else {
                binding.ivAmbiente.setImageResource(R.drawable.placeholder_ambiente)
            }
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
