package com.amkj.appreservascab

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amkj.appreservascab.Admin.AdminRegistrarUsuario
import com.amkj.appreservascab.databinding.ActivityRccorreoEnviadoBinding

class RCCorreoEnviado : AppCompatActivity() {

    private lateinit var binding: ActivityRccorreoEnviadoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRccorreoEnviadoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutCorreoEnviado)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        AdminRegistrarUsuario.iniciar(applicationContext)

        // Provisional Pruebas para poner el link de recuperación via gmail
        binding.btnAbrirGmail.setOnClickListener {
            val intent = Intent(this, CambiarContrasena::class.java)
            startActivity(intent)

        }
    }
}