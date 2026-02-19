package com.example.aac.data.remote.api

import com.example.aac.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Headers
import retrofit2.http.Streaming

interface AacApiService {

    // ----------------------------------------------------
    // [Auth]
    // ----------------------------------------------------

    // [Auth] 게스트 로그인
    @POST("api/auth/guest")
    suspend fun createGuestAccount(
        @Body request: GuestLoginRequest
    ): GuestLoginResponse

    @GET("api/auth/me")
    suspend fun getMyInfo(): MyInfoResponse

    @POST("api/auth/logout")
    suspend fun logout(): LogoutResponse

    @DELETE("api/auth/account")
    suspend fun withdraw(): BaseResponse<Unit>

    // [Auth] 카카오 SDK 로그인
    @POST("api/auth/kakao/sdk")
    suspend fun kakaoLogin(@Body request: KakaoLoginRequest): KakaoLoginResponse

    @POST("api/auth/social/complete")
    suspend fun completeSocialSignup(@Body request: SocialCompleteRequest): KakaoLoginResponse

    @GET("api/auth/terms")
    suspend fun getTerms(): BaseResponse<List<TermsResponse>>


    // ----------------------------------------------------
    // [Word] 낱말 카드 관련
    // ----------------------------------------------------

    // [Main] 단어 목록 조회
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


    // ----------------------------------------------------
    // [Category]
    // ----------------------------------------------------

    // [Category] 카테고리 목록 조회
    @GET("api/categories")
    suspend fun getCategories(): BaseResponse<List<CategoryResponse>>

    // [Category] 카테고리 생성
    @POST("api/categories")
    suspend fun createCategory(
        @Body request: CreateCategoryRequest
    ): BaseResponse<CategoryResponse>

    // [Category] 카테고리 수정
    @PATCH("api/categories/{id}")
    suspend fun updateCategory(@Path("id") id: String, @Body request: UpdateCategoryRequest): BaseResponse<CategoryResponse>

    // [Category] 카테고리 삭제
    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(
        @Path("id") id: String
    ): BaseResponse<DeleteCategoryResponse>

    // [Category] 카테고리 순서 변경
    @PATCH("api/order/categories")
    suspend fun updateCategoryOrders(
        @Body request: CategoryOrderRequest
    ): BaseResponse<CategoryOrderResponse>


    // ----------------------------------------------------
    // [Setting]
    // ----------------------------------------------------

    // [Settings] 설정 관련
    @GET("api/settings/grid")
    suspend fun getGridSetting(): GridSettingResponse

    @PATCH("api/settings/grid")
    suspend fun updateGridSetting(
        @Body request: GridSettingRequest
    ): GridSettingResponse


    // ----------------------------------------------------
    // [AI]
    // ----------------------------------------------------

    // [AI] TTS 및 예측 관련
    @GET("api/ai/tts-settings")
    suspend fun getTtsSetting(): BaseResponse<TtsSettingData>

    @POST("api/ai/tts")
    suspend fun generateTts(@Body request: TtsRequest): ResponseBody

    // [AI] TTS (MP3 바이너리 응답) - 루틴 모달 등에서 사용
    @Streaming
    @POST("api/ai/tts")
    @Headers(
        "Accept: audio/mpeg",
        "Content-Type: application/json"
    )
    suspend fun requestTtsMp3(
        @Body request: TtsRequest
    ): Response<ResponseBody>

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


    // ----------------------------------------------------
    // [Routine - 자동 출력 문장]
    // ----------------------------------------------------

    // [Routine] 목록 조회
    @GET("api/routines")
    suspend fun getRoutines(): BaseResponse<RoutinesDataDto>

    // [Routine] 생성
    @POST("api/routines")
    suspend fun createRoutine(
        @Body request: CreateRoutineRequest
    ): BaseResponse<RoutineDto>

    // [Routine] 수정
    @PATCH("api/routines/{id}")
    suspend fun updateRoutine(
        @Path("id") id: String,
        @Body body: RoutineUpdateRequest
    ): RoutineUpdateResponse

    // [Routine] 선택 삭제
    @HTTP(method = "DELETE", path = "api/routines", hasBody = true)
    suspend fun deleteRoutines(
        @Body body: DeleteRoutinesRequest
    ): BaseResponse<DeleteRoutinesResponse>

    // [Routine] 전체 삭제
    @DELETE("api/routines/all")
    suspend fun deleteAllRoutines(): BaseResponse<DeleteAllRoutinesResponse>

    // ----------------------------------------------------
    // [Routine Modal - 자동 출력 문장 모달]
    // ----------------------------------------------------

    // [Routine Modal] 현재 시간에 해당하는 루틴 1건 조회
    @GET("api/routines/modal")
    suspend fun getRoutineModal(): BaseResponse<RoutineModalResponse>

    // [Routine Modal] 다시 알림 (snooze)
    @POST("api/routines/{id}/modal/snooze")
    suspend fun snoozeRoutineModal(
        @Path("id") id: String,
        @Body body: SnoozeRequest
    ): BaseResponse<RoutineModalResponse>

    // [Routine Modal] 오늘 끄기 (dismiss)
    @POST("api/routines/{id}/modal/dismiss")
    suspend fun dismissRoutineModal(
        @Path("id") id: String
    ): BaseResponse<RoutineModalResponse>

    @Streaming
    @POST("api/ai/tts-settings/preview")
    @Headers(
        "Accept: audio/mpeg",
        "Content-Type: application/json"
    )
    suspend fun requestTtsPreviewMp3(
        @Body request: TtsPreviewRequest
    ): Response<ResponseBody>

    @PUT("api/ai/tts-settings")
    suspend fun updateTtsSetting(
        @Body request: TtsSettingUpdateRequest
    ): BaseResponse<TtsSettingData>
}