package com.amkj.appreservascab

import android.content.Intent
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.amkj.appreservascab.Adapters.AdapterAmbientes
import com.amkj.appreservascab.Modelos.ModeloAmbientes
import com.amkj.appreservascab.databinding.ActivityVistaPrincipalBinding

class VistaPrincipal : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityVistaPrincipalBinding
    private lateinit var adaptador: AdapterAmbientes
    private lateinit var listaOriginal: ArrayList<ModeloAmbientes>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Usar el binding para inflar la vista
        binding = ActivityVistaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // CORRECCIÓN: Usar el ID correcto del NavigationView
        binding.VistaNavegacionxd.setNavigationItemSelectedListener(this)

        // Datos de ejemplo
        listaOriginal = arrayListOf(
            ModeloAmbientes("Auditorio"),
            ModeloAmbientes("Laboratorio"),
            ModeloAmbientes("Biblioteca"),
            ModeloAmbientes("Ambiente A1"),
            ModeloAmbientes("Ambiente B2")
        )

        adaptador = AdapterAmbientes(ArrayList(listaOriginal))
        binding.rvLista.layoutManager = LinearLayoutManager(this)
        binding.rvLista.adapter = adaptador
        binding.rvLista.visibility = View.GONE

        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val texto = s.toString()
                if (texto.isNotEmpty()) {
                    binding.rvLista.visibility = View.VISIBLE
                    val listaFiltrada = ArrayList(listaOriginal.filter {
                        it.ambiente.contains(texto, ignoreCase = true)
                    })
                    adaptador.filtrar(listaFiltrada)
                } else {
                    binding.rvLista.visibility = View.GONE

                }
            }
        })

        // Esta diablura abre el menu de opciones
        binding.ibMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }

        // Obtener referencia del layout del menú lateral
        val headerView = binding.VistaNavegacionxd.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)
        val tvUserSubtitle = headerView.findViewById<TextView>(R.id.tvUserSubtitle)

// Obtener nombre y rol del usuario desde SharedPreferences
        val sharedPref = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
        val nombre = sharedPref.getString("nombre", "Nombre Usuario")
        val rol = sharedPref.getString("rol", "Rol Desconocido")

// Mostrar en los TextView del menú lateral
        tvUserName.text = nombre
        tvUserSubtitle.text = rol



    }

    // redirecciones
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_reserva -> {
                try {
                    val intent = Intent(this, SolicitudReservas::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()

                }
                return true
            }

            R.id.nav_mi_reservas -> {
                try {
                    val intent = Intent(this, InstruMisReservas::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return true
            }

            R.id.nav_perfil -> {
                try {
                    val intent = Intent(this@VistaPrincipal, PerfilAprendizInstru::class.java)
                    startActivity(intent)
                } catch (e: Exception){
                    e.printStackTrace()
                }
            }

            R.id.nav_quejas -> {
                // hagan la actividad de quejas
                try {
                 val intent = Intent(this, QuejasNovedades::class.java)
                 startActivity(intent)
                } catch (e: Exception){
                    e.printStackTrace()
                }
                return true
            }

            R.id.nav_crear_equipo->{
                try {
                    val intent = Intent(this, CrearEquipos::class.java)
                    startActivity(intent)
                } catch (e: Exception){
                    e.printStackTrace()
                }
                return true
            }

            R.id.nav_cerrar_Sesion -> {
                val sharedPreferences = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                Toast.makeText(this, "Inicio de sesion cerrado con exito", Toast.LENGTH_SHORT)
                    .show()
                finish()

            }

            else -> return false
        }
        return false
    }



    // Manejar el botón de retroceso para cerrar el drawer si está abierto
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }
}