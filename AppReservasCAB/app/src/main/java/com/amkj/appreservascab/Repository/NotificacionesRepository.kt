package com.amkj.appreservascab.Repository

import com.amkj.appreservascab.Network.RetrofitClient
import com.amkj.appreservascab.Modelos.Notificacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificacionesRepository {

    private val apiService = RetrofitClient.notificacionesService

    suspend fun obtenerNotificaciones(userId: Int): Result<List<Notificacion>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.obtenerNotificaciones(userId)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Error desconocido"))
                    }
                } else {
                    Result.failure(Exception("Error HTTP: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun marcarComoLeida(notificacionId: Int): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.marcarComoLeida(notificacionId)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Result.success(apiResponse.message ?: "Marcada como leída")
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Error al marcar como leída"))
                    }
                } else {
                    Result.failure(Exception("Error HTTP: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}