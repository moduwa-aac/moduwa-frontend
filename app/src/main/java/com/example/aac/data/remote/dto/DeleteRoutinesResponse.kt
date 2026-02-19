package com.example.aac.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeleteRoutinesResponse(
    @SerializedName("deletedCount")
    val deletedCount: Int,
    @SerializedName("deletedIds")
    val deletedIds: List<String>
)

data class DeleteAllRoutinesResponse(
    @SerializedName("deletedCount")
    val deletedCount: Int
)
