package com.amkj.appreservascab.Adaptadores

import android.content.Intent
import android.graphics.Bitmap
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amkj.appreservascab.databinding.ItemEquipoBinding
import android.graphics.BitmapFactory
import com.amkj.appreservascab.Modelos.ModeloEquipos
import com.amkj.appreservascab.R
import com.amkj.appreservascab.SolicitudReservas
import com.bumptech.glide.Glide

class AdapterEquipos(
    private val listaEquipos: List<ModeloEquipos>,
    private val onItemClick: (ModeloEquipos) -> Unit
) : RecyclerView.Adapter<AdapterEquipos.EquipoViewHolder>() {

    inner class EquipoViewHolder(val binding: ItemEquipoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(equipo: ModeloEquipos) {
            binding.tvMarca.text = "${equipo.marca}"
            binding.tvModelo.text = "${equipo.modelo}"
            binding.tvDescripcion.text = "${equipo.modelo}"
            val url = "http://192.168.1.23/phpGestionReservas/${equipo.imagen}"
            Glide.with(binding.root.context).load(url).into(binding.ivEquipo)

            binding.root.setOnClickListener {
                onItemClick(equipo) // <-- Aquí se pasa el modelo correctamente
            }

            binding.ibirReservas.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, SolicitudReservas::class.java)
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


//class AdapterEquipos(private val listaEquipos: List<ModeloEquipos>) :
//    RecyclerView.Adapter<AdapterEquipos.EquipoViewHolder>() {
//
//    inner class EquipoViewHolder(val binding: ItemEquipoBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipoViewHolder {
//        val binding = ItemEquipoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return EquipoViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: EquipoViewHolder, position: Int) {
//        val equipo = listaEquipos[position]
//        holder.binding.tvMarca.text = equipo.marca
//        holder.binding.tvModelo.text = equipo.modelo
//        holder.binding.tvDescripcion.text = equipo.descripcion
//        // URL completa (si solo guardas el nombre o ruta en la BD)
//        val urlImagen = "http://192.168.1.23/phpGestionReservas/" + equipo.imagen
//
//        Glide.with(holder.itemView.context)
//            .load(urlImagen)
//            .placeholder(R.drawable.imagen_error) // mientras carga
//            .error(R.drawable.imagen_error)       // si falla
//            .into(holder.binding.ivEquipo)
////        holder.binding.ivEquipo.setImageBitmap(decodeBase64ToBitmap(equipo.imagen))
////
////        // Convertir base64 a imagen
////        val imageBytes = Base64.decode(equipo.imagen, Base64.DEFAULT)
////        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
////        holder.binding.ivEquipo.setImageBitmap(decodedImage)
//    }
//
//    fun convertirBase64ABitmap(base64: String): Bitmap? {
//        return try {
//            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
//            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//
//    override fun getItemCount(): Int = listaEquipos.size
//}
