package com.example.aac.data.remote.dto

import com.google.gson.annotations.SerializedName

// 1. 전체 응답
data class WordResponse(
    val success: Boolean,
    val data: WordData,
    val message: String
)

// 2. 데이터 내부 리스트
data class WordData(
    // 🔥 중요: 여기서 List<Word>가 아니라 List<WordItem>을 써야 합니다!
    @SerializedName("words")
    val words: List<WordItem>
)

// 3. 낱말 아이템 (서버 JSON 필드와 1:1 매칭)
data class WordItem(
    @SerializedName("cardId") val cardId: String,
    @SerializedName("categoryId") val categoryId: String?, // null 가능성 대비
    @SerializedName("categoryName") val categoryName: String?,
    @SerializedName("word") val word: String,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("partOfSpeech") val partOfSpeech: String,
    @SerializedName("isFavorite") val isFavorite: Boolean,

    @SerializedName("isDefault") val isDefault: Boolean?,
    @SerializedName("displayOrder") val displayOrder: Int?
)