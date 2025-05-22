package com.amkj.appreservascab

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amkj.appreservascab.Admin.AdminRegistrarUsuario
import com.amkj.appreservascab.databinding.ActivityRecuperarContrasenaBinding

class RecuperarContrasena : AppCompatActivity() {

    private lateinit var binding: ActivityRecuperarContrasenaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRecuperarContrasenaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        AdminRegistrarUsuario.iniciar(applicationContext)

        binding.btnEnviarInstrucciones.setOnClickListener{
            val intent = Intent(this, RCCorreoEnviado::class.java)
            startActivity(intent)
        }

    }

}