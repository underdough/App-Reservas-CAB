package com.amkj.appreservascab.servicios

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val gson = GsonBuilder()
        .setLenient()  // Permite parsear JSON malformado o menos estricto
        .create()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ConexionDB.URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val instance: ConexionDB by lazy {
        retrofit.create(ConexionDB::class.java)
    }
}
