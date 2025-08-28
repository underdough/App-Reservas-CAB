package com.amkj.appreservascab.servicios

import com.amkj.appreservascab.Modelos.ModeloQueja
import com.amkj.appreservascab.Modelos.ActualizarDatosUsario
import com.amkj.appreservascab.Modelos.BackendMsg
import com.amkj.appreservascab.Modelos.DisponibilidadEquipoDiaResponse
import com.amkj.appreservascab.Modelos.DisponibilidadRangoAmbienteResponse
import com.amkj.appreservascab.Modelos.DisponibilidadRangoEquipoResponse
import com.amkj.appreservascab.Modelos.DisponibilidadRespuesta
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
import com.amkj.appreservascab.Modelos.QuejasQuery
import com.amkj.appreservascab.Modelos.RegistrarQuejaRequest
import com.amkj.appreservascab.Modelos.ReportResponse
import com.amkj.appreservascab.Modelos.ReservaEquipoRequest
import com.amkj.appreservascab.Modelos.ReservasQuery
import com.amkj.appreservascab.Modelos.RespuestaActualizarUsuario
import com.amkj.appreservascab.Modelos.RespuestaCodigo
import com.amkj.appreservascab.Modelos.RespuestaContraNue
import com.amkj.appreservascab.Modelos.RespuestaInsertarAmbiente
import com.amkj.appreservascab.Modelos.RespuestaInsertarEquipo
import com.amkj.appreservascab.Modelos.RespuestaVerificacion
import com.amkj.appreservascab.Modelos.SolicitudDisponibilidadRequest
import com.amkj.appreservascab.Modelos.ValidacionAmbienteResponse
import com.amkj.appreservascab.Modelos.ValidacionEquipoRequest
import com.amkj.appreservascab.Modelos.ValidacionEquipoResponse
import com.amkj.appreservascab.models.DisponibilidadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ConexionDB {
    companion object {
        const val URL = "https://emma-essential-mae-singh.trycloudflare.com/phpGestionReservas/"
    }

    @GET("consultaUsuario.php")
    suspend fun consultaUsuario(): Response<List<ModeloUsuarios>>

    @POST("insertarUsuario.php")
    suspend fun insertarUsusario(@Body mReservas: ModeloUsuarios): Response<ModeloUsuarios>

    @PUT("modificarUsuario.php")
    suspend fun modificarUsuario(@Body mReservas: ModeloUsuarios): Response<ModeloUsuarios>

    @POST("actualizar_usuario.php")
    suspend fun actualizarUsuario(@Body req: ActualizarDatosUsario): Response<RespuestaActualizarUsuario>

    @GET("obtener_usuario.php")
    suspend fun obtenerUsuario(@Body body: Int): Response<RespuestaActualizarUsuario>

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

    @Headers("Content-Type: application/json")
    @POST("obtenerReservas.php")
    fun obtenerReservas(@Body body: ReservasQuery): Call<List<ModeloReserva>>

    @GET("disponibilidadAmbienteRango.php")
    fun disponibilidadAmbienteRango(
        @Query("ambiente_id") ambienteId: Int,
        @Query("inicio") inicio: String,
        @Query("fin") fin: String
    ): Call<DisponibilidadRangoAmbienteResponse>

    @POST("validarDisponibilidadAmbiente.php")
    fun validarDisponibilidadAmbiente(
        @Body body: com.amkj.appreservascab.Modelos.SolicitudDisponibilidadRequest
    ): retrofit2.Call<com.amkj.appreservascab.Modelos.ValidacionAmbienteResponse>  // <-- antes era Map<String, Boolean>

    @GET("disponibilidadAmbienteDia.php")
    fun disponibilidadAmbienteDia(
        @Query("ambiente_id") ambienteId: Int,
        @Query("fecha") fecha: String // YYYY-MM-DD
    ): Call<DisponibilidadResponse>

    // Rango (el que ya tienes)
    @GET("disponibilidadAmbiente.php")
    fun disponibilidadAmbiente(
        @Query("ambiente_id") ambienteId: Int,
        @Query("desde") desde: String,
        @Query("hasta") hasta: String
    ): Call<DisponibilidadResponse>

    @POST("guardarReservaEquipo.php")
    fun guardarReservaEquipo(@Body reserva: ReservaEquipoRequest): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("obtenerReservaEquipo.php")
    fun obtenerReservasEquipo(@Body body: ReservasQuery): Call<List<ModeloReservaEquipo>>

    @POST("validarDisponibilidadEquipo.php")
    fun validarDisponibilidadEquipo(
        @Body body: ValidacionEquipoRequest
    ): Call<ValidacionEquipoResponse>   // <---- antes tenías Map<String, Boolean>

//    @POST("validarDisponibilidadEquipo.php")
//    fun validarDisponibilidadEquipo(@Body datos: ValidacionEquipoRequest): Call<Map<String, Boolean>>

    @GET("disponibilidadEquipoDia.php")
    fun disponibilidadEquipoDia(
        @Query("elemento_id") elementoId: Int,
        @Query("fecha") fecha: String // "YYYY-MM-DD"
    ): Call<DisponibilidadEquipoDiaResponse>

    // ApiService.kt
    @GET("disponibilidadEquipoRango.php")
    fun disponibilidadEquipoRango(
        @Query("elemento_id") elementoId: Int,
        @Query("inicio") inicio: String, // "YYYY-MM-01"
        @Query("fin") fin: String        // fin de mes
    ): Call<DisponibilidadRangoEquipoResponse>


    @POST("crearUsuario.php")
    fun crearUsuario(@Body usuario: ModeloUsuarioCrear): Call<Map<String, Any>>

    @POST("obtener_notificaciones.php")
    suspend fun obtenerNotificaciones(@Body body: Map<String, Int>): List<Notificacion>

    @POST("marcar_notificacion_leida.php")
    suspend fun marcarNotificacionLeida(@Body datos: Map<String, Int>): Response<Unit>

    @POST("actualizar_estado_reserva.php")
    suspend fun actualizarEstadoReserva(
        @Body cuerpo: Map<String, @JvmSuppressWildcards Any>
    ): Map<String, String>

    @Headers("Content-Type: application/json")
    @POST("eliminar_reserva.php")
    fun eliminarReserva(
        @Body body: EliminarReservaRequest
    ): Call<Map<String, String>>

//    // Ambientes: rango obligatorio (si no envías, el backend asume por defecto)
//    @GET("disponibilidadAmbiente.php")
//    fun disponibilidadAmbiente(
//        @Query("ambiente_id") ambienteId: Int,
//        @Query("desde") desde: String,
//        @Query("hasta") hasta: String
//    ): Call<DisponibilidadResponse>


//    // Equipos: día único (tu caso actual)
//    @GET("disponibilidadEquipo.php")
//    fun disponibilidadEquipoDia(
//        @Query("elemento_id") elementoId: Int,
//        @Query("fecha") fecha: String
//    ): Call<DisponibilidadResponse>

//    // Equipos: rango (opcional si quieres pintar un mes)
//    @GET("disponibilidadEquipo.php")
//    fun disponibilidadEquipoRango(
//        @Query("elemento_id") elementoId: Int,
//        @Query("desde") desde: String,
//        @Query("hasta") hasta: String
//    ): Call<DisponibilidadResponse>

    // ========= Servicio de Reportes (para getTopReservas) =========
    interface ReporteService {
        @GET("reporte_top_reservas.php")
        suspend fun getTopReservas(
            @Query("inicio") inicio: String, // "YYYY-MM-DD"
            @Query("fin") fin: String,       // "YYYY-MM-DD"
            @Query("limit") limit: Int = 10
        ): Response<ReportResponse>
    }


    // ========= Servicio de Quejas =========

    // Debe existir exactamente este endpoint
    @Headers("Content-Type: application/json")
    @POST("registrar_queja.php")
    fun registrarQueja(@Body body: RegistrarQuejaRequest): Call<BackendMsg>

    @Multipart
    @POST("subirImagenQueja.php")
    fun subirImagenQueja(@Part imagen: okhttp3.MultipartBody.Part): Call<Map<String, String>>

//    @Headers("Content-Type: application/json")
//    @POST("registrarQueja.php")
//    fun registrarQueja(@Body body: RegistrarQuejaRequest): Call<Map<String, Any>>

    @Headers("Content-Type: application/json")
    @POST("obtener_quejas.php")
    fun obtenerQuejas(@Body body: QuejasQuery): Call<List<ModeloQueja>>

    // Para PDF, usar streaming
    @Streaming
    @Headers("Content-Type: application/json")
    @POST("exportarQuejasPdf.php")
    fun exportarQuejasPdf(@Body body: QuejasQuery): Call<okhttp3.ResponseBody>


    @GET("disponibilidad_recurso.php")
    suspend fun getDisponibilidad(
        @Query("tipo") tipo: String,           // "ambiente" | "equipo"
        @Query("recurso_id") recursoId: Int,
        @Query("inicio") inicio: String,       // "YYYY-MM-DD"
        @Query("fin") fin: String              // "YYYY-MM-DD"
    ): DisponibilidadRespuesta


}
