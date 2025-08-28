package com.amkj.appreservascab.Network

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val total: Int? = null
)