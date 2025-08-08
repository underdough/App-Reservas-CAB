package com.amkj.appreservascab

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amkj.appreservascab.Adaptadores.AdapterEquipos
import com.amkj.appreservascab.Adapters.AdapterAmbientes
import com.amkj.appreservascab.Modelos.ModeloAmbientes
import com.amkj.appreservascab.Modelos.ModeloEquipos
import com.amkj.appreservascab.databinding.ActivityVistaPrincipalBinding
import com.amkj.appreservascab.servicios.RetrofitClient
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class VistaPrincipal : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityVistaPrincipalBinding
    private var listaAmbientes: List<ModeloAmbientes> = listOf()
    private var listaEquipos: List<ModeloEquipos> = listOf()
    private lateinit var adapterAmbientes: AdapterAmbientes
    private lateinit var adapterEquipos: AdapterEquipos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVistaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.VistaNavegacionxd.setNavigationItemSelectedListener(this)

        // LayoutManagers
        binding.lyAmbientesPpal.recyclerAmbientes.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.lyAmbientesPpal.recyclerEquipos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Adapters iniciales vacíos
        adapterAmbientes = AdapterAmbientes(listaAmbientes) { ambiente ->
            val intent = Intent(this, DetalleAmbiente::class.java)
            intent.putExtra("ambiente", ambiente)
            startActivity(intent)
        }
        adapterEquipos = AdapterEquipos(listaEquipos) { equipo ->
            val intent = Intent(this, DetalleEquipo::class.java)
            intent.putExtra("equipo", equipo)
            startActivity(intent)
        }

        binding.lyAmbientesPpal.recyclerAmbientes.adapter = adapterAmbientes
        binding.lyAmbientesPpal.recyclerEquipos.adapter = adapterEquipos

        // Buscar mientras se escribe
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()

                adapterAmbientes.filtrar(query)
                adapterEquipos.filtrar(query)
            }
        })

        // Obtener ambientes
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.obtenerAmbiente()
                if (response.isSuccessful && response.body() != null) {
                    listaAmbientes = response.body()!!
                    adapterAmbientes.setListaCompleta(listaAmbientes)
                    adapterAmbientes.filtrar("") // inicializa con todo
                }
            } catch (e: Exception) {
                Toast.makeText(this@VistaPrincipal, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Obtener equipos
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.obtenerEquipos()
                if (response.isSuccessful && response.body() != null) {
                    listaEquipos = response.body()!!
                    adapterEquipos.setListaCompleta(listaEquipos)
                    adapterEquipos.filtrar("") // inicializa con todo
                }
            } catch (e: Exception) {
                Toast.makeText(this@VistaPrincipal, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.ibNotifcacion.setOnClickListener {
            val intent = Intent(this, ActividadNotificaciones::class.java)
            startActivity(intent)
        }

        binding.ibMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }

        val headerView = binding.VistaNavegacionxd.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)
        val tvUserSubtitle = headerView.findViewById<TextView>(R.id.tvUserSubtitle)

        val sharedPref = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
        tvUserName.text = sharedPref.getString("nombre", "Nombre Usuario")
        tvUserSubtitle.text = sharedPref.getString("rol", "Rol Desconocido")
    }

    override fun onResume() {
        super.onResume()
        verificarNotificacionesNoLeidas()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_mi_reservas -> startActivity(Intent(this, InstruMisReservas::class.java))
            R.id.nav_perfil -> startActivity(Intent(this, PerfilAprendizInstru::class.java))
            R.id.nav_quejas -> startActivity(Intent(this, QuejasNovedades::class.java))
            R.id.nav_crear_equipo -> startActivity(Intent(this, CrearEquipos::class.java))
            R.id.nav_crear_ambiente -> startActivity(Intent(this, CrearAmbiente::class.java))
            R.id.nav_crear_usuario -> startActivity(Intent(this, CrearUsuario::class.java))
            R.id.nav_cerrar_Sesion -> {
                val sharedPreferences = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
                sharedPreferences.edit().clear().apply()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                Toast.makeText(this, "Inicio de sesión cerrado con éxito", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    private fun verificarNotificacionesNoLeidas() {
        val sharedPref = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
        val usuarioId = sharedPref.getInt("id", -1)
        if (usuarioId == -1) return

        lifecycleScope.launch {
            try {
                val notificaciones = RetrofitClient.instance.obtenerNotificaciones(
                    mapOf("usuario_id" to usuarioId)
                )
                val noLeidas = notificaciones.count { it.leida == 0 }

                val contador = findViewById<TextView>(R.id.contadorNotificaciones)
                contador.text = if (noLeidas > 99) "99+" else noLeidas.toString()
                contador.visibility = if (noLeidas > 0) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Log.e("Notificaciones", "Error al obtener notificaciones", e)
            }
        }
    }
}
