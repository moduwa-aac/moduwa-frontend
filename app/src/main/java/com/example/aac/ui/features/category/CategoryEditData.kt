package com.example.aac.ui.features.category

data class CategoryEditData(
    val id: String? = null,
    val iconRes: Int,
    val iconUrl: String? = null, // 🔥 추가: 커스텀 이미지 URL
    val title: String,
    val count: Int
)
