package com.amkj.appreservascab

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amkj.appreservascab.databinding.ActivityMainBinding
import com.amkj.appreservascab.servicios.ConexionDB
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var contraVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivVerContra.setOnClickListener {
            contraVisible = !contraVisible
            if (contraVisible) {
                binding.etContra.transformationMethod = null
                binding.ivVerContra.setImageResource(R.drawable.ic_ojo_abierto)
            } else {
                binding.etContra.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.ivVerContra.setImageResource(R.drawable.ic_ojo_cerrado)
            }
            binding.etContra.setSelection(binding.etContra.text.length)
        }

        binding.btnIniciar.setOnClickListener {
            val correo = binding.etEmail.text.toString().trim()
            val contrasena = binding.etContra.text.toString().trim()

            if (correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "No se permiten campos vacíos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Detiene la ejecución si hay campos vacíos
            }

            CoroutineScope(Dispatchers.IO).launch {

                try {
                    val logging = HttpLoggingInterceptor()
                    logging.level = HttpLoggingInterceptor.Level.BODY

                    val client = OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .build()

                    val gson = GsonBuilder()
                        .setLenient()
                        .create()

                    val retrofit = Retrofit.Builder()
                        .baseUrl(ConexionDB.URL)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build()

                    val service = retrofit.create(ConexionDB::class.java)
                    val response = service.consultaUsuario()

                    if (response.isSuccessful) {
                        val usuarios = response.body()
                        withContext(Dispatchers.Main) {
                            val usuarioValido = usuarios?.find {
                                it.correo == correo && it.contrasena == md5(contrasena) || it.contrasena == contrasena
                            }


                            if (usuarioValido!=null) {
                                val sharedPref = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
                                with(sharedPref.edit()) {
                                    putInt("id", usuarioValido.id)
                                    putString("nombre", usuarioValido.nombre)
                                    putString("rol", usuarioValido.rol)
                                    putString("correo", usuarioValido.correo)
                                    apply()
                                }
                                val intent = Intent(this@MainActivity, VistaPrincipal::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this@MainActivity, "Correo o contraseña incorrectos.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Error al conectar al servidor.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("amkj","${e.message}" )
                    }
                }
            }


        }


        binding.tvOlvidaste.setOnClickListener {
            startActivity(Intent(this, RecuperarContrasena::class.java))
        }
    }
fun md5(input: String): String {
    val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
}


