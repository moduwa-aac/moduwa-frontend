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

data class UpdateWordRequest(
    @SerializedName("categoryId") val categoryId: String? = null, // 선택적 수정
    @SerializedName("word") val word: String? = null,           // 선택적 수정
    @SerializedName("imageUrl") val imageUrl: String? = null    // 선택적 수정
)

data class UpdateWordResponseData(
    @SerializedName("word") val word: MainWordItem
)