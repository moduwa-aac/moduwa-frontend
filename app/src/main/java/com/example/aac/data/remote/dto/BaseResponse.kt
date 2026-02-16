package com.example.aac.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data") val data: T?,

    @SerializedName("message")
    val message: String?
)
