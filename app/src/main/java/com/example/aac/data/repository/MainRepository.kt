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

            // ✅ response.data가 null이 아닌지 확인
            if (response.success && response.data != null) {

                // 🚨 [수정 포인트] response.data가 아니라 response.data.words를 가져와야 함!
                // 서버가 "words" 필드 안에 리스트를 넣어줬기 때문
                val rawList = response.data.words.map { word ->
                    MainWordItem(
                        cardId = word.cardId,
                        word = word.word,
                        imageUrl = word.imageUrl ?: "",
                        partOfSpeech = word.partOfSpeech,
                        categoryId = word.categoryId ?: "",
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
            // ✅ [수정] DTO 객체 생성
            val request = CreateWordRequest(
                categoryId = categoryId,
                word = word,
                imageUrl = imageUrl
            )

            // API 호출
            val response = api.createWord(request)

            // 응답 처리 (data -> word -> cardId)
            if (response.success && response.data != null) {
                // CreateWordResponseData 안에 MainWordItem이 들어있는 구조
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
            val request = UpdateWordRequest(categoryId, word, imageUrl)
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
            // 여기도 getWords와 똑같이 처리해야 함
            val response = api.getWords(categoryId) // API 재활용

            if (response.success && response.data != null) {
                // response.data.words 에서 꺼내야 함 (WordListResponse 구조)
                response.data.words.map { word ->
                    MainWordItem(
                        cardId = word.cardId,
                        word = word.word,
                        imageUrl = word.imageUrl ?: "",
                        partOfSpeech = word.partOfSpeech,
                        categoryId = word.categoryId ?: "",
                        isFavorite = word.isFavorite,
                        isDefault = word.isDefault ?: false,
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
            // tone: "HONORIFIC" or "INFORMAL"
            val request = AiPredictionRequest(words, tone)
            val response = RetrofitInstance.api.getAiPredictions(request) // API 인터페이스도 맞춰야 함

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
            // API 호출 (반환 타입을 임시로 Map이나 유연한 객체로 변경하거나, 아래처럼 처리)
            val response = api.toggleFavorite(cardId, request)

            if (response.success && response.data != null) {
                // 서버가 준 데이터만 사용
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
                // 서버에서 받은 AI 즐겨찾기 목록을 MainWordItem으로 둔갑(?)시킵니다.
                response.data.favorites.map { favItem ->
                    MainWordItem(
                        cardId = favItem.id, // 삭제/수정을 위해 id 보관
                        word = favItem.sentence, // 문장을 낱말 이름 자리에 넣음
                        imageUrl = "", // AI 문장은 기본적으로 사진 없음
                        partOfSpeech = "AI_SENTENCE", // 🟢 일반 낱말과 구분하기 위한 꼼수 태그!
                        categoryId = "FAVORITE_SENTENCE_DUMMY",
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