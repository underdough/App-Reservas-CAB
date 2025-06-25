package com.amkj.appreservascab

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.amkj.appreservascab.Modelos.ModeloEquipos
import com.amkj.appreservascab.databinding.ActivityDetalleEquipoBinding
import com.bumptech.glide.Glide

class DetalleEquipo : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleEquipoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleEquipoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Aquí recibes el objeto enviado desde VistaPrincipal
        val equipo = intent.getParcelableExtra<ModeloEquipos>("equipo")

        // Validación por si llega null
        if (equipo != null) {
            binding.tvMarca.text = equipo.marca
            binding.tvModelo.text = equipo.modelo
            binding.tvDescripcion.text = equipo.descripcion

            val urlImagen = "http://192.168.1.23/phpGestionReservas/" + equipo.imagen
            Glide.with(this)
                .load(urlImagen)
                .into(binding.ivEquipo)
        } else {
            // Podrías mostrar un mensaje o cerrar la actividad si no llegó nada
            finish()
        }

        // Volver
        binding.ibVolver.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

}
