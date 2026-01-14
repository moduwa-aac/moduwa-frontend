package com.example.aac.feature.ai_sentence.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.example.aac.feature.ai_sentence.model.SentenceItemUiModel
import java.util.UUID

class AiSentenceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        AiSentenceUiState(
            selectedWords = listOf("선택한 낱말", "픽토그램", "낱말", "픽토", "그림"),
            sentences = listOf(
                SentenceItemUiModel(UUID.randomUUID().toString(), "어제 라면을 먹고 잤더니 부었어요", false),
                SentenceItemUiModel(UUID.randomUUID().toString(), "어제 라면을 먹고 잤더니 부었나요?", true),
                SentenceItemUiModel(UUID.randomUUID().toString(), "어제 라면을 먹고 자지 않아서 붓지 않았어요.", false),
            )
        )
    )
    val uiState: StateFlow<AiSentenceUiState> = _uiState

    fun onEvent(event: AiSentenceUiEvent) {
        when (event) {
            is AiSentenceUiEvent.ClickFavorite -> {
                _uiState.update { state ->
                    state.copy(
                        sentences = state.sentences.map {
                            if (it.id == event.id) it.copy(isFavorite = !it.isFavorite) else it
                        }
                    )
                }
            }
            else -> Unit
        }
    }
}
