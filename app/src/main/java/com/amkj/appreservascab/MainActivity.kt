package com.amkj.appreservascab

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
<<<<<<< HEAD
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
=======
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amkj.appreservascab.Admin.AdminRegistrarUsuario
import com.amkj.appreservascab.databinding.ActivityMainBinding

>>>>>>> cd4810b805f411e97cae0baa3b90201d8a46e73d

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
<<<<<<< HEAD
=======
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

            AdminRegistrarUsuario.iniciar(applicationContext)


>>>>>>> cd4810b805f411e97cae0baa3b90201d8a46e73d

        binding.ivVerContra.setOnClickListener {
            var contraVisible = false
            binding.ivVerContra.setOnClickListener {
                if (contraVisible) {
                    // Ocultar contraseña
                    binding.etContra.transformationMethod = null
                    binding.ivVerContra.setImageResource(R.drawable.ic_ojo_abierto)
<<<<<<< HEAD
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
=======
//                    binding.etContra.inputType = android.text.InputType.TYPE_CLASS_TEXT or
//                            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
//                    binding.ivVerContra.setImageResource(R.drawable.ic_ojo_cerrado) // ← usa tu ícono de "ojo cerrado"
                } else {
                    // Mostrar contraseña
                    binding.etContra.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                    binding.ivVerContra.setImageResource(R.drawable.ic_ojo_cerrado) // ícono de ojo normal
//                    binding.etContra.inputType = android.text.InputType.TYPE_CLASS_TEXT or
//                            android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
//                    binding.ivVerContra.setImageResource(R.drawable.ic_ojo_abierto) // ← usa tu ícono de "ojo abierto"
>>>>>>> cd4810b805f411e97cae0baa3b90201d8a46e73d
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
<<<<<<< HEAD
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

=======
            }

            val adminValido = AdminRegistrarUsuario.usuariosad.any {
                it.correo == correo && it.contrasena == contrasena
            }

            if (adminValido) {
                // prueba
                val intent = Intent(this, MenuAprendizInstru::class.java)
                intent.putExtra("correo", correo)
                startActivity(intent)


            } else {
                Toast.makeText(this, "El usuario no está registrado, comuníquese con el administrador del sistema", Toast.LENGTH_SHORT).show()
            }



        }


>>>>>>> cd4810b805f411e97cae0baa3b90201d8a46e73d
        binding.tvOlvidaste.setOnClickListener {
            val intent = Intent(this, RecuperarContrasena::class.java)
            startActivity(intent)
        }
    }
<<<<<<< HEAD
}
=======


    }
>>>>>>> cd4810b805f411e97cae0baa3b90201d8a46e73d
