package com.amkj.appreservascab.Modelos


data class ReportItem(
    val recurso_id: Int,
    val nombre_recurso: String,
    val tipo: String, // "ambiente" | "equipo"
    val total: Int
)

data class ReportResponse(
    val inicio: String,
    val fin: String,
    val limit: Int,
    val ambientes_top: List<ReportItem>,
    val equipos_top: List<ReportItem>
)


