package com.amkj.appreservascab.Network

import com.amkj.appreservascab.Modelos.Notificacion
import retrofit2.Response
import retrofit2.http.*

interface NotificacionesService {

    @GET("notificaciones/{userId}")
    suspend fun obtenerNotificaciones(
        @Path("userId") userId: Int
    ): Response<ApiResponse<List<Notificacion>>>

    @PUT("notificaciones/marcar-leida/{notificacionId}")
    suspend fun marcarComoLeida(
        @Path("notificacionId") notificacionId: Int
    ): Response<ApiResponse<String>>
}