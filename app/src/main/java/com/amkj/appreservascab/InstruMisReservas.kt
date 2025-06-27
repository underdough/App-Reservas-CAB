package com.amkj.appreservascab

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.amkj.appreservascab.Adaptadores.AdapterReservas
import com.amkj.appreservascab.Modelos.ModeloReserva
import com.amkj.appreservascab.databinding.ActivityCambiarContrasenaBinding
import com.amkj.appreservascab.databinding.ActivityInstruMisReservasBinding
import com.amkj.appreservascab.servicios.RetrofitClient
//import okhttp3.Call
//import okhttp3.Callback
//import okhttp3.Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InstruMisReservas : AppCompatActivity() {

    private lateinit var binding: ActivityInstruMisReservasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInstruMisReservasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nombre = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE).getString("nombre", "")

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        if (!nombre.isNullOrEmpty()) {
            val call = RetrofitClient.instance.obtenerReservas(mapOf("solicitante" to nombre))
            call.enqueue(object : Callback<List<ModeloReserva>> {
                override fun onResponse(
                    call: Call<List<ModeloReserva>>,
                    response: Response<List<ModeloReserva>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val reservas = response.body()!!
                        binding.recyclerView.adapter = AdapterReservas(reservas)
                    } else {
                        Toast.makeText(this@InstruMisReservas, "Sin reservas encontradas", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<ModeloReserva>>, t: Throwable) {
                    Toast.makeText(this@InstruMisReservas, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        binding.ibVolver.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

}