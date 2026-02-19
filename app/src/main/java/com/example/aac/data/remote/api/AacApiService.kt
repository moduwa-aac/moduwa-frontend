package com.example.aac.data.remote.api

import com.example.aac.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface AacApiService {

    // [Auth] 인증 관련
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

    // [Word] 낱말 카드 관련
    @GET("api/words")
    suspend fun getWords(
        @Query("categoryId") categoryId: String? = null,
        @Query("isFavorite") isFavorite: Boolean = false
    ): BaseResponse<WordListResponse>

    @POST("api/words")
    suspend fun createWord(@Body request: CreateWordRequest): BaseResponse<CreateWordResponseData>

    @PATCH("api/words/{cardId}")
    suspend fun updateWord(
        @Path("cardId") cardId: String,
        @Body request: UpdateWordRequest
    ): BaseResponse<UpdateWordResponseData>

    @Multipart
    @PATCH("api/words/{id}")
    suspend fun updateWordMultipart(
        @Path("id") id: String,
        @Part("categoryId") categoryId: RequestBody?,
        @Part("word") word: RequestBody?,
        @Part image: MultipartBody.Part?
    ): BaseResponse<WordDto>

    @DELETE("api/words/{cardId}")
    suspend fun deleteWord(@Path("cardId") cardId: String): BaseResponse<Unit>

    @PATCH("api/words/reorder")
    suspend fun reorderWords(@Body request: WordReorderRequest): BaseResponse<Unit>

    @PATCH("api/words/{id}/favorite")
    suspend fun toggleFavorite(
        @Path("id") id: String,
        @Body request: FavoriteRequest
    ): BaseResponse<WordDto>

    // [Category] 카테고리 관련
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

    // [Settings] 설정 관련
    @GET("api/settings/grid")
    suspend fun getGridSetting(): GridSettingResponse

    @PATCH("api/settings/grid")
    suspend fun updateGridSetting(@Body request: GridSettingRequest): GridSettingResponse

    // [AI] TTS 및 예측 관련
    @GET("api/ai/tts-setting")
    suspend fun getTtsSetting(): BaseResponse<TtsSettingData>

    @POST("api/ai/tts")
    suspend fun generateTts(@Body request: TtsRequest): ResponseBody

    @POST("api/ai/predictions")
    suspend fun getAiPredictions(@Body request: AiPredictionRequest): BaseResponse<AiPredictionResponseData>

    @POST("api/ai/styles")
    suspend fun getAiStyles(@Body request: AiStyleRequest): BaseResponse<AiStyleResponseData>

    // [AI] 문장 즐겨찾기 관련
    @POST("api/ai/favorites")
    suspend fun addSentenceFavorite(@Body request: AiFavoriteRequest): BaseResponse<Any>

    @GET("api/ai/favorites")
    suspend fun getSentenceFavorites(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): BaseResponse<AiFavoriteResponseData>

    @DELETE("api/ai/favorites/{favoriteId}")
    suspend fun deleteSentenceFavorite(@Path("favoriteId") favoriteId: String): BaseResponse<Any>
}