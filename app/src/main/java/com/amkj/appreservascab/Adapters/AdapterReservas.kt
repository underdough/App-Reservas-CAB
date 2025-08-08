package com.amkj.appreservascab.Adaptadores

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amkj.appreservascab.Modelos.ModeloReserva
import com.amkj.appreservascab.R
import com.amkj.appreservascab.databinding.ItemReservaBinding
import com.bumptech.glide.Glide

class AdapterReservas(
    private var reservas: List<ModeloReserva>,
    private val onEliminarClick: (ModeloReserva) -> Unit
) : RecyclerView.Adapter<AdapterReservas.ReservaViewHolder>() {

    inner class ReservaViewHolder(val binding: ItemReservaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reserva: ModeloReserva) {
            val b = binding

            b.tvNombreAmbiente.text = "Ambiente: ${reserva.ambiente_nombre ?: "Desconocido"}"
            b.tvFecha.text = "${reserva.fecha_hora_inicio} - ${reserva.fecha_hora_fin}"
            b.tvJornadas.text = "Jornadas: ${reserva.jornadas}"
            b.tvEstado.text = "Estado: ${reserva.estado ?: "Desconocido"}"

            val imagenNombre = reserva.ambiente_imagen
            if (!imagenNombre.isNullOrEmpty()) {
                val url = "https://cf7e2811433e.ngrok-free.app/phpGestionReservas/imagenesAmbientes/$imagenNombre"
                Glide.with(b.ivAmbiente.context)
                    .load(url)
                    .placeholder(R.drawable.placeholder_ambiente)
                    .error(R.drawable.placeholder_ambiente)
                    .into(b.ivAmbiente)
            } else {
                b.ivAmbiente.setImageResource(R.drawable.placeholder_ambiente)
            }

            // Mostrar botón cancelar solo si el estado es "pendiente"
            b.btnCancelarReserva.visibility = if (reserva.estado == "pendiente") View.VISIBLE else View.GONE

            // Acción del botón cancelar
            b.btnCancelarReserva.setOnClickListener {
                onEliminarClick(reserva)
            }
        }
        fun actualizarLista(nuevaLista: List<ModeloReserva>) {
            reservas = nuevaLista
            notifyDataSetChanged()
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





//package com.amkj.appreservascab.Adaptadores
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.amkj.appreservascab.Modelos.ModeloReserva
//import com.amkj.appreservascab.R
//import com.amkj.appreservascab.databinding.ItemReservaBinding
//import com.bumptech.glide.Glide
//
//class AdapterReservas(private val reservas: List<ModeloReserva>) :
//    RecyclerView.Adapter<AdapterReservas.ReservaViewHolder>() {
//
//    inner class ReservaViewHolder(val binding: ItemReservaBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(reserva: ModeloReserva) {
//            val b = binding
//
//            b.tvNombreAmbiente.text = "Ambiente: ${reserva.ambiente_nombre ?: "Desconocido"}"
//            b.tvFecha.text = "${reserva.fecha_hora_inicio} - ${reserva.fecha_hora_fin}"
//            b.tvJornadas.text = "Jornadas: ${reserva.jornadas}"
//            b.tvEstado.text = "Estado: ${reserva.estado ?: "Desconocido"}"
//
//            val imagenNombre = reserva.ambiente_imagen
//            if (!imagenNombre.isNullOrEmpty()) {
//                val url = "http://192.168.1.4/phpGestionReservas/imagenesAmbientes/$imagenNombre"
//                Glide.with(b.ivAmbiente.context)
//                    .load(url)
//                    .placeholder(R.drawable.placeholder_ambiente)
//                    .error(R.drawable.placeholder_ambiente)
//                    .into(b.ivAmbiente)
//            } else {
//                b.ivAmbiente.setImageResource(R.drawable.placeholder_ambiente)
//            }
//
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservaViewHolder {
//        val binding = ItemReservaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return ReservaViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: ReservaViewHolder, position: Int) {
//        holder.bind(reservas[position])
//    }
//
//    override fun getItemCount(): Int = reservas.size
//}
