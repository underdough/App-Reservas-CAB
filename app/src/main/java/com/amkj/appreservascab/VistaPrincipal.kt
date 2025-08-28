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

    // Header del drawer
    private lateinit var tvUserName: TextView
    private lateinit var tvUserSubtitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVistaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.VistaNavegacionxd.setNavigationItemSelectedListener(this)

        // Header del drawer
        val headerView = binding.VistaNavegacionxd.getHeaderView(0)
        tvUserName = headerView.findViewById(R.id.tvUserName)
        tvUserSubtitle = headerView.findViewById(R.id.tvUserSubtitle)

        refreshDrawerHeader()
        aplicarReglasDeMenuPorRol() // ⬅️ Oculta/mostrar según rol (incluye Crear Usuario)

        // Layouts horizontales
        binding.lyAmbientesPpal.recyclerAmbientes.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.lyAmbientesPpal.recyclerEquipos.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Adapters
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

        // Búsqueda en vivo
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s.toString().trim().lowercase()
                adapterAmbientes.filtrar(q)
                adapterEquipos.filtrar(q)
            }
        })

        // Cargar ambientes
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.obtenerAmbiente()
                if (resp.isSuccessful && resp.body() != null) {
                    listaAmbientes = resp.body()!!
                    adapterAmbientes.setListaCompleta(listaAmbientes)
                    adapterAmbientes.filtrar("")
                }
            } catch (e: Exception) {
                Toast.makeText(this@VistaPrincipal, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Cargar equipos
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.instance.obtenerEquipos()
                if (resp.isSuccessful && resp.body() != null) {
                    listaEquipos = resp.body()!!
                    adapterEquipos.setListaCompleta(listaEquipos)
                    adapterEquipos.filtrar("")
                }
            } catch (e: Exception) {
                Toast.makeText(this@VistaPrincipal, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Notificaciones
        binding.ibNotifcacion.setOnClickListener {
            startActivity(Intent(this, ActividadNotificaciones::class.java))
        }

        // Abrir drawer (lado derecho)
        binding.ibMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshDrawerHeader()
        aplicarReglasDeMenuPorRol() // ⬅️ Revalida al volver (por si cambió el usuario)
        verificarNotificacionesNoLeidas()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val esAdmin = esUsuarioAdmin()

        when (item.itemId) {
            R.id.nav_mi_reservas -> startActivity(Intent(this, InstruMisReservas::class.java))
            R.id.nav_perfil      -> startActivity(Intent(this, PerfilAprendizInstru::class.java))
            R.id.nav_quejas      -> startActivity(Intent(this, MisQuejasActivity::class.java))

            R.id.nav_crear_equipo -> {
                if (!esAdmin) {
                    Toast.makeText(this, "No tienes permisos para crear equipos.", Toast.LENGTH_SHORT).show()
                    binding.drawerLayout.closeDrawer(GravityCompat.END)
                    return true
                }
                startActivity(Intent(this, CrearEquipos::class.java))
            }
            R.id.nav_crear_ambiente -> {
                if (!esAdmin) {
                    Toast.makeText(this, "No tienes permisos para crear ambientes.", Toast.LENGTH_SHORT).show()
                    binding.drawerLayout.closeDrawer(GravityCompat.END)
                    return true
                }
                startActivity(Intent(this, CrearAmbiente::class.java))
            }
            R.id.nav_crear_usuario -> { // ⬅️ BLOQUEO NUEVO
                if (!esAdmin) {
                    Toast.makeText(this, "No tienes permisos para crear usuarios.", Toast.LENGTH_SHORT).show()
                    binding.drawerLayout.closeDrawer(GravityCompat.END)
                    return true
                }
                startActivity(Intent(this, CrearUsuario::class.java))
            }
            R.id.nav_informes -> {
                // Si quieres que solo admin vea informes, descomenta:
                // if (!esAdmin) {
                //     Toast.makeText(this, "No tienes permisos para ver Informes.", Toast.LENGTH_SHORT).show()
                //     binding.drawerLayout.closeDrawer(GravityCompat.END)
                //     return true
                // }
                startActivity(Intent(this, InformesActivity::class.java))
            }
            R.id.nav_cerrar_Sesion -> {
                val shared = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
                shared.edit().clear().apply()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                Toast.makeText(this, "Inicio de sesión cerrado con éxito", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.END)
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

    /** Header del drawer */
    private fun refreshDrawerHeader() {
        val sharedPref = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
        val nombre = sharedPref.getString("nombre", "Nombre Usuario")
        val rol = sharedPref.getString("rol", "Rol Desconocido")
        tvUserName.text = nombre ?: "Nombre Usuario"
        tvUserSubtitle.text = rol ?: "Rol Desconocido"
    }

    /** Oculta/ muestra menú según rol (incluye Crear Usuario) */
    private fun aplicarReglasDeMenuPorRol() {
        val menu = binding.VistaNavegacionxd.menu
        val esAdmin = esUsuarioAdmin()

        menu.findItem(R.id.nav_crear_equipo)?.isVisible = esAdmin
        menu.findItem(R.id.nav_crear_ambiente)?.isVisible = esAdmin
        menu.findItem(R.id.nav_crear_usuario)?.isVisible = esAdmin // ⬅️ NUEVO
    }

    /** ¿Usuario actual es admin? */
    private fun esUsuarioAdmin(): Boolean {
        val sharedPref = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
        val rol = (sharedPref.getString("rol", "") ?: "").lowercase()
        return rol == "admin" || rol == "administrador"
    }
}
