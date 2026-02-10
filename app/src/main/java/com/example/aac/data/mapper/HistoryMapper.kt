package com.example.aac.data.mapper

import com.example.aac.data.remote.dto.HistoryDto
import com.example.aac.domain.model.UsageHistory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun HistoryDto.toDomain(): UsageHistory {
    return UsageHistory(
        id = this.id,
        sentence = this.selectedSentence,
        // UsageHistory 모델의 변수명이 'data'이므로 맞춰서 수정
        date = formatDateTime(this.createdAt),
        words = this.inputWords.map { it.word }
    )
}

/**
 * API 24에서도 에러 없이 작동하는 날짜 포맷팅 함수
 * "2026-01-30T17:24:05.128Z" -> "2026.01.30"
 */
private fun formatDateTime(isoString: String): String {
    return try {
        // 1. 서버의 ISO 8601 형식을 해석하기 위한 파서
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC") // 서버 시간대 기준 (Z)
        }

        // 2. 앱 화면에 보여줄 형식
        val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

        val parsedDate = parser.parse(isoString)
        if (parsedDate != null) formatter.format(parsedDate) else isoString
    } catch (e: Exception) {
        isoString // 포맷팅 실패 시 원본 문자열 반환
    }
}