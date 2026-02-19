package com.example.aac.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RoutinesDataDto(
    @SerializedName("routines")
    val routines: List<RoutineDto>
)

data class RoutineDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("repeatType")
    val repeatType: String, // "WEEKLY" 같은 값

    @SerializedName("daysOfWeek")
    val daysOfWeek: List<Int>?,

    @SerializedName("daysOfMonth")
    val daysOfMonth: List<Int>?,

    @SerializedName("isMonthEnd")
    val isMonthEnd: Boolean,

    @SerializedName("scheduledTime")
    val scheduledTime: String, // "08:30"

    @SerializedName("isActive")
    val isActive: Boolean,

    @SerializedName("snoozedUntil")
    val snoozedUntil: String?,

    // ✅ 응답에 없을 수 있어서 nullable + 기본값
    @SerializedName("startDate")
    val startDate: String? = null,

    @SerializedName("dismissedUntil")
    val dismissedUntil: String?,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)
