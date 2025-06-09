
package com.amkj.appreservascab

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.amkj.appreservascab.Adapters.AdapterAmbientes
import com.amkj.appreservascab.Modelos.ModelAmbientes
import com.amkj.appreservascab.databinding.ActivityVistaPrincipalBinding

class VistaPrincipal : AppCompatActivity() {

    private lateinit var binding: ActivityVistaPrincipalBinding
    private lateinit var adaptador: AdapterAmbientes
    private lateinit var listaOriginal: ArrayList<ModelAmbientes>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Usar el binding para inflar la vista
        binding = ActivityVistaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
    }
}