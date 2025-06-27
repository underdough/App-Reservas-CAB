package com.amkj.appreservascab.Adapters



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.amkj.appreservascab.R
import com.amkj.appreservascab.Modelos.Notificacion
import com.amkj.appreservascab.Modelos.obtenerMensajeFormateado
import com.amkj.appreservascab.Modelos.obtenerTiempoTranscurrido

/**
 * Adaptador para mostrar la lista de notificaciones en RecyclerView
 */
class AdaptadorNotificaciones(
    private val alHacerClickNotificacion: (Notificacion) -> Unit
) : ListAdapter<Notificacion, AdaptadorNotificaciones.ViewHolderNotificacion>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Notificacion>() {
            override fun areItemsTheSame(oldItem: Notificacion, newItem: Notificacion): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Notificacion, newItem: Notificacion): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderNotificacion {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacion, parent, false)
        return ViewHolderNotificacion(vista)
    }

    override fun onBindViewHolder(holder: ViewHolderNotificacion, position: Int) {
        val notificacion = getItem(position)
        holder.vincular(notificacion)
    }

    /**
     * ViewHolder que maneja cada item de notificación
     */
    inner class ViewHolderNotificacion(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imgAvatarUsuario: ImageView = itemView.findViewById(R.id.img_avatar_usuario)
        private val txtMensajeNotificacion: TextView = itemView.findViewById(R.id.txt_mensaje_notificacion)
        private val txtTiempoNotificacion: TextView = itemView.findViewById(R.id.txt_tiempo_notificacion)
        private val indicadorNoLeida: View = itemView.findViewById(R.id.indicador_no_leida)
        private val iconoTipoNotificacion: ImageView? = itemView.findViewById(R.id.icono_tipo_notificacion)

        fun vincular(notificacion: Notificacion) {
            // Configurar mensaje principal
            txtMensajeNotificacion.text = notificacion.obtenerMensajeFormateado()

            // Configurar tiempo transcurrido
            txtTiempoNotificacion.text = notificacion.obtenerTiempoTranscurrido()

            // Mostrar/ocultar indicador de no leída
            indicadorNoLeida.visibility = if (notificacion.esLeida) View.GONE else View.VISIBLE

            // Configurar icono del tipo de notificación
            iconoTipoNotificacion?.let { icono ->
                icono.setImageResource(notificacion.tipoNotificacion.icono)
                icono.visibility = View.VISIBLE
            }

            // Cargar avatar del usuario
            cargarAvatarUsuario(notificacion)

            // Configurar opacidad según si está leída o no
            configurarEstadoLectura(notificacion.esLeida)

            // Configurar click listener
            itemView.setOnClickListener {
                alHacerClickNotificacion(notificacion)
            }
        }

        private fun cargarAvatarUsuario(notificacion: Notificacion) {
            if (!notificacion.avatarUsuario.isNullOrEmpty()) {
                // Si tiene URL de avatar, cargarla con Glide
                Glide.with(itemView.context)
                    .load(notificacion.avatarUsuario)
                    .transform(CircleCrop())
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .into(imgAvatarUsuario)
            } else {
                // Si no tiene avatar, usar el predeterminado
                imgAvatarUsuario.setImageResource(R.drawable.ic_user)
            }
        }

        private fun configurarEstadoLectura(esLeida: Boolean) {
            val alpha = if (esLeida) 0.6f else 1.0f

            txtMensajeNotificacion.alpha = alpha
            txtTiempoNotificacion.alpha = alpha
            imgAvatarUsuario.alpha = alpha
            iconoTipoNotificacion?.alpha = alpha
        }
    }

    /**
     * Función para marcar una notificación como leída
     */
    fun marcarComoLeida(notificacion: Notificacion) {
        val listaActualizada = currentList.toMutableList()
        val indice = listaActualizada.indexOfFirst { it.id == notificacion.id }

        if (indice != -1) {
            listaActualizada[indice] = notificacion.copy(esLeida = true)
            submitList(listaActualizada)
        }
    }

    /**
     * Función para obtener el número de notificaciones no leídas
     */
    fun obtenerNumeroNoLeidas(): Int {
        return currentList.count { !it.esLeida }
    }
}