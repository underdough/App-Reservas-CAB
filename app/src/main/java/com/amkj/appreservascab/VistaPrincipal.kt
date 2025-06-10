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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.amkj.appreservascab.Adapters.AdapterAmbientes
import com.amkj.appreservascab.Modelos.ModelAmbientes
import com.amkj.appreservascab.databinding.ActivityVistaPrincipalBinding

class VistaPrincipal : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityVistaPrincipalBinding
    private lateinit var adaptador: AdapterAmbientes
    private lateinit var listaOriginal: ArrayList<ModelAmbientes>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Usar el binding para inflar la vista
        binding = ActivityVistaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el listener del NavigationView
       // binding.navView.setNavigationItemSelectedListener(this)

        // Datos de ejemplo
        listaOriginal = arrayListOf(
            ModelAmbientes("Auditorio"),
            ModelAmbientes("Laboratorio"),
            ModelAmbientes("Biblioteca"),
            ModelAmbientes("Salón A"),
            ModelAmbientes("Salón B")
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
    }

    // redirecciones del menu lateral
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_reserva -> {

                val intent = Intent(this,  PerfilAprendizInstru::class.java)
                startActivity(intent)
            }

            R.id.nav_mi_reservas -> {

                val intent = Intent(this, ::class.java)
                startActivity(intent)
            }

            R.id.nav_quejas -> {

                val intent = Intent(this, QuejasActivity::class.java)
                startActivity(intent)
            }

            R.id.nav_perfil -> {

                val intent = Intent(this, PerfilActivity::class.java)
                startActivity(intent)
            }

            // falta la opcion de cerrar sesion
        }

        // Cerrar el drawer después de la selección
        binding.drawerLayout.closeDrawer(GravityCompat.END)
        return true
    }



    // Manejar el botón de retroceso para cerrar el drawer si está abierto
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }
}