package com.example.aac.domain.repository

import com.example.aac.domain.model.Category
import com.example.aac.domain.model.Word

interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
    suspend fun createCategory(name: String, iconKey: String?, iconUrl: String?): Result<Category>
    // 🔥 [수정] iconUrl도 받을 수 있도록 파라미터 확장
    suspend fun updateCategory(id: String, name: String, iconKey: String?, displayOrder: Int, iconUrl: String?): Result<Category>
    suspend fun deleteCategory(id: String): Result<String>
    suspend fun updateCategoryOrders(orders: Map<String, Int>): Result<Boolean>
    suspend fun getWords(categoryId: String?): Result<List<Word>>

    suspend fun createWord(categoryId: String, word: String, imageUrl: String?): Result<Unit>
    suspend fun updateWord(cardId: String, word: String, imageUrl: String?, categoryId: String): Result<Unit>
    suspend fun deleteWord(cardId: String): Result<Unit>
    suspend fun reorderWords(categoryId: String, orderedCardIds: List<String>): Result<Unit>
}
