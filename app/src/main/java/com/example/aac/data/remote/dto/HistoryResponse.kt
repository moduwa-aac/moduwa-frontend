package com.example.aac.data.remote.dto

data class HistoryResponse(
    val success: Boolean,
    val data: HistoryData,
    val message: String
)

data class HistoryData(
    val histories: List<HistoryDto>
)

data class HistoryDto(
    val id: String,
    val inputWords: List<InputWordDto>,
    val selectedSentence: String,
    val createdAt: String
)

data class InputWordDto(
    val word: String,
    val order: Int,
    val wordId: String
)