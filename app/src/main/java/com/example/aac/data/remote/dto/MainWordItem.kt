package com.example.aac.data.remote.dto

// 메인 화면 API 전용 낱말 아이템
data class MainWordItem(
    val cardId: String,
    val categoryId: String,
    val categoryName: String, // 🔥 추가: 카테고리 이름
    val partOfSpeech: String,
    val word: String,
    val imageUrl: String,
    val isDefault: Boolean,
    val isFavorite: Boolean,
    val displayOrder: Int
)