package com.amkj.appreservascab

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import androidx.privacysandbox.tools.core.generator.build
import com.amkj.appreservascab.databinding.ActivityMainBinding
import com.amkj.appreservascab.servicios.ConexionDB
import com.amkj.appreservascab.Modelos.ModeloUsuarios
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivVerContra.setOnClickListener {
            var contraVisible = false
            binding.ivVerContra.setOnClickListener {
                if (contraVisible) {
                    // Ocultar contraseña
                    binding.etContra.transformationMethod = null
                    binding.ivVerContra.setImageResource(R.drawable.ic_ojo_abierto)
                    binding.etContra.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.ivVerContra.setImageResource(R.drawable.ic_ojo_cerrado)
                } else {
                    // Mostrar contraseña
                    binding.etContra.transformationMethod =
                        android.text.method.PasswordTransformationMethod.getInstance()
                    binding.ivVerContra.setImageResource(R.drawable.ic_ojo_cerrado)
                    binding.etContra.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                            android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    binding.ivVerContra.setImageResource(R.drawable.ic_ojo_abierto)
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
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val retrofit = Retrofit.Builder()
                            .baseUrl(ConexionDB.URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()

                        val service = retrofit.create(ConexionDB::class.java)
                        val response = service.consultaUsuario()

                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            if (responseBody != null && responseBody.isNotEmpty()) {
                                Log.d("APIResponse", "Response: $responseBody")
                                withContext(Dispatchers.Main) {
                                    val usuarios = responseBody
                                    if (usuarios.isNotEmpty()) {
                                        val intent = Intent(this@MainActivity, VistaPrincipal::class.java)
                                        startActivity(intent)
                                    } else {
                                        Toast.makeText(this@MainActivity, "Correo o contraseña incorrectos.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Log.e("APIError", "Respuesta del servidor vacía")
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@MainActivity, "Error al conectar al servidor.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Log.e("APIError", "Error en la respuesta del servidor: ${response.errorBody()?.string()}")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "Error al conectar al servidor.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            val logging = HttpLoggingInterceptor()
                            logging.setLevel(HttpLoggingInterceptor.Level.BODY) // Log request and response bodies
                            val httpClient = OkHttpClient.Builder()
                                .addInterceptor(logging)
                                .build()

                            val retrofit = Retrofit.Builder()
                                .baseUrl("your_base_url")
                                .addConverterFactory(GsonConverterFactory.create())
                                .client(httpClient)
                                .build()
//                            Log.e("APIError", "Error al realizar la solicitud: ${e.message}")
//                            val retrofit = Retrofit.Builder()
//                                .baseUrl(ConexionDB.URL)
//                                .addConverterFactory(GsonConverterFactory.create())
//                                .build()
//                            val service = retrofit.create(ConexionDB::class.java)
//                            val response = service.consultaUsuario()
//                            Log.d("APIResponse", "Response: ${response.body()}")
//                            Toast.makeText(this@MainActivity, "Error al conectar al servidor.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        binding.tvOlvidaste.setOnClickListener {
            val intent = Intent(this, RecuperarContrasena::class.java)
            startActivity(intent)
        }
    }
}