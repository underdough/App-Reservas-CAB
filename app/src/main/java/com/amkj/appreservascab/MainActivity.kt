package com.amkj.appreservascab

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amkj.appreservascab.Admin.AdminRegistrarUsuario
import com.amkj.appreservascab.Modelos.ModeloDatosUsuarios
import com.amkj.appreservascab.Modelos.ModeloUsuarios
import com.amkj.appreservascab.ModelView.UsuarioViewModel
import com.amkj.appreservascab.databinding.ActivityMainBinding
import com.amkj.appreservascab.servicios.ConexionDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val usuarioViewModel: UsuarioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        listarUsuario()

        AdminRegistrarUsuario.iniciar(applicationContext)




        binding.ivVerContra.setOnClickListener {
            var contraVisible = false
            binding.ivVerContra.setOnClickListener {
                if (contraVisible) {
                    // Ocultar contraseña
                    binding.etContra.transformationMethod = null
                    binding.ivVerContra.setImageResource(R.drawable.ic_ojo_abierto)
                    binding.etContra.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.ivVerContra.setImageResource(R.drawable.ic_ojo_cerrado) // ← usa tu ícono de "ojo cerrado"
                } else {
                    // Mostrar contraseña
                    binding.etContra.transformationMethod =
                        android.text.method.PasswordTransformationMethod.getInstance()
                    binding.ivVerContra.setImageResource(R.drawable.ic_ojo_cerrado) // ícono de ojo normal
                    binding.etContra.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                            android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    binding.ivVerContra.setImageResource(R.drawable.ic_ojo_abierto) // ← usa tu ícono de "ojo abierto"
                }
                // Mueve el cursor al final del texto
                binding.etContra.setSelection(binding.etContra.text.length)
                contraVisible = !contraVisible
            }

        }

        // Evento para ingreso a el inicio
        binding.btnIniciar.setOnClickListener {

            val correo = binding.etEmail.text.toString().trim()
            val contrasena = binding.etContra.text.toString().trim()

            if (correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "No se permiten campos vacíos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener

            } else{
                val intent = Intent(this, VistaPrincipal::class.java)
                startActivity(intent)
            }



            binding.tvOlvidaste.setOnClickListener {
                val intent = Intent(this, RecuperarContrasena::class.java)
                startActivity(intent)
            }
        }

//    private fun listarUsuario() {
//        usuarioViewModel.addUsuarioLista(ModeloDatosUsuarios.datos.toMutableList())
//        iniciarRecyclerView()
//    }

//    private fun iniciarRecyclerView() {
//        TODO("Not yet implemented")
//    }



    }
         private fun listarUsuario() {
        fun getRetrofit(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(ConexionDB.url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val llamada = getRetrofit().create(ConexionDB::class.java).consultaUsuario()
                    if (llamada.isSuccessful && llamada.body() != null) {
                        withContext(Dispatchers.Main) {
                            usuarioViewModel.addUsuarioLista(llamada.body()!!.toMutableList())
                        }
                    } else {
                        Log.e("amkj", "Error: No se encontró información")
                    }
                } catch (e: Exception) {
                    Log.e("amkj", "No se pudo conectar al backend", e)
                }
            }
        }
}
