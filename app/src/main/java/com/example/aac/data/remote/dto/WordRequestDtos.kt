package com.example.aac.data.remote.dto

import com.google.gson.annotations.SerializedName

// 낱말 카드 순서 변경 요청
data class WordReorderRequest(
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("orderedCardIds") val orderedCardIds: List<String>
)

// 낱말 카드 수정 요청
data class UpdateWordRequest(
    @SerializedName("word") val word: String,
    @SerializedName("imageUrl") val imageUrl: String?,
    // 🔥 [수정] 백엔드 사양 변경에 맞춰 필드명 변경: userCategory -> categoryId
    @SerializedName("categoryId") val categoryId: String
)

// 🔥 [추가] 낱말 카드 생성 요청
data class CreateWordRequest(
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("word") val word: String,
    @SerializedName("imageUrl") val imageUrl: String?
)
