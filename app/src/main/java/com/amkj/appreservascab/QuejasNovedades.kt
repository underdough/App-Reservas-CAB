package com.amkj.appreservascab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amkj.appreservascab.databinding.ActivityQuejasNovedadesBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

class QuejasNovedades : AppCompatActivity() {

    private lateinit var binding: ActivityQuejasNovedadesBinding
    private val SELECT_IMAGE = 1
    private var imageUri: Uri? = null
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuejasNovedadesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainlyQuejasNovedades)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener nombre del usuario desde SharedPreferences
        val sharedPref = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
        val nombreUsuario = sharedPref.getString("nombre", "Usuario no identificado")

// Mostrar el nombre en el TextView correspondiente
        binding.tvNombre.text = nombreUsuario


        // Selección de imagen
        binding.miImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, SELECT_IMAGE)
        }

        // También desde el botón
        binding.botonSeleccionar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, SELECT_IMAGE)
        }

        // Enviar queja
        binding.btnGuardar.setOnClickListener {
            enviarQuejaAlServidor()
        }

        // Cancelar (opcional)
        binding.btnCancelar.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK) {
            imageUri = data?.data
            binding.miImagen.setImageURI(imageUri)
        }
    }

    private fun enviarQuejaAlServidor() {
        val descripcion = binding.etDescripcion.text.toString().trim()

        if (descripcion.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Leer imagen desde URI
        val imageBytes = contentResolver.openInputStream(imageUri!!)?.readBytes()
        if (imageBytes == null) {
            Toast.makeText(this, "No se pudo leer la imagen", Toast.LENGTH_SHORT).show()
            return
        }

        val imageBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageBytes)
        val imagePart = MultipartBody.Part.createFormData("imagen", "evidencia.jpg", imageBody)

        val descripcionBody = RequestBody.create("text/plain".toMediaTypeOrNull(), descripcion)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("descripcion", descripcion)
            .addFormDataPart("imagen", "evidencia.jpg", imageBody)
            .build()

        val request = Request.Builder()
            .url("http://192.168.1.23/phpGestionReservas/guardarQueja.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@QuejasNovedades, "Error al enviar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@QuejasNovedades, "Queja enviada correctamente", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@QuejasNovedades, "Error del servidor", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
