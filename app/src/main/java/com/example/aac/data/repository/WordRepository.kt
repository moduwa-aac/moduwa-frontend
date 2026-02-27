package com.example.aac.data.repository

import android.util.Log
import com.example.aac.data.mapper.WordMapper
import com.example.aac.data.remote.api.RetrofitInstance
import com.example.aac.domain.model.Word

class WordRepository {

    suspend fun getWords(): List<Word> {
        return try {
            val response = RetrofitInstance.api.getWords()

            // 1. success가 true이고, data가 null이 아닐 때만 실행
            if (response.success && response.data != null) {

                // ✅ [수정] response가 아니라 response.data를 넘겨야 함!
                WordMapper.mapToDomain(response.data)

            } else {
                Log.e("WordRepository", "서버 에러 또는 데이터 없음: ${response.message}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("WordRepository", "네트워크 에러: ${e.message}")
            emptyList()
        }
    }
}