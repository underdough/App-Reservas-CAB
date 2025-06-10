package com.amkj.appreservascab.servicios



import com.amkj.appreservascab.Modelos.ModeloUsuarios
import retrofit2.http.GET

interface ConexionDB {
     companion object {
         val url: String = "http://192.168.20.42:80/phpGestionReservas"
     }
    @GET("/consultaUsuario")
    suspend fun consultaUsuario(): retrofit2.Response<List<ModeloUsuarios>>

}