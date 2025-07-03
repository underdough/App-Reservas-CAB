package com.amkj.appreservascab.Network

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.amkj.appreservascab.Modelos.TipoNotificacion
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

object RetrofitClient {

    private const val BASE_URL = "https://tu-servidor.com/api/" // Cambia por tu URL

    // Deserializador personalizado para TipoNotificacion
    private val tipoNotificacionDeserializer = object : JsonDeserializer<TipoNotificacion> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): TipoNotificacion {
            return try {
                TipoNotificacion.valueOf(json.asString)
            } catch (e: Exception) {
                TipoNotificacion.MENSAJE_SISTEMA // Default fallback
            }
        }
    }

    private val gson = GsonBuilder()
        .registerTypeAdapter(TipoNotificacion::class.java, tipoNotificacionDeserializer)
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val notificacionesService: NotificacionesService = retrofit.create(NotificacionesService::class.java)
}
