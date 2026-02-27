package com.example.aac.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SnoozeRequest(
    @SerializedName("minutes")
    val minutes: Int
)
