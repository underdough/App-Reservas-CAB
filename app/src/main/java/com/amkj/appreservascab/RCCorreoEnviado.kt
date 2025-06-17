package com.amkj.appreservascab

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.amkj.appreservascab.databinding.ActivityRccorreoEnviadoBinding
import com.amkj.appreservascab.Modelos.ModeloVerificarToken
import com.amkj.appreservascab.servicios.RetrofitClient
import kotlinx.coroutines.launch

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

        binding.btnVerificar.setOnClickListener {
            val tokenIngresado = binding.etToken.text.toString().trim()
            val correo = intent.getStringExtra("correo_usuario") ?: ""

            if (tokenIngresado.isNotEmpty() && correo.isNotEmpty()) {
                verificarToken(correo, tokenIngresado)
            } else {
                Toast.makeText(this, "Debe ingresar el token y haber enviado el correo previamente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verificarToken(correo: String, token: String) {
        val api = RetrofitClient.instance

        lifecycleScope.launch {
            try {
                val response = api.verificarToken(ModeloVerificarToken(correo, token))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.message != null) {
                        Toast.makeText(this@RCCorreoEnviado, body.message, Toast.LENGTH_LONG).show()
                        // Aquí podrías redirigir al usuario a la pantalla para cambiar la contraseña
                        startActivity(Intent(this@RCCorreoEnviado, CambiarContrasena::class.java)
                            .putExtra("correo_usuario", correo))
                        finish()
                    } else {
                        Toast.makeText(this@RCCorreoEnviado, body?.error ?: "Token inválido", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@RCCorreoEnviado, "Error de validación", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RCCorreoEnviado, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
