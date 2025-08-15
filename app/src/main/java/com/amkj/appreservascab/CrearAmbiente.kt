package com.amkj.appreservascab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amkj.appreservascab.databinding.ActivityCrearAmbienteBinding
import com.amkj.appreservascab.servicios.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class CrearAmbiente : AppCompatActivity() {

    private lateinit var binding: ActivityCrearAmbienteBinding
    private val SELECT_IMAGE = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🚫 Bloqueo por rol (candado en la Activity)
        val rol = (getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
            .getString("rol", "") ?: "").lowercase()

        val esAdmin = (rol == "admin" || rol == "administrador")
        if (!esAdmin) {
            Toast.makeText(this, "Acceso denegado: se requiere rol administrador.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding = ActivityCrearAmbienteBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainlyCrearAmbiente)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Selección de imagen
        binding.miImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, SELECT_IMAGE)
        }

        // Botón Guardar
        binding.btnIniciar.setOnClickListener {
            val nombreAmbiente = binding.etNombreAmbiente.text.toString().trim().uppercase()
            val descripcion = binding.etDescripcion.text.toString().trim()
            val imagen = imageUri?.let { convertirUriABase64(it) } ?: ""

            if (nombreAmbiente.isEmpty() || descripcion.isEmpty() || imagen.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos e imagen", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nombreAmbienteRB = nombreAmbiente.toRequestBody("text/plain".toMediaTypeOrNull())
            val descripcionRB = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())

            val inputStream = contentResolver.openInputStream(imageUri!!)
            val bytes = inputStream?.readBytes()
            val requestFile = bytes?.let { it.toRequestBody("imagenesAmbientes/*".toMediaTypeOrNull()) }
            val imagenPart = requestFile?.let { MultipartBody.Part.createFormData("imagen", "ambiente.jpg", it) }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.instance.insertarAmbiente(
                        nombreAmbiente = nombreAmbienteRB,
                        descripcion = descripcionRB,
                        imagen = imagenPart
                    )

                    if (!response.isSuccessful) {
                        val errorText = response.errorBody()?.string()
                        Log.e("amkj", "Error body: $errorText")
                    }
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            Toast.makeText(this@CrearAmbiente, "Ambiente guardado", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@CrearAmbiente, VistaPrincipal::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@CrearAmbiente, "Error al guardar", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CrearAmbiente, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("amkj","Error: ${e.message}")
                    }
                }
            }
        }

        // Volver
        binding.ibVolver.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    fun convertirUriABase64(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK) {
            imageUri = data?.data
            binding.miImagen.setImageURI(imageUri)
        }
    }
}