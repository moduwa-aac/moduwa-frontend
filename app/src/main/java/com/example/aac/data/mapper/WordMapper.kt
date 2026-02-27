package com.example.aac.data.mapper

import com.example.aac.data.remote.dto.WordListResponse
import com.example.aac.data.remote.dto.WordResponse
import com.example.aac.domain.model.Word

object WordMapper {
    fun mapToDomain(response: WordListResponse): List<Word> {
        // response.words 가 실제 리스트입니다.
        return response.words.map { dto ->
            Word(
                cardId = dto.cardId,
                categoryId = dto.categoryId ?: "",
                word = dto.word,
                imageUrl = dto.imageUrl ?: "",
                partOfSpeech = dto.partOfSpeech,
                categoryName = dto.categoryName ?: "미분류",
                isFavorite = dto.isFavorite,
                isDefault = dto.isDefault ?: false,
                displayOrder = dto.displayOrder ?: 0
            )
        }
    }
}