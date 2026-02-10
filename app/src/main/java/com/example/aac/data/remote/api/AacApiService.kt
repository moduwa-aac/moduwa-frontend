package com.example.aac.data.remote.api
import com.example.aac.data.remote.dto.GridSettingRequest
import com.example.aac.data.remote.dto.GridSettingResponse
import com.example.aac.data.remote.dto.GuestLoginRequest
import com.example.aac.data.remote.dto.GuestLoginResponse
import com.example.aac.data.remote.dto.HistoryResponse
import com.example.aac.data.remote.dto.WordResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query
import java.time.Year

interface AacApiService {

    // [Auth] 게스트 로그인
    @POST("api/auth/guest")
    suspend fun createGuestAccount(
        @Body request: GuestLoginRequest
    ): GuestLoginResponse

    // [Main] 단어 목록 조회
    @GET("api/words")
    suspend fun getWords(): WordResponse

    // [Setting] 그리드 설정 조회
    @GET("api/settings/grid")
    suspend fun getGridSetting(): GridSettingResponse

    // [Setting] 그리드 설정 수정
    @PATCH("api/settings/grid")
    suspend fun updateGridSetting(
        @Body request: GridSettingRequest
    ): GridSettingResponse
    @GET("api/histories")
    suspend fun getHistories(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): HistoryResponse
}