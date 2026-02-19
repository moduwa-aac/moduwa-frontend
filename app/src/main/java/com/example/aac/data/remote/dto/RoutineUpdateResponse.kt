package com.example.aac.data.remote.dto

data class RoutineUpdateResponse(
    val success: Boolean,
    val data: RoutineUpdateData,
    val message: String
)

data class RoutineUpdateData(
    val routine: RoutineDto
)
