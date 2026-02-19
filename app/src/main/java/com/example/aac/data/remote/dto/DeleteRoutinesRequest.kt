package com.example.aac.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeleteRoutinesRequest(
    @SerializedName("ids")
    val ids: List<String>
)
