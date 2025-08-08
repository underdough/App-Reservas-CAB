package com.amkj.appreservascab

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.amkj.appreservascab.Modelos.ModeloAmbientes
import com.amkj.appreservascab.databinding.ActivityDetalleAmbienteBinding
import com.bumptech.glide.Glide

class DetalleAmbiente : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleAmbienteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleAmbienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibir el ambiente del intent
        val ambiente = intent.getParcelableExtra<ModeloAmbientes>("ambiente")

        if (ambiente != null) {
            binding.tvNombre.text = ambiente.nombre
            binding.tvDescripcion.text = ambiente.descripcion

            val urlImagen = "https://cf7e2811433e.ngrok-free.app/phpGestionReservas/${ambiente.imagen}"
            Glide.with(this)
                .load(urlImagen)
                .into(binding.ivAmbiente)
        } else {
            finish() // Si no hay datos, cerrar la actividad
        }
        // Volver
        binding.ibVolver.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

}
