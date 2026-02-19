package com.example.aac.data.mapper

import com.example.aac.data.remote.dto.WordListResponse
import com.example.aac.data.remote.dto.WordResponse
import com.example.aac.domain.model.Word

// WordMapper.kt (위치: data/mapper/WordMapper.kt 또는 비슷한 경로)

object WordMapper {
    // 🔴 [수정 전] fun mapToDomain(response: WordResponse): List<Word> { ... }

    // 🟢 [수정 후] 인자 타입을 WordListResponse로 변경
    fun mapToDomain(response: WordListResponse): List<Word> {
        // response.words 가 실제 리스트입니다.
        return response.words.map { dto ->
            Word(
                cardId = dto.cardId,
                categoryId = dto.categoryId ?: "",
                // ... 나머지 필드 매핑 ...
                word = dto.word,
                imageUrl = dto.imageUrl ?: "",
                partOfSpeech = dto.partOfSpeech,
                isFavorite = dto.isFavorite,
                isDefault = dto.isDefault ?: false,
                displayOrder = dto.displayOrder ?: 0
            )
        }
    }
}