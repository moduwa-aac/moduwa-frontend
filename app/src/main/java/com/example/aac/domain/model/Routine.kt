package com.example.aac.domain.model

data class Routine(
    val id: String,
    val message: String,
    val repeatType: String,
    val daysOfWeek: List<Int>,
    val daysOfMonth: List<Int>,
    val isMonthEnd: Boolean,
    val scheduledTime: String,
    val isActive: Boolean
)