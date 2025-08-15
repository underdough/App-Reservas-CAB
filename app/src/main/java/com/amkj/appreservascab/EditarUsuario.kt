package com.amkj.appreservascab

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.amkj.appreservascab.Modelos.ActualizarDatosUsario
import com.amkj.appreservascab.Modelos.ModeloUsuarios
import com.amkj.appreservascab.databinding.ActivityEditarUsuarioBinding
import com.amkj.appreservascab.servicios.RetrofitClient
import com.amkj.appreservascab.Usuarios.SesionUsuarioPrefs // ✅ importa esto
import kotlinx.coroutines.launch

class EditarUsuario : AppCompatActivity() {

    private lateinit var binding: ActivityEditarUsuarioBinding
    private var progress: ProgressBar? = null
    private var usuario: ModeloUsuarios? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = binding.root.findViewById(R.id.progress)

        // Recibir usuario (Parcelable)
        usuario = intent.parcelableCompat("usuario")
        // Prefill (aunque usuario sea null, dejamos campos vacíos)
        binding.etNombres.setText(usuario?.nombre.orEmpty())
        binding.etCorreo.setText(usuario?.correo.orEmpty())
        binding.etTelefono.setText(usuario?.telefono.orEmpty())

        binding.btnGuardar.setOnClickListener { guardarCambios() }
        binding.btnVolver?.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun guardarCambios() {
        // Fallback robusto del ID (intent -> prefs "id" -> prefs "id_usuario")
        val prefs = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
        val id = listOf(
            usuario?.id ?: -1,
            prefs.getInt("id", -1),
            prefs.getInt("id_usuario", -1)
        ).firstOrNull { it > 0 } ?: -1

        if (id <= 0) { toast("ID inválido"); return }

        val nombre = binding.etNombres.text.toString().trim()
        val correo = binding.etCorreo.text.toString().trim()
        val tel    = binding.etTelefono.text.toString().trim()

        if (nombre.isEmpty()) { binding.etNombres.error = "Requerido"; return }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.etCorreo.error = "Correo no válido"; return
        }

        val req = ActualizarDatosUsario(
            id = id,
            nombre = nombre,
            correo = correo,
            telefono = tel
        )

        setLoading(true)
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.actualizarUsuario(req)
                val body = resp.body()
                setLoading(false)

                if (resp.isSuccessful && body?.ok == true && body.usuario != null) {
                    val actualizado = body.usuario!!

                    android.util.Log.d(
                        "EDITAR_USUARIO",
                        "Servidor -> nombre=${body?.usuario?.nombre}, correo=${body?.usuario?.correo}, telefono=${body?.usuario?.telefono}"
                    )

                    val telOk = actualizado.telefono ?: tel
                    // Persistir bajo ambas claves para evitar inconsistencias
                    val ed = prefs.edit()
                    ed.putInt("id", actualizado.id ?: id)
                    ed.putInt("id_usuario", actualizado.id ?: id)
                    ed.putString("nombre", actualizado.nombre)
                    ed.putString("correo", actualizado.correo)
                    ed.putString("telefono", telOk) // <- usa telOk en vez de actualizado.telefono
                    // No tocamos 'rol' aquí
                    ed.apply()

                    SesionUsuarioPrefs.iniciar(this@EditarUsuario)
                    SesionUsuarioPrefs.updatePerfil(
                        nombre = actualizado.nombre,
                        correo = actualizado.correo,
                        telefono = telOk,   // <- usa telOk
                        rol = null // no cambiamos rol aquí
                    )

                    // Devolver al caller
                    val data = Intent().putExtra("usuario_actualizado", actualizado.copy(telefono = telOk))
                    setResult(Activity.RESULT_OK, data)
                    finish()
                } else {
                    toast(body?.mensaje ?: "No se pudo actualizar")
                }
            } catch (e: Exception) {
                setLoading(false)
                toast("Fallo de red: ${e.message}")
            }
        }
    }

    private fun setLoading(on: Boolean) {
        binding.btnGuardar.isEnabled = !on
        progress?.visibility = if (on) View.VISIBLE else View.GONE
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

// Compat para parcelables en API 33+
inline fun <reified T : android.os.Parcelable> android.content.Intent.parcelableCompat(key: String): T? {
    return if (Build.VERSION.SDK_INT >= 33) getParcelableExtra(key, T::class.java)
    else @Suppress("DEPRECATION") getParcelableExtra(key)
}
