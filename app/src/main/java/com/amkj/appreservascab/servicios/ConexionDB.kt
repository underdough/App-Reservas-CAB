package com.amkj.appreservascab.servicios


import com.amkj.appreservascab.Modelos.ModeloActualizarContrasena
import com.amkj.appreservascab.Modelos.ModeloCorreo
import com.amkj.appreservascab.Modelos.ModeloEquipos
import com.amkj.appreservascab.Modelos.ModeloUsuarios
import com.amkj.appreservascab.Modelos.ModeloVerificarToken
import com.amkj.appreservascab.Modelos.RespuestaCodigo
import com.amkj.appreservascab.Modelos.RespuestaContraNue
import com.amkj.appreservascab.Modelos.RespuestaInsertarEquipo
import com.amkj.appreservascab.Modelos.RespuestaVerificacion
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface ConexionDB {
    companion object {
        const val URL = "http://192.168.0.14:80/phpGestionReservas/"
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
    suspend fun actualizarContrasena(@Body datos: ModeloActualizarContrasena): Response<RespuestaContraNue>

    @Multipart
    @POST("insertarEquipo.php")
    suspend fun insertarEquipo(
        @Part("marca") marca: RequestBody,
        @Part("modelo") modelo: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part imagen: MultipartBody.Part?
    ): Response<RespuestaInsertarEquipo>

    @GET("obtenerEquiposDisponibles.php")
    suspend fun obtenerEquipos(): Response<List<ModeloEquipos>>

}
