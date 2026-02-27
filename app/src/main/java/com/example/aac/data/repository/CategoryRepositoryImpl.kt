package com.example.aac.data.repository

import android.util.Log
import com.example.aac.data.mapper.WordMapper
import com.example.aac.data.remote.api.AacApiService
import com.example.aac.data.remote.dto.*
import com.example.aac.domain.model.Category
import com.example.aac.domain.model.Word
import com.example.aac.domain.repository.CategoryRepository

class CategoryRepositoryImpl(
    private val api: AacApiService
) : CategoryRepository {

    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = api.getCategories()
            if (response.success && response.data != null) {
                val list = response.data.map { dto ->
                    Category(
                        id = dto.id ?: "",
                        name = dto.name,
                        iconUrl = dto.iconUrl,
                        displayOrder = dto.displayOrder ?: 0,
                        iconKey = dto.iconKey,
                        wordCount = dto.wordCount ?: 0
                    )
                }
                Result.success(list.sortedBy { it.displayOrder })
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createCategory(name: String, iconKey: String?, iconUrl: String?): Result<Category> {
        Log.e("CATEGORY_DEBUG", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.e("CATEGORY_DEBUG", "1. Repository - createCategory 진입")
        Log.e("CATEGORY_DEBUG", "▶ 파라미터 - iconKey: $iconKey, iconUrl: $iconUrl")

        return try {
            val request = CreateCategoryRequest(
                name = name,
                iconKey = if (!iconUrl.isNullOrBlank()) null else iconKey,
                iconUrl = if (iconUrl.isNullOrBlank()) null else iconUrl
            )

            Log.e("CATEGORY_DEBUG", "▶ 서버 전송 데이터: $request")

            val response = api.createCategory(request)

            if (response.success && response.data != null) {
                val data = response.data
                Result.success(Category(
                    id = data.id ?: "",
                    name = data.name,
                    iconUrl = data.iconUrl,
                    displayOrder = data.displayOrder ?: 0,
                    iconKey = data.iconKey,
                    wordCount = data.wordCount ?: 0
                ))
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e("CATEGORY_DEBUG", "💥 예외 발생: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateCategory(id: String, name: String, iconKey: String?, displayOrder: Int, iconUrl: String?): Result<Category> {
        Log.e("CATEGORY_DEBUG", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.e("CATEGORY_DEBUG", "1. Repository - updateCategory 진입 (ID: $id)")

        return try {
            val request = UpdateCategoryRequest(
                name = name,
                iconKey = if (!iconUrl.isNullOrBlank()) null else iconKey,
                iconUrl = if (iconUrl.isNullOrBlank()) null else iconUrl,
                displayOrder = displayOrder
            )

            Log.e("CATEGORY_DEBUG", "▶ 서버 PATCH 요청 본문: $request")

            val response = api.updateCategory(id, request)

            if (response.success && response.data != null) {
                val data = response.data
                Log.e("CATEGORY_DEBUG", "✅ 수정 성공! 결과 iconKey: ${data.iconKey}, iconUrl: ${data.iconUrl}")
                Result.success(Category(
                    id = data.id ?: id,
                    name = data.name ?: name,
                    iconUrl = data.iconUrl,
                    displayOrder = data.displayOrder ?: displayOrder,
                    iconKey = data.iconKey ?: iconKey,
                    wordCount = data.wordCount ?: 0
                ))
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e("CATEGORY_DEBUG", "💥 수정 중 예외 발생: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(id: String): Result<String> {
        return try {
            val response = api.deleteCategory(id)
            if (response.success && response.data != null) Result.success(response.data.id)
            else Result.failure(Exception(response.message))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateCategoryOrders(orders: Map<String, Int>): Result<Boolean> {
        return try {
            val orderItems = orders.map { (id, order) -> CategoryOrderItem(id = id, displayOrder = order) }
            val request = CategoryOrderRequest(orders = orderItems)
            val response = api.updateCategoryOrders(request)
            if (response.success) Result.success(true) else Result.failure(Exception(response.message))
        } catch (e: Exception) { Result.failure(e) }
    }

    // ✅ 님의 변경 사항 (WordMapper.mapToDomain(response.data)) 적용
    override suspend fun getWords(categoryId: String?): Result<List<Word>> {
        return try {
            val response = api.getWords(categoryId)
            if (response.success && response.data != null) {
                val domainList = WordMapper.mapToDomain(response.data)
                Result.success(domainList)
            } else {
                Result.failure(Exception(response.message ?: "데이터가 비어있거나 에러가 발생했습니다."))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    // ✅ 동료의 추가 기능 (createWord) 반영
    override suspend fun createWord(categoryId: String, word: String, imageUrl: String?): Result<Unit> {
        Log.e("WORD_CREATE_DEBUG", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.e("WORD_CREATE_DEBUG", "1. Repository - createWord 시작")
        Log.e("WORD_CREATE_DEBUG", "▶ word: $word, imageUrl: $imageUrl")

        return try {
            val finalUrl = imageUrl ?: ""
            val request = CreateWordRequest(
                categoryId = categoryId,
                word = word,
                imageUrl = finalUrl
            )

            val response = api.createWord(request)

            Log.e("WORD_CREATE_DEBUG", "📡 서버 응답 수신: ${response.success}")

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "데이터가 비어있거나 에러가 발생했습니다."))
            }
        } catch (e: Exception) {
            Log.e("WORD_CREATE_DEBUG", "💥 예외 발생: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateWord(cardId: String, word: String, imageUrl: String?, categoryId: String): Result<Unit> {
        return try {
            val request = UpdateWordRequest(word = word, imageUrl = imageUrl, categoryId = categoryId)
            val response = api.updateWord(cardId, request)
            if (response.success) Result.success(Unit) else Result.failure(Exception(response.message))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteWord(cardId: String): Result<Unit> {
        return try {
            val response = api.deleteWord(cardId)
            if (response.success) Result.success(Unit) else Result.failure(Exception(response.message))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun reorderWords(categoryId: String, orderedCardIds: List<String>): Result<Unit> {
        return try {
            val response = api.reorderWords(WordReorderRequest(categoryId, orderedCardIds))
            if (response.success) Result.success(Unit) else Result.failure(Exception(response.message))
        } catch (e: Exception) { Result.failure(e) }
    }
}