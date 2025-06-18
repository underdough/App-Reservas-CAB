package com.amkj.appreservascab

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.amkj.appreservascab.databinding.ActivityRecuperarContrasenaBinding
import com.amkj.appreservascab.Modelos.ModeloCorreo
import com.amkj.appreservascab.servicios.RetrofitClient
import kotlinx.coroutines.launch

class RecuperarContrasena : AppCompatActivity() {

    private lateinit var binding: ActivityRecuperarContrasenaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRecuperarContrasenaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutRecuperarContrasena)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.ibVolver.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        // Acción al presionar el botón para enviar código
        binding.btnEnviarInstrucciones.setOnClickListener {
            val correo = binding.etCorreoElectronico.text.toString().trim()

            if (correo.isNotEmpty()) {
                enviarCodigoPorCorreo(correo)
            } else {
                Toast.makeText(this, "Por favor ingresa tu correo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enviarCodigoPorCorreo(correo: String) {
        val api = RetrofitClient.instance

        lifecycleScope.launch {
            try {
                val response = api.enviarToken(ModeloCorreo(correo))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.message != null) {
                        Toast.makeText(this@RecuperarContrasena, body.message, Toast.LENGTH_LONG).show()

                        val intent = Intent(this@RecuperarContrasena, RCCorreoEnviado::class.java)
                        intent.putExtra("correo_usuario", correo)
                        startActivity(intent)

                    } else {
                        Toast.makeText(this@RecuperarContrasena, body?.error ?: "Error desconocido", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@RecuperarContrasena, "Error del servidor", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RecuperarContrasena, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
