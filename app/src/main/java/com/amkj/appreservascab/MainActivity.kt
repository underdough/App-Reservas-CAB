package com.amkj.appreservascab

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var contraVisible = false

    // TAGs para logs
    private val TAG_LOGIN = "LOGIN"
    private val TAG_NET = "NETWORK"

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

            // Log de entrada
            Log.d(TAG_LOGIN, "Input -> correo='$correo' | passLength=${contrasena.length}")

            if (correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "No se permiten campos vacíos.", Toast.LENGTH_SHORT).show()
                Log.w(TAG_LOGIN, "Campos vacíos: correo o contraseña")
                return@setOnClickListener
            }

            // Verificar conectividad ANTES de llamar a Retrofit
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "Sin conexión a Internet.", Toast.LENGTH_SHORT).show()
                Log.e(TAG_NET, "Conectividad ausente. Abortando login.")
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Interceptor de logs HTTP
                    val logging = HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }

                    // Timeouts sensatos para detectar errores de red rápido
                    val client = OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .writeTimeout(20, TimeUnit.SECONDS)
                        .build()

                    val gson = GsonBuilder()
                        .setLenient()
                        .create()

                    Log.d(TAG_NET, "Usando baseUrl='${ConexionDB.URL}'")

                    val retrofit = Retrofit.Builder()
                        .baseUrl(ConexionDB.URL)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build()

                    val service = retrofit.create(ConexionDB::class.java)

                    Log.d(TAG_LOGIN, "Invocando consultaUsuario() ...")
                    val response = service.consultaUsuario()

                    if (response.isSuccessful) {
                        val usuarios = response.body()
                        Log.d(TAG_LOGIN, "Respuesta OK. Usuarios recibidos=${usuarios?.size ?: 0}")

                        withContext(Dispatchers.Main) {
                            val usuarioValido = usuarios?.find {
                                // Permite comparar contra en MD5 o en texto plano
                                it.correo == correo && (
                                        it.contrasena == md5(contrasena) || it.contrasena == contrasena
                                        )
                            }

                            if (usuarioValido != null) {
                                Log.i(TAG_LOGIN, "Login exitoso. id=${usuarioValido.id}, rol=${usuarioValido.rol}")
                                val sharedPref = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
                                with(sharedPref.edit()) {
                                    putInt("id", usuarioValido.id)
                                    putString("nombre", usuarioValido.nombre)
                                    putString("rol", usuarioValido.rol)
                                    putString("correo", usuarioValido.correo)
                                    usuarioValido.telefono?.let { putString("telefono", it) }
                                    apply()
                                }
                                startActivity(Intent(this@MainActivity, VistaPrincipal::class.java))
                            } else {
                                Log.w(TAG_LOGIN, "Credenciales inválidas para correo='$correo'")
                                Toast.makeText(
                                    this@MainActivity,
                                    "Correo o contraseña incorrectos.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        // Log detallado de error de servidor
                        val code = response.code()
                        val rawError = response.errorBody()?.string()
                        Log.e(TAG_LOGIN, "HTTP $code. errorBody=$rawError")

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MainActivity,
                                "Error del servidor ($code).",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG_NET, "Fallo de red: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Error de red: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        binding.tvOlvidaste.setOnClickListener {
            startActivity(Intent(this, RecuperarContrasena::class.java))
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        val available = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        Log.d(TAG_NET, "Conectividad disponible=$available")
        return available
    }

    fun md5(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
