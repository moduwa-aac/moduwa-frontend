package com.example.aac.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateRoutineRequest(
    @SerializedName("message")
    val message: String,

    @SerializedName("scheduledTime")
    val scheduledTime: String,   // "21:00"

    @SerializedName("repeatType")
    val repeatType: String,       // "DAILY" | "WEEKLY" | "BIWEEKLY" | "MONTHLY"

    @SerializedName("daysOfWeek")
    val daysOfWeek: List<Int> = emptyList(),

    @SerializedName("daysOfMonth")
    val daysOfMonth: List<Int> = emptyList(),

    @SerializedName("isMonthEnd")
    val isMonthEnd: Boolean = false
)
