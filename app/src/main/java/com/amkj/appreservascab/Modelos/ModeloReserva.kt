package com.amkj.appreservascab.Modelos

data class ModeloReserva(
    val id_reserva: Int? = null,
    val usuario_id: Int,
    val ambiente_id: Int,
    val fecha_hora_inicio: String,
    val fecha_hora_fin: String,
    val motivo: String?,
    val jornadas: String?,
    val ambiente_nombre: String?,
    val ambiente_imagen: String? = null,
    val estado: String?= null,
    val tipo_reserva: String? = null // ← AGREGADO: "ambiente" o "equipo"
)


//data class ModeloReserva(
//    val usuario_id: Int,
//    val ambiente_id: Int,
//    val fecha_hora_inicio: String,
//    val fecha_hora_fin: String,
//    val motivo: String = "Reserva de ambiente",
//    val jornadas: String,
//    val id: Int = 0,  // Este campo puedes mantenerlo si lo usas en tu adaptador
//
//    val ambiente_nombre: String? = null,
//    val ambiente_imagen: String? = null,
//    val estado: String? = null,
//
//    // 🔽 NUEVOS CAMPOS (para cancelación)
//    val id_reserva: Int? = null,          // ID real de la reserva (ambiente o equipo)
//    val tipo_reserva: String? = null      // "ambiente" o "equipo"
//)
