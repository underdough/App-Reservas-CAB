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
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amkj.appreservascab.Adaptadores.AdapterEquipos
import com.amkj.appreservascab.Adapters.*
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVistaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.VistaNavegacionxd.setNavigationItemSelectedListener(this)

        // Configurar búsqueda
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()

                val ambientesFiltrados = listaAmbientes.filter {
                    it.nombre.lowercase().contains(query)
                }

                val equiposFiltrados = listaEquipos.filter {
                    it.marca.lowercase().contains(query) || it.modelo.lowercase().contains(query)
                }

                val listaBusqueda = ambientesFiltrados.map { ItemBusqueda.Ambiente(it) } +
                        equiposFiltrados.map { ItemBusqueda.Equipo(it) }

                val adaptadorBusqueda = AdapterBusqueda(listaBusqueda) { recurso ->
                    when (recurso) {
                        is ItemBusqueda.Ambiente -> {
                            val intent = Intent(this@VistaPrincipal, DetalleAmbiente::class.java)
                            intent.putExtra("ambiente", recurso.data)
                            startActivity(intent)
                        }
                        is ItemBusqueda.Equipo -> {
                            val intent = Intent(this@VistaPrincipal, DetalleEquipo::class.java)
                            intent.putExtra("equipo", recurso.data)
                            startActivity(intent)
                        }
                    }
                }

                binding.rvLista.adapter = adaptadorBusqueda
                binding.rvLista.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            }
        })

        // RecyclerView horizontales
        binding.lyAmbientesPpal.recyclerAmbientes.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.lyAmbientesPpal.recyclerEquipos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.obtenerAmbiente()
                if (response.isSuccessful && response.body() != null) {
                    listaAmbientes = response.body()!! //  Guardamos para la búsqueda
                    binding.lyAmbientesPpal.recyclerAmbientes.adapter =
                        AdapterAmbientes(listaAmbientes) { ambiente ->
                            val intent = Intent(this@VistaPrincipal, DetalleAmbiente::class.java)
                            intent.putExtra("ambiente", ambiente)
                            startActivity(intent)
                        }
                }
            } catch (e: Exception) {
                Toast.makeText(this@VistaPrincipal, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.obtenerEquipos()
                if (response.isSuccessful && response.body() != null) {
                    listaEquipos = response.body()!! // Guardamos para la búsqueda
                    binding.lyAmbientesPpal.recyclerEquipos.adapter =
                        AdapterEquipos(listaEquipos) { equipo ->
                            val intent = Intent(this@VistaPrincipal, DetalleEquipo::class.java)
                            intent.putExtra("equipo", equipo)
                            startActivity(intent)
                        }
                }
            } catch (e: Exception) {
                Toast.makeText(this@VistaPrincipal, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }


        binding.ibNotifcacion.setOnClickListener {
            try {
                val intent = Intent(this, ActividadNotificaciones::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al abrir notificaciones", Toast.LENGTH_SHORT).show()
            }
        }


        // Menú lateral
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
}
