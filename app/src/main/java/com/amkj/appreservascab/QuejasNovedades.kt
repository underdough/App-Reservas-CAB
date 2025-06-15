package com.amkj.appreservascab

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView

class QuejasNovedades : AppCompatActivity() {

    private val SELECT_IMAGE = 1
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quejas_novedades)

        imageView = findViewById(R.id.miImagen)

        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, SELECT_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK) {
            imageView.setImageURI(data?.data)
        }
    }
}