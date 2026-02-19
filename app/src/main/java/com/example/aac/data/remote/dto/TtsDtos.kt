package com.example.aac.data.remote.dto

import com.google.gson.annotations.SerializedName

// TTS 설정 조회 응답 데이터
data class TtsSettingData(
    @SerializedName("voiceKey") val voiceKey: String,
    @SerializedName("isDefault") val isDefault: Boolean
)

// TTS 오디오 요청 데이터
data class TtsRequest(
    @SerializedName("text") val text: String,
    @SerializedName("voiceKey") val voiceKey: String
)

data class TtsPreviewRequest(
    @SerializedName("voiceKey") val voiceKey: String
)

data class TtsSettingUpdateRequest(
    @SerializedName("voiceKey") val voiceKey: String
)