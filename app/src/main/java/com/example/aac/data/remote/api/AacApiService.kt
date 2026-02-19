package com.example.aac.data.remote.api

import com.example.aac.data.remote.dto.*
import retrofit2.http.*

interface AacApiService {

    // [Auth] 관련 API 생략...
    @POST("api/auth/guest")
    suspend fun createGuestAccount(@Body request: GuestLoginRequest): GuestLoginResponse
    @GET("api/auth/me")
    suspend fun getMyInfo(): MyInfoResponse
    @POST("api/auth/logout")
    suspend fun logout(): LogoutResponse
    @DELETE("api/auth/account")
    suspend fun withdraw(): BaseResponse<Unit>
    @POST("api/auth/kakao/sdk")
    suspend fun kakaoLogin(@Body request: KakaoLoginRequest): KakaoLoginResponse
    @POST("api/auth/social/complete")
    suspend fun completeSocialSignup(@Body request: SocialCompleteRequest): KakaoLoginResponse
    @GET("api/auth/terms")
    suspend fun getTerms(): BaseResponse<List<TermsResponse>>

    // ----------------------------------------------------
    // 🔥 [Word] 낱말 카드 관련 API
    // ----------------------------------------------------

    @GET("api/words")
    suspend fun getWords(
        @Query("categoryId") categoryId: String? = null,
        @Query("onlyFavorite") onlyFavorite: Boolean? = null
    ): WordResponse

    // [수정] 명세서대로 JSON(@Body) 방식을 사용합니다.
    @POST("api/words")
    suspend fun createWord(@Body request: CreateWordRequest): BaseResponse<Any>

    @PATCH("api/words/{cardId}")
    suspend fun updateWord(
        @Path("cardId") cardId: String,
        @Body request: UpdateWordRequest
    ): BaseResponse<Any>

    @DELETE("api/words/{cardId}")
    suspend fun deleteWord(
        @Path("cardId") cardId: String
    ): BaseResponse<Unit>

    @PATCH("api/words/reorder")
    suspend fun reorderWords(
        @Body request: WordReorderRequest
    ): BaseResponse<Unit>

    // ----------------------------------------------------
    // [Category] 관련 API 생략...
    // ----------------------------------------------------
    @GET("api/categories")
    suspend fun getCategories(): BaseResponse<List<CategoryResponse>>
    @POST("api/categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): BaseResponse<CategoryResponse>
    @PATCH("api/categories/{id}")
    suspend fun updateCategory(@Path("id") id: String, @Body request: UpdateCategoryRequest): BaseResponse<CategoryResponse>
    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): BaseResponse<DeleteCategoryResponse>
    @PATCH("api/order/categories")
    suspend fun updateCategoryOrders(@Body request: CategoryOrderRequest): BaseResponse<CategoryOrderResponse>
    @GET("api/settings/grid")
    suspend fun getGridSetting(): GridSettingResponse
    @PATCH("api/settings/grid")
    suspend fun updateGridSetting(@Body request: GridSettingRequest): GridSettingResponse
    @POST("api/ai/predictions")
    suspend fun getAiPredictions(@Body request: AiPredictionRequest): AiPredictionResponse
}
