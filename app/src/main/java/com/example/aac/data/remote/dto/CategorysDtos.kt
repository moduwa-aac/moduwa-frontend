package com.example.aac.data.remote.dto

import com.google.gson.annotations.SerializedName

// ==========================================
// [통합] 카테고리 관련 DTO 모음
// ==========================================

// 1. 카테고리 조회 응답
data class CategoryResponse(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("iconUrl") val iconUrl: String?,
    @SerializedName("iconKey") val iconKey: String? = null,
    @SerializedName("displayOrder") val displayOrder: Int? = null,
    @SerializedName("wordCount") val wordCount: Int? = null
)

// 2. 카테고리 생성(POST) 요청
data class CreateCategoryRequest(
    @SerializedName("name") val name: String,
    @SerializedName("iconKey") val iconKey: String? = null,
    @SerializedName("iconUrl") val iconUrl: String? = null
)

// 3. 카테고리 수정(PATCH) 요청
data class UpdateCategoryRequest(
    @SerializedName("name") val name: String?,
    @SerializedName("iconKey") val iconKey: String? = null, // 🔥 Nullable로 변경
    @SerializedName("displayOrder") val displayOrder: Int?,
    @SerializedName("iconUrl") val iconUrl: String? = null, // 🔥 추가 및 Nullable 처리
    @SerializedName("isFavorite") val isFavorite: Boolean? = null
)

// 4. 카테고리 삭제(DELETE) 응답
data class DeleteCategoryResponse(
    @SerializedName("id") val id: String
)

// ==========================================
// [순서 변경] 관련 DTO
// ==========================================

data class CategoryOrderRequest(
    @SerializedName("orders") val orders: List<CategoryOrderItem>
)

data class CategoryOrderItem(
    @SerializedName("categoryId") val id: String,
    @SerializedName("displayOrder") val displayOrder: Int
)

data class CategoryOrderResponse(
    @SerializedName("updatedCount") val updatedCount: Int
)
