package com.example.aac.feature.ai_sentence.ui

import com.example.aac.feature.ai_sentence.model.SentenceItemUiModel

data class AiSentenceUiState(
    val selectedWords: List<String> = emptyList(),
    val sentences: List<SentenceItemUiModel> = emptyList()
)

sealed interface AiSentenceUiEvent {
    data object ClickPlayTop : AiSentenceUiEvent
    data class ClickEdit(val id: String) : AiSentenceUiEvent
    data class ClickFavorite(val id: String) : AiSentenceUiEvent
    data class ClickPlaySentence(val id: String) : AiSentenceUiEvent
}
