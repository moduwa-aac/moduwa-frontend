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

data class AiPredictionRequest(
    @SerializedName("words") val words: List<String>,
    @SerializedName("tone") val tone: String? = "HONORIFIC", // "HONORIFIC" or "INFORMAL"
    @SerializedName("refresh") val refresh: Boolean = false
)

// ✅ [AI-01] 응답 데이터
data class AiPredictionResponseData(
    @SerializedName("words") val words: List<String>,

    @SerializedName("predictions") // 👈 서버가 주는 키값
    val sentences: List<String>,   // 👈 앱에서 쓸 변수명

    @SerializedName("tone") val tone: String?,
    @SerializedName("fromCache") val fromCache: Boolean
)

// ✅ [AI-05] 스타일 변환 요청
data class AiStyleRequest(
    @SerializedName("words") val words: List<String>,
    @SerializedName("endingCards") val endingCards: List<String>,
    @SerializedName("tone") val tone: String? = "HONORIFIC", // 독립 파라미터
    @SerializedName("refresh") val refresh: Boolean = false
)

// ✅ [AI-05] 응답 데이터
data class AiStyleResponseData(
    @SerializedName("words") val words: List<String>,
    @SerializedName("endingCards") val endingCards: List<String>, // 더 이상 톤 정보 안 옴
    @SerializedName("sentences") val sentences: List<String>,
    @SerializedName("tone") val tone: String?,
    @SerializedName("fromCache") val fromCache: Boolean
)

data class FavoriteRequest(val isFavorite: Boolean)

data class WordListResponse(
    @SerializedName("category") val category: String,
    @SerializedName("words") val words: List<WordDto> // 진짜 리스트는 여기에!
)

data class WordDto(
    @SerializedName("cardId") val cardId: String,
    @SerializedName("categoryId") val categoryId: String?,
    @SerializedName("categoryName") val categoryName: String?,
    @SerializedName("partOfSpeech") val partOfSpeech: String,
    @SerializedName("word") val word: String,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("isDefault") val isDefault: Boolean?,
    @SerializedName("isFavorite") val isFavorite: Boolean,
    @SerializedName("displayOrder") val displayOrder: Int?
)

data class FavoriteResult(val cardId: String, val isFavorite: Boolean)

data class AiFavoriteRequest(
    @SerializedName("sentence") val sentence: String,
    @SerializedName("sentenceSource") val sentenceSource: String = "AI_SUGGESTED"
)

// ✅ AI 문장 즐겨찾기 조회 응답 (GET) - 나중에 메인화면에서 쓸 용도
data class AiFavoriteItem(
    @SerializedName("id") val id: String,
    @SerializedName("sentence") val sentence: String,
    @SerializedName("sentenceSource") val sentenceSource: String,
    @SerializedName("createdAt") val createdAt: String
)

data class AiFavoriteResponseData(
    @SerializedName("favorites") val favorites: List<AiFavoriteItem>,
    @SerializedName("total") val total: Int
)

