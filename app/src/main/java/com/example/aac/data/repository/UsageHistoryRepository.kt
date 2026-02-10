package com.example.aac.data.repository

import com.example.aac.data.mapper.toDomain
import com.example.aac.data.remote.api.RetrofitInstance
import com.example.aac.domain.model.UsageHistory

class UsageHistoryRepository {

    // 1. 아까 만든 RetrofitInstance를 통해 API 서비스 가져오기
    private val api = RetrofitInstance.api

    // 2. ViewModel이 찾고 있는 바로 그 함수!
    suspend fun getHistoryList(year: Int, month: Int): List<UsageHistory> {
        return try {
            // API 명세서에 있는 함수 호출 (AacApiService에 정의된 함수)
            val response = api.getHistories(year, month)

            if (response.success) {
                // 성공하면 DTO -> Domain Model로 변환해서 반환
                response.data.histories.map { it.toDomain() }
            } else {
                // 실패하면(success가 false면) 빈 리스트 반환
                emptyList()
            }
        } catch (e: Exception) {
            // 네트워크 에러 등이 나면 에러 로그 찍고 빈 리스트 반환
            e.printStackTrace()
            emptyList()
        }
    }
}