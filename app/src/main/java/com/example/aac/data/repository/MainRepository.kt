package com.example.aac.data.repository

import android.util.Log
import com.example.aac.data.remote.api.RetrofitInstance
import com.example.aac.data.remote.api.RetrofitInstance.api
import com.example.aac.data.remote.dto.CategoryResponse
import com.example.aac.data.remote.dto.MainWordItem
import com.example.aac.data.remote.dto.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MainRepository {
    suspend fun getCategories(): List<CategoryResponse> {
        return try {
            val response = RetrofitInstance.api.getCategories()

            if (response.success && response.data != null) {
                response.data
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getWords(categoryId: String? = null, onlyFavorite: Boolean = false): List<MainWordItem> {
        return try {
            val response = api.getWords(categoryId, onlyFavorite)

            if (response.success && response.data != null) {
                val rawList = response.data.words.map { word ->
                    MainWordItem(
                        cardId = word.cardId,
                        word = word.word,
                        imageUrl = word.imageUrl ?: "",
                        partOfSpeech = word.partOfSpeech,
                        categoryId = word.categoryId ?: "",
                        categoryName = word.categoryName ?: "미분류", // 🔥 동료 코드 반영
                        isFavorite = word.isFavorite,
                        isDefault = word.isDefault ?: false,
                        displayOrder = word.displayOrder ?: 0
                    )
                }

                // 중복 제거 (서버 이슈 방어)
                val uniqueList = rawList.distinctBy { it.word }

                Log.d("RepoFix", "📦 가져온 단어: ${uniqueList.size}개 (카테고리: ${response.data.category})")

                return uniqueList

            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RepoFix", "데이터 로드 실패: ${e.message}")
            emptyList()
        }
    }

    suspend fun createWord(
        categoryId: String,
        word: String,
        imageUrl: String? // URL String 그대로 전달
    ): String? {
        return try {
            val request = CreateWordRequest(
                categoryId = categoryId,
                word = word,
                imageUrl = imageUrl
            )

            val response = api.createWord(request)

            if (response.success && response.data != null) {
                response.data.word.cardId
            } else {
                android.util.Log.e("MainRepo", "생성 실패(서버): ${response.message}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("MainRepo", "생성 실패(네트워크): ${e.message}")
            null
        }
    }

    // 낱말 수정
    suspend fun updateWord(cardId: String, categoryId: String?, word: String?, imageUrl: String?): MainWordItem? {
        return try {

            val request = UpdateWordRequest(
                categoryId = categoryId ?: "", // null이면 빈 문자열 전달
                word = word ?: "",             // null이면 빈 문자열 전달
                imageUrl = imageUrl            // imageUrl은 보통 Nullable이 허용됨
            )

            val response = RetrofitInstance.api.updateWord(cardId, request)
            if (response.success) response.data?.word else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 낱말 삭제
    suspend fun deleteWord(cardId: String): Boolean {
        return try {
            val response = RetrofitInstance.api.deleteWord(cardId)
            response.success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun fetchWords(categoryId: String): List<MainWordItem> {
        return try {
            val response = api.getWords(categoryId)

            if (response.success && response.data != null) {
                response.data.words.map { word ->
                    MainWordItem(
                        cardId = word.cardId,
                        categoryId = word.categoryId ?: "",
                        categoryName = word.categoryName ?: "미분류",
                        partOfSpeech = word.partOfSpeech,
                        word = word.word,
                        imageUrl = word.imageUrl ?: "",
                        isDefault = word.isDefault ?: false,
                        isFavorite = word.isFavorite,
                        displayOrder = word.displayOrder ?: 0
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getTtsVoiceKey(): String {
        return try {
            val response = RetrofitInstance.api.getTtsSetting()
            if (response.success && response.data != null) {
                response.data.voiceKey
            } else {
                "ADULT_FEMALE_DEFAULT" // 기본값
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "ADULT_FEMALE_DEFAULT" // 에러 시 기본값
        }
    }

    // ✅ TTS 오디오 데이터 가져오기 (ByteArray 반환)
    suspend fun fetchTtsAudio(text: String, voiceKey: String): ByteArray? {
        return try {
            val request = TtsRequest(text, voiceKey)

            val responseBody = RetrofitInstance.api.generateTts(request)
            responseBody.bytes() // 스트림을 바이트 배열로 변환
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAiPredictions(words: List<String>, tone: String): List<String> {
        return try {
            val request = AiPredictionRequest(words, tone)
            val response = RetrofitInstance.api.getAiPredictions(request)

            if (response.success && response.data != null) {
                response.data.sentences
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ✅ [AI-05] 스타일 변환 (어미 있을 때)
    suspend fun getAiStyles(words: List<String>, endingCards: List<String>, tone: String): List<String> {
        return try {
            val request = AiStyleRequest(words, endingCards, tone)
            val response = RetrofitInstance.api.getAiStyles(request)

            if (response.success && response.data != null) {
                android.util.Log.d("AI_API", "Style 응답: ${response.data.sentences}, Tone: ${response.data.tone}")
                response.data.sentences
            } else {
                android.util.Log.e("AI_API", "Style 실패: ${response.message}")
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun toggleFavorite(cardId: String, isFavorite: Boolean): FavoriteResult? {
        return try {
            val request = FavoriteRequest(isFavorite)
            val response = api.toggleFavorite(cardId, request)

            if (response.success && response.data != null) {
                FavoriteResult(
                    cardId = response.data.cardId,
                    isFavorite = response.data.isFavorite
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAiSentenceFavorites(): List<MainWordItem> {
        return try {
            val response = RetrofitInstance.api.getSentenceFavorites()
            if (response.success && response.data != null) {
                response.data.favorites.map { favItem ->
                    MainWordItem(
                        cardId = favItem.id,
                        word = favItem.sentence,
                        imageUrl = "",
                        partOfSpeech = "AI_SENTENCE",
                        categoryId = "FAVORITE_SENTENCE_DUMMY",
                        categoryName = "AI 문장", // 🔥 DTO 변경에 따른 컴파일 에러 방지용
                        isFavorite = true,
                        isDefault = false,
                        displayOrder = 0
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun deleteAiSentenceFavorite(favoriteId: String): Boolean {
        return try {
            val response = RetrofitInstance.api.deleteSentenceFavorite(favoriteId)
            response.success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}