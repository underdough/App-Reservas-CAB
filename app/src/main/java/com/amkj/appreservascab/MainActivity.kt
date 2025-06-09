package com.amkj.appreservascab

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amkj.appreservascab.Admin.AdminRegistrarUsuario
import com.amkj.appreservascab.databinding.ActivityMainBinding
import retrofit2.Retrofit


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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

            AdminRegistrarUsuario.iniciar(applicationContext)



        binding.ivVerContra.setOnClickListener {
            var contraVisible = false
            binding.ivVerContra.setOnClickListener {
                if (contraVisible) {
                    // Ocultar contraseña
                    binding.etContra.transformationMethod = null
                    binding.ivVerContra.setImageResource(R.drawable.ic_ojo_abierto)
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

//            if (correo.isEmpty() || contrasena.isEmpty()) {
//                Toast.makeText(this, "No se permiten campos vacíos.", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            val adminValido = AdminRegistrarUsuario.usuariosad.any {
//                it.correo == correo && it.contrasena == contrasena
//            }
//
//            if (adminValido) {
//                // prueba
//                val intent = Intent(this, VistaPrincipal::class.java)
//                intent.putExtra("correo", correo)
//                startActivity(intent)
//
//
//            } else {
//                Toast.makeText(this, "El usuario no está registrado, comuníquese con el administrador del sistema", Toast.LENGTH_SHORT).show()
//            }


            val intent = Intent(this, VistaPrincipal::class.java)
            startActivity(intent)

        }


        binding.tvOlvidaste.setOnClickListener {
            val intent = Intent(this, RecuperarContrasena::class.java)
            startActivity(intent)
        }
    }

//    fun getRetrofit(): Retrofit{
//        return Retrofit.Builder()
//            .baseUrl()
//    }

    }
