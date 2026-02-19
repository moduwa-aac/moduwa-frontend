package com.example.aac.data.remote.dto

data class RoutineUpdateRequest(
    val message: String,
    val scheduledTime: String,
    val repeatType: String,
    val daysOfWeek: List<Int>?,
    val daysOfMonth: List<Int>?,
    val isMonthEnd: Boolean
)
