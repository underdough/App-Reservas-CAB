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
import android.util.Log

class AdapterEquipos(
    private var listaEquipos: List<ModeloEquipos>,
    private val onItemClick: (ModeloEquipos) -> Unit
) : RecyclerView.Adapter<AdapterEquipos.EquipoViewHolder>() {

    companion object {
        // Ajusta a tu dominio actual; DEBE terminar con "/"
        private const val BASE_URL = "https://emma-essential-mae-singh.trycloudflare.com/phpGestionReservas/"
        private const val TAG = "ADAPTER_EQUIPOS"
    }

    private var listaEquiposOriginal: List<ModeloEquipos> = listOf()

    inner class EquipoViewHolder(val binding: ItemEquipoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(equipo: ModeloEquipos) {
            binding.tvMarca.text = equipo.marca
            binding.tvModelo.text = equipo.modelo
            binding.tvDescripcion.text = equipo.descripcion

            val url = buildImageUrl(equipo.imagen)
            Log.d(TAG, "img raw=${equipo.imagen} -> final=$url")

            Glide.with(binding.root.context)
                .load(url)
                .placeholder(R.drawable.imagen_error) // usa tu placeholder
                .error(R.drawable.imagen_error)
                .into(binding.ivEquipo)

            binding.root.setOnClickListener { onItemClick(equipo) }

            binding.ibirReservas.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, SolicitudReservasEquipo::class.java)
                intent.putExtra("equipo", equipo)
                context.startActivity(intent)
            }
        }
    }

    /** Normaliza la URL: si ya es absoluta (http/https), la deja; si es relativa, antepone BASE_URL */
    private fun buildImageUrl(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val trimmed = raw.trim()
        return if (trimmed.startsWith("http://", true) || trimmed.startsWith("https://", true)) {
            trimmed
        } else {
            BASE_URL + trimmed.trimStart('/')
        }
    }

    fun filtrar(query: String) {
        val filtrada = listaEquiposOriginal.filter {
            it.marca.contains(query, ignoreCase = true) ||
                    it.modelo.contains(query, ignoreCase = true)
        }
        listaEquipos = filtrada
        notifyDataSetChanged()
    }

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
