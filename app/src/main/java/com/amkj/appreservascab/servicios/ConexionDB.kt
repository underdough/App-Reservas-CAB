package com.amkj.appreservascab.servicios

import com.amkj.appreservascab.Modelos.ModeloUsuarios
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ConexionDB {
    companion object {
        const val URL = "http://192.168.1.23:80/phpGestionReservas/"
    }
    @POST("consultaUsuario.php")
    suspend fun consultaUsuario(): Response<List<ModeloUsuarios>>

    @POST("insertarUsuario.php")
    suspend fun insertarUsusario(@Body mReservas: ModeloUsuarios): retrofit2.Response<ModeloUsuarios>

    @PUT("modificarUsuario.php")
    suspend fun modificarUsuario(@Body mReservas: ModeloUsuarios): retrofit2.Response<ModeloUsuarios>

    @DELETE("eliminarUsuario.php")
    suspend fun eliminarUsuario(@Query("id") id: Int): retrofit2.Response<Any>

}