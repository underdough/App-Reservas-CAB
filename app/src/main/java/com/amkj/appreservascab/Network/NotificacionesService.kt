package com.amkj.appreservascab.Network

import com.amkj.appreservascab.Modelos.Notificacion
import com.amkj.appreservascab.Network.RetrofitClient.BASE_URL
import com.amkj.appreservascab.Network.RetrofitClient.gson
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface NotificacionesService {

    private val BASE_URL: String
        get() = "http://192.168.1.5:80/phpGestionReservas/"

    private val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @GET("notificaciones.php/{userId}")
    suspend fun obtenerNotificaciones(
        @Path("userId") userId: Int
    ): Response<ApiResponse<List<Notificacion>>>

    @PUT("notificaciones.php/marcar-leida/{notificacionId}")
    suspend fun marcarComoLeida(
        @Path("notificacionId") notificacionId: Int
    ): Response<ApiResponse<String>>
}