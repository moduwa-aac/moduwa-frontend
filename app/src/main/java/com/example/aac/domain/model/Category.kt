package com.example.aac.domain.model

data class Category(
    val id: String,
    val name: String,
    val iconUrl: String?,
    val displayOrder: Int,
    val iconKey: String? = null,
    val wordCount: Int = 0 // 🔥 추가: 해당 카테고리의 낱말 개수
)
