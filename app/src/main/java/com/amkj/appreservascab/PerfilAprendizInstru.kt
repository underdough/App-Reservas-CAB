package com.amkj.appreservascab

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.amkj.appreservascab.Modelos.ModeloUsuarios
import com.amkj.appreservascab.Adapters.PerfilAdapter
import com.amkj.appreservascab.databinding.ActivityMainBinding
import com.amkj.appreservascab.databinding.ActivityPerfilAprendizInstruBinding

class PerfilAprendizInstru : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilAprendizInstruBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilAprendizInstruBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainlyPerfilAprendizInstru)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //  Obtener datos guardados localmente (SharedPreferences)
        val prefs = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
        val usuario = ModeloUsuarios(
            id = prefs.getString("id", "") ?: "",
            correo = prefs.getString("correo", null),
            contrasena = prefs.getString("contrasena", null),
            nombre = prefs.getString("nombre", "Nombre no disponible") ?: "Nombre no disponible",
            rol = prefs.getString("rol", "Rol no asignado") ?: "Rol no asignado",
            bloque = prefs.getString("bloque", null)
        )

        // Mostrar nombre en el TextView del activity principal
        binding.tvNomUser.text = usuario.nombre

        // Mostrar en RecyclerView
        val adapter = PerfilAdapter(listOf(usuario))
        binding.recycler.layoutManager = LinearLayoutManager(this@PerfilAprendizInstru)
        binding.recycler.adapter = adapter


        binding.ibVolver.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }
    }

}

