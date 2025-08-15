package com.amkj.appreservascab

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Log.e
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.amkj.appreservascab.Adapters.UsuariosAdapter
import com.amkj.appreservascab.Modelos.ModeloUsuarios
import com.amkj.appreservascab.databinding.ActivityPerfilAprendizInstruBinding
import com.amkj.appreservascab.Usuarios.SesionUsuarioPrefs

class PerfilAprendizInstru : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilAprendizInstruBinding
    private lateinit var usuariosAdapter: UsuariosAdapter
    private lateinit var lista: MutableList<ModeloUsuarios>

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val actualizado = res.data?.getParcelableExtra<ModeloUsuarios>("usuario_actualizado")
            if (actualizado != null) {
                // Actualiza la lista y la UI inmediatamente
                usuariosAdapter.updateItem(actualizado)
                binding.tvNomUser.text = actualizado.nombre ?: binding.tvNomUser.text
                // Opcional: si tienes estos TextView en el layout, se actualizarán; si no existen, no pasa nada
                binding.root.findViewById<TextView>(R.id.tvInfoCorreo)?.text = actualizado.correo ?: ""
                binding.root.findViewById<TextView>(R.id.tvInfoTelefono)?.text    = actualizado.telefono ?: ""
                binding.root.findViewById<TextView>(R.id.tvInfoRol)?.text    = actualizado.rol ?: ""
                Toast.makeText(this, "Usuario actualizado", Toast.LENGTH_SHORT).show()
            } else  {
                Toast.makeText(this, "No pudo ser actualizado", Toast.LENGTH_SHORT).show()
//                Log.e("error","${mess}")
            }
            // Además, en onResume() repintamos desde SesionUsuarioPrefs (que EditarUsuario ya sincroniza)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilAprendizInstruBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainlyPerfilAprendizInstru)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // 1) Leer datos de sesión (fuente de verdad en la app)
        SesionUsuarioPrefs.iniciar(this)
        val usuarioSesion = ModeloUsuarios(
            id = SesionUsuarioPrefs.obtenerId(),
            nombre = SesionUsuarioPrefs.obtenerNombre(),
            correo = SesionUsuarioPrefs.obtenerCorreo(),
            telefono = SesionUsuarioPrefs.obtenerTel(),
            rol = SesionUsuarioPrefs.obtenerRol()
        )


        // 2) Lista y adapter
        lista = mutableListOf(usuarioSesion)
        usuariosAdapter = UsuariosAdapter(lista) { seleccionado ->
            val i = Intent(this, EditarUsuario::class.java).putExtra("usuario", seleccionado)
            editLauncher.launch(i)
        }

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(this@PerfilAprendizInstru)
            adapter = usuariosAdapter
            setHasFixedSize(true)
        }

        // 3) Pintar UI inicial
        binding.tvNomUser.text = usuarioSesion.nombre ?: "Nombre no disponible"
        binding.root.findViewById<TextView>(R.id.tvInfoCorreo)?.text = usuarioSesion.correo ?: ""
        binding.root.findViewById<TextView>(R.id.tvInfoTelefono)?.text    = usuarioSesion.telefono ?: ""
        binding.root.findViewById<TextView>(R.id.tvInfoRol)?.text    = usuarioSesion.rol ?: ""

        binding.ibVolver.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        // 4) Siempre que vuelves a la pantalla, repinta desde SesionUsuarioPrefs
        SesionUsuarioPrefs.iniciar(this)
        val nombre = SesionUsuarioPrefs.obtenerNombre()
        val correo = SesionUsuarioPrefs.obtenerCorreo()
        var tel    = SesionUsuarioPrefs.obtenerTel()
        val rol    = SesionUsuarioPrefs.obtenerRol()

        android.util.Log.d("PERFIL", "SESSION_UI -> nombre='$nombre', correo='$correo', telefono='$tel', rol='$rol'")

        if (tel.isBlank()) {
            val sp = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
            tel = sp.getString("telefono", "") ?: ""
        }

        binding.tvNomUser.text = if (nombre.isNotBlank()) nombre else binding.tvNomUser.text
        binding.root.findViewById<TextView>(R.id.tvInfoCorreo)?.text = correo
        binding.root.findViewById<TextView>(R.id.tvInfoTelefono)?.text  = tel
        binding.root.findViewById<TextView>(R.id.tvInfoRol)?.text  = rol

        // 5) Mantén el item del Recycler sincronizado con la sesión
        val u = ModeloUsuarios(
            id = SesionUsuarioPrefs.obtenerId(),
            nombre = nombre,
            correo = correo,
            telefono = tel,
            rol = rol
        )
        usuariosAdapter.updateItem(
            ModeloUsuarios(
                id = SesionUsuarioPrefs.obtenerId(),
                nombre = nombre,
                correo = correo,
                telefono = tel,
                rol = rol
            )
        )

    }
}
