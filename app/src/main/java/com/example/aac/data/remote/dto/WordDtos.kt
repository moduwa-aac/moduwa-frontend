package com.example.aac.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateWordRequest(
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("word") val word: String,
    @SerializedName("imageUrl") val imageUrl: String? = null
)

data class CreateWordResponseData(
    @SerializedName("word") val word: MainWordItem
)