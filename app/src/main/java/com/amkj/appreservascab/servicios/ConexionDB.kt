package com.amkj.appreservascab.servicios


import com.amkj.appreservascab.Modelos.EliminarReservaRequest
import com.amkj.appreservascab.Modelos.ModeloActualizarContrasena
import com.amkj.appreservascab.Modelos.ModeloAmbientes
import com.amkj.appreservascab.Modelos.ModeloCorreo
import com.amkj.appreservascab.Modelos.ModeloEquipos
import com.amkj.appreservascab.Modelos.ModeloReserva
import com.amkj.appreservascab.Modelos.ModeloReservaEquipo
import com.amkj.appreservascab.Modelos.ModeloUsuarioCrear
import com.amkj.appreservascab.Modelos.ModeloUsuarios
import com.amkj.appreservascab.Modelos.ModeloVerificarToken
import com.amkj.appreservascab.Modelos.Notificacion
import com.amkj.appreservascab.Modelos.ReservaEquipoRequest
import com.amkj.appreservascab.Modelos.RespuestaCodigo
import com.amkj.appreservascab.Modelos.RespuestaContraNue
import com.amkj.appreservascab.Modelos.RespuestaInsertarAmbiente
import com.amkj.appreservascab.Modelos.RespuestaInsertarEquipo
import com.amkj.appreservascab.Modelos.RespuestaVerificacion
import com.amkj.appreservascab.Modelos.SolicitudDisponibilidadRequest
import com.amkj.appreservascab.Modelos.ValidacionEquipoRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Query


interface ConexionDB {
    companion object {
        const val URL = "https://cf7e2811433e.ngrok-free.app/phpGestionReservas/"
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

    @Multipart
    @POST("insertarAmbiente.php")
    suspend fun insertarAmbiente(
        @Part("nombreAmbiente") nombreAmbiente: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part imagen: MultipartBody.Part?
    ): Response<RespuestaInsertarAmbiente>

    @GET("obtenerAmbientesDisponibles.php")
    suspend fun obtenerAmbiente(): Response<List<ModeloAmbientes>>

    @POST("guardarReserva.php")
    fun guardarReserva(@Body reserva: ModeloReserva): Call<ResponseBody>

    @POST("obtenerReservas.php")
    fun obtenerReservas(@Body usuario: Map<String, Int>): Call<List<ModeloReserva>>

    @POST("validarDisponibilidadAmbiente.php")
    fun validarDisponibilidadAmbiente(@Body datos :SolicitudDisponibilidadRequest): Call<Map<String, Boolean>>


    @POST("guardarReservaEquipo.php")
    fun guardarReservaEquipo(@Body reserva: ReservaEquipoRequest): Call<ResponseBody>

    @POST("obtenerReservaEquipo.php")
    fun obtenerReservasEquipo(@Body body: Map<String, Int>): Call<List<ModeloReservaEquipo>>

    @POST("validarDisponibilidadEquipo.php")
    fun validarDisponibilidadEquipo(@Body datos: ValidacionEquipoRequest): Call<Map<String, Boolean>>

    @POST("crearUsuario.php")
    fun crearUsuario(@Body usuario: ModeloUsuarioCrear): Call<Map<String, Any>>

    @POST("obtener_notificaciones.php")
    suspend fun obtenerNotificaciones(@Body body: Map<String, Int>): List<Notificacion>

    @POST("marcar_notificacion_leida.php")
    suspend fun marcarNotificacionLeida(@Body datos: Map<String, Int>): retrofit2.Response<Unit>

    @POST("actualizar_estado_reserva.php")
    suspend fun actualizarEstadoReserva(
        @Body cuerpo: Map<String, @JvmSuppressWildcards Any>
    ): Map<String, String>

    @POST("eliminar_reserva.php")
    fun eliminarReserva(@Body body: EliminarReservaRequest): Call<Map<String, String>>










}
