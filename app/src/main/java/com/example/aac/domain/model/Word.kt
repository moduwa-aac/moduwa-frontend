package com.example.aac.domain.model

data class Word(
    val cardId: String,
    val categoryId: String,
    val categoryName: String, // 🔥 추가: 추출을 위해 필요
    val partOfSpeech: String,
    val word: String,
    val imageUrl: String,
    val isDefault: Boolean,
    val isFavorite: Boolean,
    val displayOrder: Int
)
