package com.amkj.appreservascab.Adaptadores

import android.app.AlertDialog
import android.util.Log
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

    companion object {
        // Tu dominio actual; DEBE terminar con "/"
        private const val BASE_URL = "https://emma-essential-mae-singh.trycloudflare.com/phpGestionReservas/"
        private const val TAG = "ADAPTER_RESERVAS"
    }

    inner class ReservaViewHolder(val binding: ItemReservaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reserva: ModeloReserva) {
            val b = binding

            b.tvNombreAmbiente.text = " ${reserva.ambiente_nombre}"
            b.tvFecha.text = "${reserva.fecha_hora_inicio} - ${reserva.fecha_hora_fin}"
            b.tvJornadas.text = "Jornadas: ${reserva.jornadas}"
            b.tvEstado.text = "Estado: ${reserva.estado ?: "Desconocido"}"

            val raw = reserva.ambiente_imagen
            val url = buildImageUrl(raw)
            Log.d(TAG, "img raw=$raw -> final=$url")

            Glide.with(b.ivAmbiente.context)
                .load(url)
                .placeholder(R.drawable.placeholder_ambiente)
                .error(R.drawable.placeholder_ambiente)
                .into(b.ivAmbiente)

            // Mostrar botón cancelar solo si el estado es "pendiente"
            b.btnCancelarReserva.visibility =
                if (reserva.estado == "pendiente") View.VISIBLE else View.GONE

            b.btnCancelarReserva.setOnClickListener {
                onEliminarClick(reserva)
            }
        }
    }

    /** Si ya es absoluta (http/https), la deja; si es relativa, antepone BASE_URL */
    private fun buildImageUrl(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val t = raw.trim()
        return if (t.startsWith("http://", true) || t.startsWith("https://", true)) {
            t
        } else {
            BASE_URL + t.trimStart('/')
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

    // (Opcional) si necesitas actualizar lista desde afuera:
    fun actualizarLista(nuevaLista: List<ModeloReserva>) {
        reservas = nuevaLista
        notifyDataSetChanged()
    }
}
