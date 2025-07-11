package com.amkj.appreservascab

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amkj.appreservascab.Modelos.ModeloActualizarContrasena
import com.amkj.appreservascab.databinding.ActivityCambiarContrasenaBinding
import com.amkj.appreservascab.Modelos.ModeloUsuarios
import com.amkj.appreservascab.Usuarios.SesionUsuarioPrefs
import com.amkj.appreservascab.servicios.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CambiarContrasena : AppCompatActivity() {

    private lateinit var binding: ActivityCambiarContrasenaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val correoRecibido = intent.getStringExtra("correo_usuario") ?: ""
        Log.d("amkj", "Correo recibido por intent: $correoRecibido")

        // Iniciar preferencias para obtener datos del usuario
        SesionUsuarioPrefs.iniciar(this)

        binding = ActivityCambiarContrasenaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clRestablecerContrasena)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnRestablecerContrasena.setOnClickListener {
            val nuevaContra = binding.etContrasena.text.toString().trim()
            val confirmarContra = binding.etConfirmarContrasena.text.toString().trim()

            if (nuevaContra.isEmpty() || confirmarContra.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nuevaContra != confirmarContra) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val usuarioActualizado = ModeloActualizarContrasena(
                correo = correoRecibido,
                contrasena = nuevaContra
            )



            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.instance.actualizarContrasena(usuarioActualizado)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            Toast.makeText(this@CambiarContrasena, "Contraseña actualizada", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@CambiarContrasena, MainActivity::class.java)
                            startActivity(intent)
                            Log.d("amkj", "Datos enviados: $usuarioActualizado")

                        } else {
                            Toast.makeText(this@CambiarContrasena, "Error al actualizar", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CambiarContrasena, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
