package com.example.aac.ui.components

data class CategoryItem(
    val name: String,
    val iconRes: Int,
    val isSelected: Boolean = false,
    val serverId: String? = null,
    val iconUrl: String? = null,
    val iconKey: String? = null,
    val displayOrder: Int = 0
)