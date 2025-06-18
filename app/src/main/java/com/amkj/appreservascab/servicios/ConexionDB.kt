package com.amkj.appreservascab.servicios


import com.amkj.appreservascab.Modelos.ModeloCorreo
import com.amkj.appreservascab.Modelos.ModeloUsuarios
import com.amkj.appreservascab.Modelos.ModeloVerificarToken
import com.amkj.appreservascab.Modelos.RespuestaCodigo
import com.amkj.appreservascab.Modelos.RespuestaVerificacion
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ConexionDB {
    companion object {
        const val URL = "http://192.168.0.7:80/phpGestionReservas/"
    }

    @POST("consultaUsuario.php")
    suspend fun consultaUsuario(): Response<List<ModeloUsuarios>>

    @POST("insertarUsuario.php")
    suspend fun insertarUsusario(@Body mReservas: ModeloUsuarios): Response<ModeloUsuarios>

    @PUT("modificarUsuario.php")
    suspend fun modificarUsuario(@Body mReservas: ModeloUsuarios): Response<ModeloUsuarios>

    @POST("enviarToken.php")
    suspend fun enviarToken(@Body correo: ModeloCorreo): Response<RespuestaCodigo>

    @POST("verificarToken.php")
    suspend fun verificarToken(@Body datos: ModeloVerificarToken): Response<RespuestaVerificacion>

    @PUT("actualizarContrasena.php")
    suspend fun actualizarContrasena(@Body usuario: ModeloUsuarios): Response<RespuestaCodigo>
}
