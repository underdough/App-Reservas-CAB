package com.amkj.appreservascab.servicios

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val gson = GsonBuilder()
        .setLenient()  // Permite parsear JSON malformado o menos estricto
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ConexionDB.URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Servicio "general" que ya usas
    val servicioApi: ConexionDB = retrofit.create(ConexionDB::class.java)

    // Alias que ya usas en otras partes
    val instance: ConexionDB by lazy {
        retrofit.create(ConexionDB::class.java)
    }

    // 🔹 Nuevo: servicio de reportes (para getTopReservas)
    val reporteService: ConexionDB.ReporteService by lazy {
        retrofit.create(ConexionDB.ReporteService::class.java)
    }
}
