package com.amkj.appreservascab.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amkj.appreservascab.Modelos.Notificacion
import com.amkj.appreservascab.databinding.ItemNotificacionBinding

class NotificacionesAdapter(
    private var lista: List<Notificacion>,
    private val onItemClick: (Notificacion) -> Unit
) : RecyclerView.Adapter<NotificacionesAdapter.NotificacionViewHolder>() {


    inner class NotificacionViewHolder(val binding: ItemNotificacionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionViewHolder {
        val binding = ItemNotificacionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificacionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        val notificacion = lista[position]
        Log.d("AdapterNotificaciones", "Mostrando notificación: ${notificacion.id} - ${notificacion.mensaje}")

        val b = holder.binding

        b.txtMensajeNotificacion.text = notificacion.mensaje

        // Mostrar indicador si no está leída
        b.indicadorNoLeida.visibility = if (notificacion.leida == 0) View.VISIBLE else View.GONE

        // Mostrar el tiempo (puedes usar una función util para calcular "hace 1h", etc.)
        b.txtTiempoNotificacion.text = formatearFecha(notificacion.fecha_creacion)

        // Puedes modificar el icono si quieres usar diferentes para ambiente/equipo
        b.iconoTipoNotificacion.visibility = View.VISIBLE

        // Aquí agregamos el clic
        holder.itemView.setOnClickListener {
            onItemClick(notificacion)
        }
    }


    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<Notificacion>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }

    // Función para mostrar fecha relativa simple (puedes mejorarla luego)
    private fun formatearFecha(fecha: String): String {
        return "Fecha: $fecha"  // Aquí podrías convertir a "hace 1h", etc.
    }
}
