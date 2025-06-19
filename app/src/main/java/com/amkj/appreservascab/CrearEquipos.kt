package com.amkj.appreservascab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amkj.appreservascab.databinding.ActivityCrearEquiposBinding
import okhttp3.OkHttpClient

class CrearEquipos : AppCompatActivity() {

    private lateinit var binding: ActivityCrearEquiposBinding
    private val SELECT_IMAGE = 1
    private var imageUri: Uri? = null
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearEquiposBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainlyCrearEquipos)) { v, insets ->
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



        binding.btnGuardar.setOnClickListener {
            TODO()
        }


        binding.ibVolver.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK) {
            imageUri = data?.data
            binding.miImagen.setImageURI(imageUri)
        }
    }
}