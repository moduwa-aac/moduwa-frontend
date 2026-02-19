package com.example.aac.data.mapper

import com.example.aac.data.remote.dto.WordResponse
import com.example.aac.domain.model.Word

object WordMapper {
    fun mapToDomain(response: WordResponse): List<Word> {
        return response.data.words.map { dto ->
            Word(
                cardId = dto.cardId,
                word = dto.word,
                imageUrl = dto.imageUrl ?: "",
                partOfSpeech = dto.partOfSpeech,
                categoryId = dto.categoryId ?: "",
                categoryName = dto.categoryName ?: "미분류", // 🔥 categoryName 매핑 추가
                isFavorite = dto.isFavorite,
                isDefault = dto.isDefault ?: false,
                displayOrder = dto.displayOrder ?: 0
            )
        }
    }
}
