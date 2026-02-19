package com.example.aac.data.remote.api

import com.example.aac.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import okhttp3.ResponseBody

interface AacApiService {

    // [Auth] 게스트 로그인
    @POST("api/auth/guest")
    suspend fun createGuestAccount(@Body request: GuestLoginRequest): GuestLoginResponse

    // [Auth] 내 정보 조회
    @GET("api/auth/me")
    suspend fun getMyInfo(): MyInfoResponse

    // [Auth] 로그아웃
    @POST("api/auth/logout")
    suspend fun logout(): LogoutResponse

    // [Auth] 회원탈퇴
    @DELETE("api/auth/account")
    suspend fun withdraw(): BaseResponse<Unit>

    // ----------------------------------------------------
    // 🔥 [Main] 단어 목록 조회 (여기가 중요!)
    // ----------------------------------------------------
    // [Auth] 카카오 SDK 로그인
    @POST("api/auth/kakao/sdk")
    suspend fun kakaoLogin(
        @Body request: KakaoLoginRequest
    ): KakaoLoginResponse

    // [Auth] 약관 동의
    @POST("api/auth/social/complete")
    suspend fun completeSocialSignup(
        @Body request: SocialCompleteRequest
    ): KakaoLoginResponse

    // [Auth] 약관 목록 조회
    @GET("api/auth/terms")
    suspend fun getTerms(): BaseResponse<List<TermsResponse>>


    // [Main] 단어 목록 조회
    @GET("api/words")
    suspend fun getWords(
        @Query("categoryId") categoryId: String? = null,
        @Query("isFavorite") isFavorite: Boolean = false
    ): BaseResponse<WordListResponse> // 👈 여기가 핵심!

    // [Category] 카테고리 목록 조회
    @GET("api/categories")
    suspend fun getCategories(): BaseResponse<List<CategoryResponse>>

    // 카테고리 생성
    @POST("api/categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): BaseResponse<CategoryResponse>

    // 카테고리 수정
    @PATCH("api/categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body request: UpdateCategoryRequest
    ): BaseResponse<CategoryResponse>

    // 카테고리 삭제
    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): BaseResponse<DeleteCategoryResponse>

    // 카테고리 순서 변경
    @PATCH("api/order/categories")
    suspend fun updateCategoryOrders(@Body request: CategoryOrderRequest): BaseResponse<CategoryResponse>

    // [Setting] 그리드 설정 조회
    @GET("api/settings/grid")
    suspend fun getGridSetting(): GridSettingResponse

    // [Setting] 그리드 설정 수정
    @PATCH("api/settings/grid")
    suspend fun updateGridSetting(@Body request: GridSettingRequest): GridSettingResponse

    @POST("api/words")
    suspend fun createWord(
        @Body request: CreateWordRequest
    ): BaseResponse<CreateWordResponseData>
    @PATCH("api/words/{cardId}")
    suspend fun updateWord(
        @Path("cardId") cardId: String,
        @Body request: UpdateWordRequest
    ): BaseResponse<UpdateWordResponseData>

    @DELETE("api/words/{cardId}")
    suspend fun deleteWord(
        @Path("cardId") cardId: String
    ): BaseResponse<Unit?>

    // 내 목소리 설정 조회
    @GET("api/ai/tts-setting")
    suspend fun getTtsSetting(): BaseResponse<TtsSettingData>

    // TTS 오디오 생성
    @POST("api/ai/tts")
    suspend fun generateTts(@Body request: TtsRequest): ResponseBody

    @POST("api/ai/predictions")
    suspend fun getAiPredictions(
        @Body request: AiPredictionRequest
    ): BaseResponse<AiPredictionResponseData>

    // ✅ [AI-05] 스타일 문장 변환 (어미 있음 + tone 추가됨)
    @POST("api/ai/styles")
    suspend fun getAiStyles(
        @Body request: AiStyleRequest
    ): BaseResponse<AiStyleResponseData>

    @Multipart
    @PATCH("api/words/{id}")
    suspend fun updateWord(
        @Path("id") id: String,
        @Part("categoryId") categoryId: RequestBody?,
        @Part("word") word: RequestBody?,
        @Part image: MultipartBody.Part?
    ): BaseResponse<WordDto>

    // ✅ [수정] 즐겨찾기 토글 (단어 정보 반환)
    @PATCH("api/words/{id}/favorite")
    suspend fun toggleFavorite(
        @Path("id") id: String,
        @Body request: FavoriteRequest // { "isFavorite": true }
    ): BaseResponse<WordDto>
}