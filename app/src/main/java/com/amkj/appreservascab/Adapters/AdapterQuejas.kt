package com.amkj.appreservascab.Adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amkj.appreservascab.Modelos.ModeloQueja
import com.amkj.appreservascab.R
import com.amkj.appreservascab.databinding.ItemQuejaBinding
import com.bumptech.glide.Glide
import android.util.Log

class AdapterQuejas(
    data: List<ModeloQueja>   // 👈 TIPADO CORRECTO (no List<T>)
) : RecyclerView.Adapter<AdapterQuejas.VH>() {

    companion object {
        // Cambia a tu dominio/base si el backend devuelve rutas relativas
        private const val BASE_URL = "https://intensive-shanghai-but-possible.trycloudflare.com/phpGestionReservas/"
        private const val TAG = "ADAPTER_QUEJAS"
    }

    private var original: List<ModeloQueja> = data
    private var shown: List<ModeloQueja> = data

    inner class VH(val b: ItemQuejaBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(q: ModeloQueja) {
            b.tvAsunto.text = q.asunto.ifBlank { "(sin asunto)" }
            b.tvDescripcion.text = q.descripcion
            b.tvEstado.text = "Estado: ${q.estado.ifBlank { "pendiente" }}"
            b.tvFecha.text = q.fecha_creacion
            b.tvUsuario.text = "Usuario: ${q.usuario_id} • ${q.rol}"

            val raw = q.imagen_url?.trim()
            if (!raw.isNullOrEmpty()) {
                val url = buildImageUrl(raw)
                Log.d(TAG, "imagen_url raw=$raw -> final=$url")
                b.ivAdjunta.visibility = View.VISIBLE
                Glide.with(b.root.context)
                    .load(url)
                    .placeholder(R.drawable.placeholder_ambiente)
                    .error(R.drawable.placeholder_ambiente)
                    .into(b.ivAdjunta)
            } else {
                b.ivAdjunta.visibility = View.GONE
            }
        }
    }

    /** Si ya es absoluta, la devuelve; si es relativa, la completa con BASE_URL */
    private fun buildImageUrl(raw: String): String {
        return if (raw.startsWith("http://", true) || raw.startsWith("https://", true)) {
            raw
        } else {
            BASE_URL + raw.trimStart('/')
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inf = LayoutInflater.from(parent.context)
        val binding = ItemQuejaBinding.inflate(inf, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(shown[position])

    override fun getItemCount(): Int = shown.size

    /** Reemplaza toda la data tras recarga */
    fun submitList(list: List<ModeloQueja>) {
        original = list
        shown = list
        notifyDataSetChanged()
    }

    /** Filtro simple */
    fun filter(query: String) {
        val f = query.trim().lowercase()
        shown = if (f.isEmpty()) {
            original
        } else {
            original.filter {
                (it.asunto).lowercase().contains(f) ||
                        (it.descripcion).lowercase().contains(f) ||
                        (it.estado).lowercase().contains(f) ||
                        (it.fecha_creacion).lowercase().contains(f)
            }
        }
        notifyDataSetChanged()
    }
}
