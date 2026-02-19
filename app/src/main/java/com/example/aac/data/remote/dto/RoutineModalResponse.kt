package com.example.aac.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RoutineModalResponse(

    @SerializedName("routine")
    val routine: RoutineDto?,

    @SerializedName("actions")
    val actions: RoutineModalActions?,

    @SerializedName("serverTime")
    val serverTime: String?
)

data class RoutineModalActions(

    @SerializedName("snoozeMinutes")
    val snoozeMinutes: Int?,

    @SerializedName("canSnooze")
    val canSnooze: Boolean?,

    @SerializedName("canDismiss")
    val canDismiss: Boolean?
)
