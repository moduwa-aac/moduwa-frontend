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
}