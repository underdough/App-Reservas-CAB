package com.amkj.appreservascab

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amkj.appreservascab.Modelos.ModeloUsuarioCrear
import com.amkj.appreservascab.databinding.ActivityCrearUsuarioBinding
import com.amkj.appreservascab.servicios.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CrearUsuario : AppCompatActivity() {

    private lateinit var binding: ActivityCrearUsuarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCrearUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainlyCrearUsuario)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ------ INICIALIZACIÓN DEL SPINNER DE ROLES ------
        val roles = arrayOf("aprendiz", "instructor", "admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spRol.adapter = adapter
        // --------------------------------------------------

        binding.ibVolver.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        var contraVisible = false
        binding.ivVerContra.setOnClickListener {
            contraVisible = !contraVisible
            if (contraVisible) {
                binding.etContrasena.transformationMethod = null
                binding.ivVerContra.setImageResource(R.drawable.ic_ojo_abierto)
            } else {
                binding.etContrasena.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.ivVerContra.setImageResource(R.drawable.ic_ojo_cerrado)
            }
            binding.etContrasena.setSelection(binding.etContrasena.text.length)
        }

        binding.btnCrearUsuario.setOnClickListener {
            val numDocumento = binding.etNumDocumento.text.toString().trim()
            val nombre = binding.etNombreUsuario.text.toString().trim()
            val telefono = binding.etTelefono.text.toString().trim()
            val correo = binding.etCorreo.text.toString().trim()
            val contrasena = binding.etContrasena.text.toString().trim()
            val rol = binding.spRol.selectedItem.toString() // <-- Aquí obtienes el rol seleccionado


            if (contrasena.length < 6) {
                Toast.makeText(this, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            // Validaciones básicas
            if (numDocumento.isEmpty() || nombre.isEmpty() || telefono.isEmpty() || correo.isEmpty() || contrasena.isEmpty() || rol.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nuevoUsuario = ModeloUsuarioCrear(
                num_documento = numDocumento,
                nombre = nombre,
                telefono = telefono,
                correo = correo,
                contrasena = contrasena,
                rol = rol
            )

            RetrofitClient.servicioApi.crearUsuario(nuevoUsuario).enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    if (response.isSuccessful && response.body()?.get("success") == true) {
                        Toast.makeText(this@CrearUsuario, "Usuario creado correctamente", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val mensaje = response.body()?.get("error") ?: "Error al crear usuario"
                        Toast.makeText(this@CrearUsuario, mensaje.toString(), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Toast.makeText(this@CrearUsuario, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
