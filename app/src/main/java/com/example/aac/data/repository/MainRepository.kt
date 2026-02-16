package com.example.aac.data.repository

import com.example.aac.data.remote.api.RetrofitInstance
import com.example.aac.data.remote.dto.CategoryResponse
import com.example.aac.data.remote.dto.MainWordItem
import com.example.aac.data.remote.dto.*
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
            val response = RetrofitInstance.api.getWords(categoryId, if(onlyFavorite) true else null)

            response.data.words.map { oldWord ->
                MainWordItem(
                    cardId = oldWord.cardId,

                    categoryId = oldWord.categoryId ?: "",

                    partOfSpeech = oldWord.partOfSpeech,
                    word = oldWord.word,
                    imageUrl = oldWord.imageUrl ?: "",

                    isDefault = oldWord.isDefault ?: false,
                    isFavorite = oldWord.isFavorite,
                    displayOrder = oldWord.displayOrder ?: 0
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createWord(categoryId: String, word: String, imageUrl: String?): Boolean {
        return try {
            val request = CreateWordRequest(categoryId, word, imageUrl)
            val response = RetrofitInstance.api.createWord(request)
            response.success // 성공 여부 반환
        } catch (e: Exception) {
            e.printStackTrace()
            false
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
            val response = RetrofitInstance.api.getWords(categoryId)

            if (response.success && response.data != null) {
                // ✅ 안전하게 캐스팅 (as? 사용)
                (response.data as? List<MainWordItem>) ?: emptyList()
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
}